# Guia de estudo: entrevista técnica do Sulwork Eventos

Perguntas prováveis e respostas-modelo, baseadas no código e no README do projeto. Estude **falando em voz alta**, com as suas palavras. Decorar o texto soa artificial; o que importa é você saber o **porquê** de cada decisão.

Como usar: leia a seção "Pitch de 1 minuto", depois passe pelas perguntas de cada bloco. No fim há um bloco de "perguntas armadilha" e uma lista de pontos fracos para você assumir com tranquilidade, em vez de ser pego de surpresa.

---

## 0. Pitch de 1 minuto

> O Sulwork Eventos organiza eventos de alimentação da empresa (café da manhã, almoço, churrasco). O organizador cadastra o evento com categorias e itens, os colaboradores se inscrevem e combinam quem leva o quê, e todo mundo vê **quanto já foi atendido** e **o que falta**, considerando as restrições alimentares de quem vai.
>
> O backend é Java 21 com Spring Boot 3.5, PostgreSQL e Flyway. O frontend é React com TypeScript e Vite. Está publicado: frontend na Vercel, API no Render e banco no Neon.
>
> Os pontos que mais me orgulho são: a **máquina de estados** do evento com a regra de **prazo × status**, o **lock pessimista** que garante o limite de quantidade sob concorrência (com teste que prova), o **cálculo de cobertura** e a feature livre **"O que falta?"**.

---

## 1. Visão geral e produto

**P: Qual problema o sistema resolve?**
Quem organiza um café da manhã coletivo costuma usar planilha, e ninguém sabe o que ainda falta, o que é essencial, nem se o item serve para quem tem alergia. O sistema centraliza isso: itens, quem leva cada um, cobertura em percentual e alertas de alérgenos.

**P: Quais são as entidades principais?**
`Evento` → `Categoria` → `Item` → `Contribuição` ← `Participante`, e `Inscrição` ligando participante e evento.
Regras de unicidade: CPF único no participante; inscrição única por (evento, participante); contribuição única por (item, participante); nome de categoria único dentro do evento.

**P: O enunciado era ambíguo em algum ponto? O que você fez?**
Sim, e documentei 12 ambiguidades no README. As principais:
- **Prazo × status:** decidi que o participante só altera algo se o evento está `ABERTO` **e** o prazo não venceu. As duas condições são independentes.
- **Meta da categoria:** interpretei como a quantidade mínima de unidades que a categoria deve reunir.
- **Alérgeno bloqueia contribuição?** Não, só alerta. O item ainda serve aos outros inscritos.
- **Organizador sujeito ao prazo?** Não. O prazo protege o compromisso dos participantes, não o planejamento.
- **Login?** O desafio não pede, então não fiz, e registrei isso como premissa.

Dica: se perguntarem "por que não perguntou ao avaliador?", responda que registrou a decisão e o raciocínio para que fosse revisável.

---

## 2. Arquitetura

**P: Como o backend está organizado?**
Por **funcionalidade**, não por camada técnica. Cada pacote (`evento`, `item`, `contribuicao`, `cobertura`...) tem entidade, repositório, service, controller e DTOs. Isso deixa o código de uma feature todo junto e reduz o acoplamento entre pacotes. Dentro de cada pacote o fluxo é `controller → service → repository`.

**P: Por que DTOs e não devolver a entidade?**
Três motivos: (1) o contrato da API não fica preso ao modelo do banco; (2) evita problemas de serialização de relações lazy; (3) permite incluir **campos derivados** (`aceitaAlteracoes`, `transicoesPermitidas`, `quantidadeFaltante`) para o frontend não duplicar regra de negócio. Usei `record` do Java.

**P: O que é "rich domain model" no seu projeto?**
As invariantes que dependem só da própria entidade ficam nela. Exemplo: `Evento.transicionarPara()` e `Evento.garantirJanelaDeAlteracoes()`. Assim são testadas com JUnit puro, sem Spring. Regras que dependem de outras entidades ou do banco (limite de quantidade, inscrição obrigatória) ficam nos services.

**P: Por que injetou o `Clock`?**
Toda regra que usa "agora" (prazo, abrir evento, marcar realizado) usa o mesmo `Clock`. Nos testes de integração troco por um relógio ajustável e testo prazo vencido **sem `Thread.sleep`**, de forma determinística.

**P: Por que Flyway e `ddl-auto: validate`?**
O schema fica explícito, versionado e revisável (5 migrations). O Hibernate só **valida** na subida que as entidades batem com o banco, e nunca altera o schema sozinho. `update` em produção é arriscado e não deixa histórico.

**P: O que é `open-in-view` e por que desligou?**
O padrão do Spring Boot mantém a sessão do Hibernate aberta até a serialização da resposta, o que permite consultas lazy "escondidas" no controller/JSON. Desliguei para que **toda leitura aconteça dentro das transações dos services**, o que deixa o custo de consultas visível e evita `LazyInitializationException` mascarado.

**P: Quais padrões de projeto usou e por quê?**
Máquina de estados (`StatusEvento`), Repository (Spring Data), DTO, rich domain model e pessimistic locking. E **rejeitei** Strategy na cobertura: só existe uma fórmula, então uma classe pura e testada é mais simples. Abstração sem segunda implementação é só indireção.

---

## 3. Regras de negócio

**P: Explique a máquina de estados do evento.**
```
RASCUNHO → ABERTO, CANCELADO
ABERTO → ENCERRADO, CANCELADO
ENCERRADO → ABERTO (reabrir), REALIZADO, CANCELADO
REALIZADO, CANCELADO → estados finais
```
Fica numa tabela (`EnumMap<StatusEvento, Set<StatusEvento>>`) dentro do enum, fácil de ler e de testar. Além da tabela há guardas de tempo na entidade:
- abrir/reabrir exige **prazo não vencido** (e, para abrir um rascunho, **ao menos um item**);
- marcar como realizado exige que a **data do evento já tenha chegado**.

**P: Como prazo e status interagem?**
Regra central, em `Evento.aceitaAlteracoesDeParticipantes`: `status == ABERTO && !prazoVencido(agora)`. O prazo é checado **a cada requisição**, não por job agendado.

**P: Por que não usou um job `@Scheduled` para encerrar o evento quando o prazo vence?** *(pergunta favorita de entrevistador)*
Três razões:
1. Com job existe uma **janela** entre o vencimento e a execução do job em que o prazo venceu, mas o sistema ainda aceita alteração.
2. O **status é uma decisão do organizador**; o prazo é uma trava automática. Misturar os dois tira o controle dele (por exemplo, estender o prazo).
3. Testes ficam **determinísticos** com o relógio injetado; com job, ficariam dependentes de tempo real.
Essa foi uma sugestão de IA que eu rejeitei de forma consciente, e isso está documentado no README.

**P: O que o organizador pode fazer depois que o prazo vence?**
Se o evento está `RASCUNHO` ou `ABERTO`, pode ajustar categorias e itens, estender o prazo ou encerrar. Proteções: a quantidade necessária de um item não pode ficar menor que o já contribuído, e item com contribuições não pode ser excluído.

**P: O que acontece se alguém cancela a inscrição?**
As contribuições dessa pessoa naquele evento são removidas, porque quem não vai não leva nada. Só é possível dentro da janela de alterações. Participante inscrito em algum evento não pode ser excluído (preserva histórico).

**P: Por que só rascunho pode ser excluído?**
Eventos publicados têm inscrições e contribuições, então são **cancelados** (mantém histórico). Excluir só faz sentido para algo que nunca foi publicado.

**P: Como trata o CPF?**
Aceita com ou sem máscara, guarda só os dígitos, valida os dígitos verificadores e rejeita sequências repetidas (`111.111.111-11`). Não pode ser alterado depois, pois identifica o colaborador. A API **nunca devolve o CPF completo**, só `cpfMascarado` (ex.: `***.982.247-**`).

**P: Como as datas são tratadas?**
`LocalDateTime` em horário de São Paulo (Hibernate e Jackson configurados com `America/Sao_Paulo`). Premissa: eventos presenciais na empresa. Em um sistema multi-fuso eu usaria `OffsetDateTime`/`Instant`.

---

## 4. Concorrência (provavelmente a pergunta mais técnica)

**P: Duas pessoas contribuem ao mesmo tempo com o mesmo item. O que acontece?**
Sem proteção, as duas transações leem a mesma soma (ex.: 8 de 10), cada uma valida "cabe 2" e ambas gravam, estourando para 12. É um *race condition* clássico de check-then-act.

Solução: em `ContribuicaoService`, antes de somar, chamo `travarItem`, que faz `SELECT ... FOR UPDATE` na linha do item (`@Lock(PESSIMISTIC_WRITE)` no `ItemRepository`). A segunda transação **espera** a primeira commitar e então lê a soma atualizada. Isso serializa contribuições **no mesmo item**, sem bloquear o resto do evento.

**P: Como você provou que funciona?**
Teste de integração com Testcontainers: 4 requisições simultâneas de 4 unidades num item de 10. Esperado: exatamente 2 aceitas. Rodei **com e sem o lock**: sem o lock, mais do que o permitido era aceito; com ele, exatamente 2 de 4. Um teste que nunca falhou não prova nada, por isso removi o lock para ver ele falhar.

**P: Por que lock pessimista e não otimista (`@Version`)?**
- O alvo é a **soma de várias linhas** (contribuições) contra um limite que está em outra linha (item). Versionar o item não protege a soma, a menos que eu também atualize o item a cada contribuição.
- Com otimista, em disputa alta eu teria retries e erros para o usuário. Aqui a contenção é pequena e localizada (um item), então esperar um lock curto é barato e mais simples.
- Alternativas que eu consideraria em escala maior: coluna `quantidade_contribuida` no item com `UPDATE ... WHERE contribuido + :q <= necessario` (atômico, sem lock explícito), ou constraint/trigger.

**P: Existe risco de deadlock?**
Cada operação trava **uma** linha de item por transação, então não há ordem de aquisição conflitante. Em `alterarQuantidade` e `assumir` também é um único item. O risco de deadlock é praticamente nulo.

**P: A unicidade de inscrição também está protegida contra corrida?**
Sim. O service checa e devolve mensagem clara, mas existe também `UNIQUE (evento, participante)` no banco. Se duas requisições passarem pela checagem juntas, o banco rejeita a segunda. A checagem do service é para UX; a constraint é para integridade.

---

## 5. Cobertura

**P: Como calcula a cobertura?**
Uma consulta SQL **nativa** agrega tudo numa ida ao banco: `categoria LEFT JOIN item LEFT JOIN contribuicao`, `SUM` e `GROUP BY`. O `LEFT JOIN` mantém categorias sem itens (relevante se têm meta) e itens sem contribuição. O cálculo está em `CalculadoraCobertura`, classe pura sem Spring além da anotação.

Fórmula:
1. **Item:** `min(contribuído / necessário, 1)`.
2. **Categoria sem meta:** média ponderada dos itens (obrigatório peso 2, opcional peso 1).
3. **Categoria com meta:** `min(total contribuído / meta, 1)`. A meta prevalece.
4. **Categoria sem itens e sem meta:** ignorada.
5. **Geral:** média simples das categorias consideradas.
6. **Pronto:** todos os obrigatórios completos **e** todas as metas atingidas. Indicador separado do percentual.

**P: Por que razão em vez de somar unidades?**
Com unidades absolutas, 50 guardanapos entregues e 0 de 2 bolos dariam 96%. Pela razão dá 50%, que é a realidade. Um item de 50 unidades não pode pesar mais que um de 2 só por ter números maiores.

**P: Por que "pronto" é separado do percentual?**
Um evento com 95% mas sem o item obrigatório principal (a carne do churrasco, por exemplo) **não está pronto**. E um evento sem nenhum opcional pode estar pronto com menos de 100%.

**P: Por que arredondar para baixo?**
Para só mostrar 100% quando está de fato completo. Com 999/1000, mostrar 100% enganaria; mostra 99%. No código: `Math.floor(razao * 100 + 1e-9)`; o `1e-9` evita que erro de ponto flutuante transforme 0,29 × 100 = 28,999… em 28.

**P: Você mudou a fórmula durante o desenvolvimento?**
Sim. A primeira proposta combinava média dos itens com a meta dentro da mesma categoria, e isso gerava casos difíceis de explicar (meta atingida mas categoria abaixo de 100%). Simplifiquei: **com meta, a meta define a categoria; sem meta, vale a média dos itens**. Os obrigatórios continuam cobrados à parte no "pronto".

**P: E se a meta for maior que a soma dos itens?**
Nunca será atingida, porque contribuição não passa do necessário. Calculo `metaAtingivel` e o frontend sinaliza para o organizador ajustar.

**P: Por que SQL nativo aqui e JPA no resto?**
É uma agregação por grupo com junções; em JPA eu carregaria entidades para somar em memória (muitas consultas e objetos). Um `GROUP BY` resolve em uma consulta. Uso JPA/JPQL onde o modelo de objetos ajuda, e SQL onde o problema é de agregação.

---

## 6. Feature livre: "O que falta?"

**P: Qual foi a feature livre e por que ela?**
Lista **apenas os itens incompletos**, priorizados, com um botão **"Eu levo"**.
- Prioridade **ALTA**: item obrigatório. **MÉDIA**: opcional de categoria com meta não atingida. **BAIXA**: demais opcionais.
- Dentro da mesma prioridade: menos coberto primeiro, depois maior quantidade faltante.
- Mostra quais inscritos seriam afetados por alérgenos **no momento em que alguém decide o que levar**.

Motivo: o problema real é que numa planilha todo mundo leva o que é fácil (refrigerante) e falta o essencial. A lista guia a decisão.

**P: Como o "Eu levo" funciona?**
Sem quantidade, assume **tudo o que falta**. Com quantidade, só aquela parte. Se a pessoa já contribui com o item, **soma** à contribuição existente em vez de dar 409, porque a intenção ("quero levar mais") é clara. Passa pelas **mesmas regras e pelo mesmo lock** (janela, inscrição, limite), sem caminho alternativo que possa burlar.

**P: Por que reaproveitou o cálculo de cobertura?**
Para a lista mostrar exatamente os mesmos números do resto da aplicação. Uma segunda fórmula seria dois lugares para manter e uma fonte de inconsistência.

---

## 7. Restrições alimentares e LGPD

**P: Como modelou restrições alimentares?**
Lista fechada de alérgenos (glúten, lactose, ovo, amendoim, castanhas, soja, peixe, crustáceos, gergelim) usada **tanto em participantes quanto em itens**, o que torna o cruzamento confiável, mais um campo de texto livre para o que não é alérgeno (vegetariano, etc.). Texto livre dos dois lados não cruzaria de forma confiável.

**P: Como o cruzamento é feito?**
`GET /eventos/{id}/restricoes` considera só os **inscritos**, não todos os cadastrados. Para cada item, retorna quais inscritos são afetados e se é seguro para todos.

**P: O que a LGPD tem a ver com isso?**
Restrição alimentar é **dado sensível** (referente à saúde, Art. 5º, II). Usei só para a finalidade de segurança alimentar. Apliquei **minimização** no CPF (Art. 6º, III): a API não devolve o CPF completo, e a busca aceita só o CPF inteiro. O README lista o que faltaria em produção: consentimento/base legal, retenção e anonimização após o evento, direitos do titular (Art. 18) e auditoria de acesso.

**P: Por que a busca por CPF só aceita o número completo?** *(foi achado da revisão de segurança)*
A máscara mostra os 6 dígitos do meio e os dois últimos são calculados a partir dos nove primeiros. Se a busca aceitasse pedaços, dava para reconstruir o CPF inteiro com no máximo ~mil buscas. Corrigi e há teste de integração.

---

## 8. Segurança

**P: Que cuidados de segurança tomou?**
- **SQL injection:** tudo parametrizado, inclusive a consulta nativa.
- **Validação de entrada:** Bean Validation (`@Valid`), tamanhos máximos e teto de 100.000 em quantidades/metas.
- **Erros padronizados:** sem stack trace para o cliente.
- **Actuator:** só `/actuator/health` exposto.
- **CORS** restrito ao site publicado e ao ambiente local.
- **Cabeçalhos:** CSP, `X-Frame-Options: DENY`, `frame-ancestors 'none'`, `nosniff`, Referrer-Policy e Permissions-Policy (Vercel e nginx).
- **XSS:** React escapa o conteúdo; sem `dangerouslySetInnerHTML`.
- **Segredos:** senha do banco só nas variáveis do Render; histórico do Git verificado; `.env` ignorado.
- **Contêiner** do backend com usuário sem privilégio.

**P: Qual a maior fraqueza?**
**Não há autenticação nem autorização**: qualquer pessoa pode criar, alterar ou excluir dados e ver as restrições dos inscritos. Foi decisão consciente por não constar no enunciado, documentada. Em produção: SSO corporativo com papéis (organizador/participante), restrições visíveis só para quem precisa, rate limiting e auditoria.

**P: E o `npm audit`?**
Os críticos são do `vitest` (só desenvolvimento, não vai para o site publicado). Os moderados do `react-router` exigem condições que o projeto não usa. Corrigir exige troca de versão principal; ficou registrado para depois. Em produção eu automatizaria com Dependabot no CI.

---

## 9. Testes

**P: Qual a estratégia de testes?**
Pirâmide: **unitários rápidos** para regra pura (`StatusEventoTest`, `EventoTest`, `CpfTest`, `CalculadoraCoberturaTest`, `AnalisadorRestricoesTest`, `ContribuicaoServiceTest` com Mockito) e **integração** (`*IT`) subindo Spring Boot com **PostgreSQL real via Testcontainers** e MockMvc. Total: 118 no backend e 53 no frontend (Vitest + Testing Library).

**P: Por que Testcontainers em vez de H2?**
H2 se comporta diferente do PostgreSQL (SQL nativo, locks, tipos). O `SELECT ... FOR UPDATE` e a consulta de cobertura precisam ser testados no banco real, senão o teste de concorrência não prova nada.

**P: O que é testado nos testes de integração?**
Fluxos completos por HTTP, códigos de erro, CPF que nunca sai completo, busca que rejeita pedaço de CPF, teto de quantidades, avanço do relógio para testar prazo e a concorrência no limite do item.

**P: O que você diria que falta testar?**
Testes de ponta a ponta no navegador com assertivas automatizadas (usei Playwright para verificação manual do fluxo, mas não ficou como suíte), testes de carga e um pipeline de CI rodando tudo a cada push.

---

## 10. Performance e produção

**P: Teve problema de performance?**
Sim, e é uma boa história para contar. Após o deploy, cada ação demorava ~10 s. Os testes não mostravam, porque localmente tudo é rápido. Medi as consultas da página do evento: **71 por ação**, por causa do **N+1** (alérgenos e restrições carregados um registro por vez). Configurei `hibernate.default_batch_fetch_size`, e caiu para **30** consultas (as coleções passam a ser buscadas com `IN (...)`). Também ajustei a JVM para o servidor gratuito (0,1 CPU).

Lição: observar o comportamento em produção, não só os testes.

**P: O que é o problema N+1?**
Uma consulta para listar N registros e depois mais N consultas, uma para carregar a relação de cada um. Soluções: `join fetch`, `@EntityGraph`, batch fetching (`default_batch_fetch_size`) ou projeções/DTOs direto na consulta.

**P: Por que três plataformas diferentes (Vercel + Render + Neon)?**
A Vercel não executa uma aplicação Spring Boot contínua, então o frontend ficou lá e o backend em Docker no Render. O Neon (PostgreSQL) não expira no plano gratuito. O primeiro acesso à API pode levar até 1 minuto porque o Render gratuito hiberna.

**P: Por que chamou a API direto com CORS em vez de usar o proxy da Vercel?**
O proxy (`rewrites`) corta a requisição antes de o servidor gratuito acordar. Chamando direto, a requisição espera o cold start.

**P: Como escalaria isso?**
- Backend é *stateless*, então escala horizontalmente atrás de um balanceador.
- O lock é no banco, então continua correto com várias instâncias (um lock em memória Java não funcionaria).
- Cache da cobertura se virasse gargalo, invalidado ao mudar contribuição.
- Índices conforme o padrão de consulta, paginação nas listagens grandes, observabilidade (métricas, logs estruturados, tracing).

---

## 11. Frontend

**P: Como o frontend está estruturado?**
`api` (cliente REST tipado), `components` (reutilizáveis), `pages` (Visão geral, Eventos, Evento com abas, Minhas contribuições, Participantes) e `utils` (CPF, formatação, prazo).

**P: Onde ficam as regras de negócio no frontend?**
Em lugar nenhum de propósito. O backend é a fonte de verdade; o frontend usa campos derivados (`aceitaAlteracoes`, `transicoesPermitidas`) para habilitar/desabilitar ações e exibe a mensagem de erro devolvida pela API. Duplicar regra leva a divergência.

**P: Como tratou acessibilidade?**
- Contraste: texto branco sobre `#4779F5` dá 3,95:1, abaixo do mínimo de 4,5:1 da WCAG AA. Usei `#3366E6` (5,0:1) nos botões e deixei `#4779F5` para barras, ícones e foco, onde o mínimo é 3:1.
- Capa do evento marcada como "Capa ilustrativa" para leitores de tela.
- Bug achado pelos testes: a mensagem "CPF inválido" estava dentro do `<label>`, o que alterava o nome acessível do campo. Movi para fora e liguei com `aria-describedby`.
- Botões desabilitados mostram o **motivo**.

**P: Por que o seletor "Você é"?**
Como não há login, ele identifica quem está agindo (usado em "Eu levo" e "Minhas contribuições"). Guarda só o id no navegador. É um substituto honesto para autenticação, não uma solução de segurança.

**P: Por que o estado de "pronto/sem eventos" não mostra 0%?**
Sem eventos abertos, a cobertura média aparece como "—", porque 0% seria um número enganoso. O projeto evita métricas inventadas.

---

## 12. Uso de IA (prepare-se, é quase certo que perguntam)

**P: Como usou IA?**
Claude Code durante todo o desenvolvimento: levantamento de ambiguidades, implementação, testes, verificação da interface com Playwright, deploy e revisão de segurança. O design visual veio de protótipos do Stitch.

**P: Você entende o código que a IA gerou?**
Sim, e consigo explicar cada decisão. **Prepare-se para abrir qualquer arquivo e explicar.** Reveja principalmente `ContribuicaoService`, `Evento`, `StatusEvento`, `CalculadoraCobertura` e `CoberturaRepository`.

**P: Cite algo em que discordou da IA.**
- Rejeitei o job `@Scheduled` para encerrar eventos (ver seção 3).
- Rejeitei Strategy para a cobertura (abstração sem segunda implementação).
- Alterei a fórmula de cobertura (ficou mais simples).
- Do protótipo do Stitch, tirei fotos, perfil com login, notificações, valores em R$ e histórico de atividades, porque **não existem no domínio** e mostrariam números inventados.

**P: Cite algo em que aceitou a sugestão e como validou.**
O lock pessimista. Validei rodando o teste de concorrência com e sem o lock.

**P: Onde precisou de julgamento próprio?**
Diagnosticar a lentidão em produção (N+1), escolher onde hospedar cada parte, interpretar as regras abertas do enunciado, definir a fórmula de cobertura e decidir o que **deixar de fora** para manter o escopo coerente.

---

## 13. Perguntas armadilha e respostas honestas

**"Se pudesse refazer, o que mudaria?"**
Autenticação desde o início (mesmo simples), `@Version`/constraints adicionais como segunda camada de proteção, CI automatizado com testes e varredura de dependências, e suíte E2E automatizada.

**"Por que `LocalDateTime` e não `Instant`?"**
Por simplicidade e porque todos os eventos são presenciais em um fuso só. Reconheço a limitação: multi-fuso exigiria `Instant`/`OffsetDateTime`.

**"Por que não paginou as listagens?"**
O volume esperado (eventos de uma empresa) é pequeno. Com crescimento eu adicionaria paginação (`Pageable`) nas listagens de eventos, participantes e contribuições.

**"O lock pessimista não é um gargalo?"**
Só serializa contribuições **no mesmo item**, e a transação é curta. Itens diferentes seguem em paralelo.

**"Qualquer um pode excluir tudo, isso é aceitável?"**
Não em produção. É uma limitação declarada do escopo do desafio, com plano claro: SSO, papéis, rate limiting e auditoria.

**"E se o Render estiver dormindo na hora da demo?"**
O primeiro acesso pode levar até 1 minuto. Abra a aplicação alguns minutos antes de entrar na entrevista para "acordar" a API.

**"Como você sabe que a cobertura está correta?"**
`CalculadoraCobertura` é uma classe pura com testes para pesos, razão × unidades, metas, metas inatingíveis, arredondamento e "pronto", mais testes de integração que passam pela consulta SQL real.

---

## 14. Checklist antes da entrevista

- [ ] Abrir a aplicação publicada alguns minutos antes (cold start do Render).
- [ ] Rodar o projeto localmente (`docker compose up --build`) como plano B.
- [ ] Reler `ContribuicaoService`, `Evento`, `StatusEvento`, `CalculadoraCobertura`, `CoberturaRepository` e `ItemRepository`.
- [ ] Ser capaz de desenhar de memória: o diagrama de entidades e a máquina de estados.
- [ ] Ser capaz de explicar a corrida de concorrência com um exemplo numérico (8 de 10, duas pessoas tentando levar 2).
- [ ] Ter pronta a história do N+1 (71 → 30 consultas).
- [ ] Ter 2 exemplos de "discordei da IA" e 1 de "aceitei e validei".
- [ ] Rodar `./mvnw verify` e `npm test` uma vez para ver tudo verde.
- [ ] Revisar o Swagger UI para mostrar a API ao vivo.

## 15. Glossário rápido

| Termo | Definição curta |
|---|---|
| Race condition | Resultado depende da ordem de execução de operações concorrentes. |
| Lock pessimista | Trava o recurso ao lê-lo (`FOR UPDATE`); os demais esperam. |
| Lock otimista | Não trava; detecta conflito na gravação (`@Version`) e rejeita. |
| N+1 | 1 consulta principal + N consultas para relações de cada item. |
| Open-in-view | Sessão do Hibernate aberta até o fim da requisição web. |
| DTO | Objeto de transferência usado na borda da API. |
| Testcontainers | Biblioteca que sobe contêineres Docker reais (ex.: PostgreSQL) nos testes. |
| Flyway | Ferramenta de migrations versionadas do banco. |
| CORS | Mecanismo do navegador que controla chamadas entre origens diferentes. |
| CSP | `Content-Security-Policy`: restringe de onde o site carrega scripts e conexões. |
| LGPD | Lei Geral de Proteção de Dados (Lei 13.709/2018). |
