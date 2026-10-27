# Sulwork Eventos

Aplicação para organizar os eventos de alimentação da Sulwork (cafés da manhã, almoços, churrascos...). O organizador cadastra o evento com categorias e itens, os colaboradores se inscrevem e combinam quem leva o quê, e todos acompanham **quanto das necessidades já foi atendido** e **o que ainda falta**, levando em conta as restrições alimentares de quem vai participar.

- **Repositório:** https://github.com/MatheusIago99/Desafio-Sulwork
- **Aplicação publicada:** _(link da publicação)_
- **Swagger/OpenAPI:** `http://localhost:8080/swagger-ui.html` (local) · JSON em `/v3/api-docs`

---

## Sumário

1. [Tecnologias](#tecnologias)
2. [Como executar](#como-executar)
3. [Como rodar os testes](#como-rodar-os-testes)
4. [Arquitetura](#arquitetura)
5. [Modelo de domínio](#modelo-de-domínio)
6. [Regras de negócio, decisões e premissas](#regras-de-negócio-decisões-e-premissas)
7. [Ambiguidades identificadas e como foram tratadas](#ambiguidades-identificadas-e-como-foram-tratadas)
8. [Cálculo da cobertura](#cálculo-da-cobertura)
9. [Restrições alimentares e alérgenos](#restrições-alimentares-e-alérgenos)
10. [Feature livre: “O que falta?”](#feature-livre-o-que-falta)
11. [API](#api)
12. [Testes](#testes)
13. [Uso de IA](#uso-de-ia)

---

## Tecnologias

| Camada | Tecnologias |
|---|---|
| Backend | Java 21, Spring Boot 3.5 (Web, Data JPA/Hibernate, Validation, Actuator), Flyway, springdoc-openapi |
| Banco | PostgreSQL 16 |
| Frontend | React 18, TypeScript, Vite, React Router |
| Testes | JUnit 5, Mockito, AssertJ, Spring Boot Test + MockMvc, Testcontainers · Vitest, Testing Library |
| Execução | Docker Compose (banco + backend + frontend servido por nginx) |

## Como executar

### Com Docker Compose (recomendado)

Pré-requisito: Docker com Compose.

```bash
docker compose up --build
```

| Serviço | URL |
|---|---|
| Frontend | http://localhost:3000 |
| API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |

Por padrão o backend sobe com o perfil `demo`, que cria dados de exemplo quando o banco está vazio (participantes, um café da manhã aberto com contribuições parciais e um churrasco em rascunho). Para começar com o banco vazio:

```bash
SPRING_PROFILES_ACTIVE= docker compose up --build
```

Para apagar os dados: `docker compose down -v`.

### Em modo de desenvolvimento

```bash
# 1. Banco
docker compose up -d db

# 2. Backend (porta 8080) — o perfil demo é opcional
cd backend
SPRING_PROFILES_ACTIVE=demo ./mvnw spring-boot:run

# 3. Frontend (porta 5173, com proxy de /api para o backend)
cd frontend
npm install
npm run dev
```

Variáveis do backend: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `PORT`, `CORS_ALLOWED_ORIGINS`, `SPRING_PROFILES_ACTIVE`.
Variável do frontend (build): `VITE_API_URL` (padrão `/api`).

## Como rodar os testes

```bash
# Backend — testes unitários (rápidos, sem banco)
cd backend
./mvnw test

# Backend — unitários + integração (sobe um PostgreSQL com Testcontainers; requer Docker)
./mvnw verify

# Frontend
cd frontend
npm test
```

## Arquitetura

```
┌────────────┐      /api       ┌────────────────────────────┐      JPA / SQL      ┌────────────┐
│  Frontend  │ ──────────────▶ │  Backend Spring Boot (REST) │ ──────────────────▶ │ PostgreSQL │
│ React+Vite │   (nginx proxy) │ controller → service → repo │      Flyway         │            │
└────────────┘                 └────────────────────────────┘                      └────────────┘
```

### Backend

Organizado **por funcionalidade** (cada pacote tem entidade, repositório, service, controller e DTOs):

```
br.com.sulwork
├── evento         Evento, StatusEvento (máquina de estados), TipoEvento
├── participante   Participante (CPF único, restrições)
├── inscricao      Inscrição de um participante em um evento
├── item           Categoria (com meta) e Item (quantidade, obrigatoriedade, alérgenos)
├── contribuicao   Contribuição (quem leva quanto de qual item)
├── cobertura      Consulta SQL nativa + CalculadoraCobertura
├── alergeno       Enum Alergeno e cruzamento restrições × alérgenos
├── pendencia      Feature livre "O que falta?"
├── demo           Dados de demonstração (perfil demo)
└── common         Tratamento de erros, validação de CPF, configurações
```

Decisões de arquitetura:

- **Regras no domínio quando dependem só da entidade.** As transições de status e a janela de alterações (`Evento.transicionarPara`, `Evento.garantirJanelaDeAlteracoes`) ficam na entidade e são testadas sem Spring. Regras que dependem de outras entidades ou do banco (limite de quantidade, inscrição obrigatória) ficam nos services.
- **DTOs (records) na borda da API.** Entidades nunca são serializadas diretamente. As respostas trazem campos derivados (`aceitaAlteracoes`, `transicoesPermitidas`, `quantidadeFaltante`) para que o frontend não duplique regras.
- **Relógio injetável (`Clock`).** Toda regra que depende de "agora" usa o mesmo `Clock`. Nos testes de integração ele é substituído por um relógio ajustável, o que permite testar prazos sem `sleep`.
- **Migrations versionadas com Flyway** e `ddl-auto: validate`: o schema é explícito, revisável e conferido pelo Hibernate na subida.
- **Erros padronizados** (`@RestControllerAdvice`): `400` dados inválidos (com a lista de campos), `404` recurso inexistente, `409` conflito com dados existentes (CPF ou inscrição duplicada), `422` violação de regra de negócio (prazo vencido, limite excedido, transição inválida).
- **`open-in-view` desligado**: toda leitura acontece dentro das transações dos services, sem consultas escondidas na serialização.

**Padrões de projeto usados**, onde fizeram sentido:

| Padrão | Onde | Por quê |
|---|---|---|
| Máquina de estados | `StatusEvento` | As transições ficam declaradas em uma tabela única, fácil de ler e de testar. |
| Repository | Spring Data JPA | Isola o acesso a dados. |
| DTO | pacotes `dto` | Contrato da API desacoplado das entidades. |
| Rich domain model | `Evento`, `Participante`, `Item` | As invariantes ficam na entidade, não espalhadas pelos services. |
| Pessimistic locking | `ItemRepository.findByIdAndEventoIdForUpdate` | Garante o limite de quantidade sob concorrência. |

### Frontend

```
src
├── api          cliente REST tipado e tipos da API
├── components   componentes reutilizáveis (cobertura, lista O que falta, formulários...)
├── pages        Eventos, Evento (com abas) e Participantes
└── utils        CPF (máscara/validação) e formatação
```

A página do evento carrega todos os dados em paralelo e recarrega depois de cada alteração. As regras ficam todas no backend: o frontend só usa os campos derivados para habilitar ou desabilitar ações e mostra a mensagem de erro devolvida pela API.

## Modelo de domínio

```
Evento 1───* Categoria 1───* Item 1───* Contribuicao *───1 Participante
   │                                                          │
   └──────────────────* Inscricao *───────────────────────────┘
```

| Entidade | Campos | Restrições |
|---|---|---|
| Evento | nome, descrição, tipo, data/hora, prazo para alterações, status | prazo ≤ data do evento |
| Participante | nome, CPF, restrições (alérgenos), observações alimentares | CPF válido e único |
| Inscrição | evento, participante, data | única por (evento, participante) |
| Categoria | evento, nome, meta (opcional) | nome único no evento; meta > 0 |
| Item | evento, categoria, nome, quantidade necessária, unidade, obrigatório, alérgenos | quantidade > 0 |
| Contribuição | item, participante, quantidade | quantidade > 0; única por (item, participante); soma ≤ necessária |

## Regras de negócio, decisões e premissas

### Status e transições do evento

```
           ┌──────────────► CANCELADO ◄──────────────┐
           │                    ▲                    │
     RASCUNHO ──abrir──► ABERTO ──encerrar──► ENCERRADO ──realizar──► REALIZADO
                            ▲                    │
                            └──────reabrir───────┘
```

| De | Para | Condições adicionais |
|---|---|---|
| RASCUNHO | ABERTO | ao menos um item cadastrado; prazo ainda não vencido |
| RASCUNHO | CANCELADO | — |
| ABERTO | ENCERRADO | — |
| ABERTO | CANCELADO | — |
| ENCERRADO | ABERTO (reabrir) | prazo ainda não vencido |
| ENCERRADO | REALIZADO | a data e hora do evento já chegaram |
| ENCERRADO | CANCELADO | — |
| REALIZADO, CANCELADO | — | estados finais |

O que cada status permite:

| Status | Editar evento, categorias e itens | Inscrições e contribuições | Excluir evento |
|---|---|---|---|
| RASCUNHO | sim | não | sim |
| ABERTO | sim | sim, **enquanto o prazo não vence** | não (cancelar) |
| ENCERRADO | não | não | não |
| REALIZADO | não | não | não |
| CANCELADO | não | não | não |

### Prazo × status (regra central)

> **Participantes só alteram inscrições e contribuições quando o evento está `ABERTO` e o prazo para alterações ainda não venceu.** As duas condições são necessárias e independentes.

- **O prazo vale sozinho, sem job agendado.** Ele é verificado em cada requisição usando o `Clock` da aplicação. Assim não existe janela em que o prazo venceu mas um job ainda não rodou, e o comportamento é determinístico nos testes.
- **O status não muda automaticamente quando o prazo vence.** O status representa uma decisão do organizador; o prazo é uma trava automática. Um evento `ABERTO` com prazo vencido é exibido como "prazo encerrado", e o organizador pode **estender o prazo** (editando o evento) ou **encerrá-lo**.
- **Encerrar antes do prazo é permitido**: o organizador pode fechar a lista mais cedo, e as alterações param na hora, mesmo dentro do prazo.
- **Reabrir só é permitido com prazo vigente**: não faz sentido reabrir um evento em que ninguém mais pode contribuir.
- O prazo não pode ser posterior à data do evento (validado na API e com `CHECK` no banco), e a data do evento precisa estar no futuro ao criar ou editar.
- **O organizador pode ajustar categorias e itens com o evento `RASCUNHO` ou `ABERTO`, mesmo após o prazo.** O prazo protege os compromissos dos participantes, não o planejamento. Mesmo assim, há proteções: a quantidade necessária de um item não pode ficar abaixo do que já foi contribuído, e um item com contribuições não pode ser excluído.

### Participantes e inscrições

- O participante é um **colaborador global**: é cadastrado uma vez (CPF único) e pode se inscrever em vários eventos. A inscrição é única por evento, garantida no service (mensagem clara) e por `UNIQUE` no banco (proteção contra corrida).
- O CPF é aceito com ou sem máscara, armazenado só com dígitos e validado pelos dígitos verificadores. Sequências repetidas (`111.111.111-11`) são rejeitadas. **O CPF não pode ser alterado**, pois identifica o colaborador.
- Para contribuir, é preciso estar inscrito no evento. Cancelar a inscrição (dentro da janela) **remove junto as contribuições** da pessoa naquele evento, porque quem não vai não leva nada.
- Participantes inscritos em algum evento não podem ser excluídos (preserva o histórico).

### Contribuições

- Uma contribuição por participante e item. Para mudar a quantidade, a contribuição existente é alterada (ou, pela feature "eu levo", somada).
- **A soma das contribuições de um item nunca ultrapassa a quantidade necessária.** A resposta de erro informa quanto ainda resta (ex.: _"restam 4 de 10"_).
- **Concorrência:** duas pessoas contribuindo ao mesmo tempo poderiam ler a mesma soma e, juntas, estourar o limite. Por isso o service trava a linha do item (`SELECT ... FOR UPDATE`) antes de somar, o que serializa as contribuições **no mesmo item** sem bloquear o resto do evento. Há um teste de integração que dispara 4 contribuições simultâneas de 4 unidades num item de 10 e verifica que exatamente 2 são aceitas. Esse teste **falha quando o lock é removido** (verificado durante o desenvolvimento).

### Premissas

- **Sem autenticação.** O desafio não pede controle de acesso. Na interface, o seletor **"Você é"** no topo define o participante atual, usado na ação "Eu levo". Qualquer pessoa pode agir como organizador. Em produção, isso seria substituído por login (ex.: SSO corporativo), com papéis de organizador e participante.
- **Quantidades são inteiras**, com unidade opcional em texto (ex.: "litros", "pacotes"). Frações (1,5 kg) podem ser representadas trocando a unidade (1500 g).
- **Datas são horário local de São Paulo** (`LocalDateTime`), já que todos os eventos são presenciais na empresa.

## Ambiguidades identificadas e como foram tratadas

| # | Ambiguidade | Decisão |
|---|---|---|
| 1 | Quais as transições entre status? | Máquina de estados explícita (tabela acima), com guardas de tempo e "ao menos um item" para abrir. |
| 2 | Como prazo e status interagem? | As duas condições são exigidas (ABERTO **e** prazo vigente); o prazo não muda o status automaticamente. |
| 3 | O organizador também fica sujeito ao prazo? | Não: ele pode ajustar itens enquanto o evento está em organização (RASCUNHO/ABERTO), com proteções para não invalidar contribuições já feitas. |
| 4 | Participante é por evento ou global? | Global (CPF único), com inscrição por evento. Atende "vários eventos, mas não mais de uma vez no mesmo". |
| 5 | Pode haver mais de uma contribuição da mesma pessoa no mesmo item? | Não; ela altera a quantidade da que já existe. Isso simplifica a lista "quem leva o quê". |
| 6 | O que é a "meta por categoria"? | A quantidade mínima de unidades que a categoria deve reunir (ex.: Bebidas: 10). É opcional. Se ela for maior que a soma das quantidades dos itens, nunca será atingida, e a interface sinaliza isso. |
| 7 | O que acontece com as contribuições de quem cancela a inscrição? | São removidas junto. |
| 8 | Restrições alimentares: texto livre ou lista? | Lista fechada de alérgenos (permite cruzar com os itens) **mais** um campo livre para restrições que não são alérgenos (vegetariano etc.). |
| 9 | Um conflito de alérgeno deve bloquear a contribuição? | Não. É um alerta: o item ainda serve aos demais, e o objetivo é incentivar alternativas. |
| 10 | Como medir a cobertura? | Fórmula explicada na próxima seção. |
| 11 | Excluir ou cancelar eventos? | Só rascunhos podem ser excluídos; eventos publicados são cancelados (mantém o histórico). |

## Cálculo da cobertura

`GET /api/eventos/{id}/cobertura`. Os dados vêm de **uma consulta SQL nativa** (`CoberturaRepository`) que agrega, por categoria e item, o total contribuído (`LEFT JOIN` + `SUM` + `GROUP BY`). O cálculo é feito na classe pura `CalculadoraCobertura`, testada isoladamente.

1. **Item:** `min(contribuído / necessário, 1)`.
   Usar a **razão** em vez de unidades absolutas evita distorções: num evento com 50 guardanapos entregues e 0 de 2 bolos, somar unidades daria 96%; pela razão, dá 50%, que é a situação real.
2. **Categoria sem meta:** média ponderada dos itens, com **obrigatório peso 2 e opcional peso 1**.
3. **Categoria com meta:** `min(total contribuído na categoria / meta, 1)`. A meta é a definição explícita do organizador de "suficiente" para aquela categoria, então ela prevalece sobre a média dos itens.
4. **Categoria sem itens e sem meta:** ignorada no cálculo.
5. **Geral:** média simples das categorias consideradas. Cada categoria é uma área de necessidade do evento (bebidas, salgados...) e pesa igual, independentemente de quantos itens tem.
6. **Pronto para acontecer:** todos os itens obrigatórios completos **e** todas as metas atingidas. É um indicador separado do percentual: um evento com 95% sem o item obrigatório principal **não** está pronto, e um evento sem nenhum opcional pode estar pronto com menos de 100%.

Os percentuais são **arredondados para baixo**: o evento só mostra 100% quando está de fato completo (999/1000 aparece como 99%).

A resposta traz o percentual geral, `obrigatoriosCompletos/obrigatoriosTotal`, `metasAtingidas/metasTotal`, `pronto` e o detalhamento por categoria (critério usado, totais, `metaAtingivel`) e por item.

## Restrições alimentares e alérgenos

- Participantes e itens usam o **mesmo vocabulário** (`Alergeno`: glúten, lactose, ovo, amendoim, castanhas, soja, peixe, crustáceos, gergelim), o que torna o cruzamento confiável. Restrições fora da lista ficam em "observações" (texto livre) e aparecem como informação.
- `GET /api/eventos/{id}/restricoes` consolida:
  - as restrições **dos inscritos** (não de todos os cadastrados), ordenadas pelas mais frequentes, com os nomes;
  - para cada item, **quais inscritos são afetados por cada alérgeno** e se o item é **seguro para todos**.
- Efeitos na experiência:
  - na aba **Restrições e alérgenos**: resumo das restrições, itens que afetam alguém (com a dica de levar alternativas) e itens seguros para todos;
  - na lista **O que falta?**: cada item mostra o alerta (ex.: _"⚠ Lactose: restrição de Ana"_) **no momento em que alguém decide o que levar**;
  - nos itens e participantes: chips com os alérgenos e as restrições.
- O cruzamento é **informativo e não bloqueia** contribuições (ver ambiguidade 9).

## Feature livre: “O que falta?”

**Problema.** Numa planilha ou lista de itens, quem quer ajudar precisa varrer tudo para descobrir o que ainda falta, o que é mais importante e se aquilo serve para todo mundo. O resultado comum é excesso de opcionais e falta de itens essenciais.

**Como funciona.**

- `GET /api/eventos/{id}/pendencias` lista **apenas os itens incompletos**, já priorizados:
  - **ALTA:** item obrigatório (o evento não fica pronto sem ele);
  - **MÉDIA:** item opcional de uma categoria cuja **meta ainda não foi atingida**;
  - **BAIXA:** demais opcionais;
  - dentro da mesma prioridade, **o menos coberto primeiro**, depois o que tem maior quantidade faltante.
- Cada item informa quanto falta, os alérgenos, **quais inscritos seriam afetados** e, se `participanteId` for informado, quanto aquela pessoa já leva.
- `POST /api/eventos/{id}/pendencias/{itemId}/assumir` é o **"Eu levo"**: sem quantidade, a pessoa assume **tudo o que falta**; com quantidade, só aquela parte. Se ela já contribui com o item, a quantidade é **somada** à contribuição existente em vez de gerar conflito.
- No frontend, é a aba padrão de eventos abertos, com contador de pendências. O botão fica desabilitado, **com o motivo**, quando o evento não aceita contribuições, quando ninguém foi selecionado em "Você é" ou quando a pessoa não está inscrita.

**Decisões.**

- A prioridade e os percentuais **reaproveitam o cálculo de cobertura e o cruzamento de alérgenos**, então a lista mostra exatamente os mesmos números do resto da aplicação. Não há uma segunda fórmula para manter.
- O "eu levo" passa pelas **mesmas regras e pelo mesmo lock** das contribuições (janela, inscrição, limite), sem caminho alternativo que possa burlá-las.
- Somar à contribuição existente, em vez de retornar 409, porque nesse fluxo a intenção do usuário é clara ("quero levar mais disso").

## API

Documentação interativa completa no **Swagger UI** (`/swagger-ui.html`). Resumo:

| Método | Rota | Descrição |
|---|---|---|
| GET/POST | `/api/eventos` | Lista (filtro `?status=`) / cria (nasce em RASCUNHO) |
| GET/PUT/DELETE | `/api/eventos/{id}` | Detalha / edita / exclui (só rascunho) |
| PATCH | `/api/eventos/{id}/status` | Transição de status |
| GET/POST/PUT/DELETE | `/api/participantes[/{id}]` | CRUD de participantes (busca `?busca=` por nome ou CPF) |
| GET/POST | `/api/eventos/{id}/inscricoes` | Lista / inscreve participante |
| DELETE | `/api/eventos/{id}/inscricoes/{participanteId}` | Cancela inscrição (remove contribuições da pessoa) |
| GET/POST/PUT/DELETE | `/api/eventos/{id}/categorias[/{categoriaId}]` | Categorias com meta opcional |
| GET/POST/PUT/DELETE | `/api/eventos/{id}/itens[/{itemId}]` | Itens (com quantidade contribuída e faltante) |
| GET/POST/PUT/DELETE | `/api/eventos/{id}/contribuicoes[/{contribuicaoId}]` | Contribuições |
| GET | `/api/eventos/{id}/cobertura` | Cobertura (consulta SQL nativa) |
| GET | `/api/eventos/{id}/restricoes` | Restrições × alérgenos |
| GET | `/api/eventos/{id}/pendencias` | Feature "O que falta?" (`?participanteId=` opcional) |
| POST | `/api/eventos/{id}/pendencias/{itemId}/assumir` | "Eu levo" |
| GET | `/api/alergenos` | Vocabulário de alérgenos |

Exemplo de erro de regra de negócio (`422`):

```json
{
  "timestamp": "2026-03-10T09:00:00-03:00",
  "status": 422,
  "erro": "Unprocessable Entity",
  "mensagem": "Quantidade excede o necessário para \"Suco\": restam 4 de 10."
}
```

## Testes

**Backend: 100 testes** (`./mvnw verify`).

| Tipo | Classes | O que cobrem |
|---|---|---|
| Unitários (JUnit 5) | `StatusEventoTest`, `EventoTest` | Tabela de transições, guardas de tempo, janela de alterações |
| | `CpfTest` | Dígitos verificadores, máscara, sequências repetidas |
| | `ContribuicaoServiceTest` (Mockito) | Limite, inscrição obrigatória, duplicidade, prazo, status, alteração de quantidade |
| | `CalculadoraCoberturaTest` | Pesos, razão × unidades, metas, metas inatingíveis, arredondamento, "pronto" |
| | `AnalisadorRestricoesTest` | Agrupamento de restrições, conflitos por item, observações |
| Integração (Spring Boot + Testcontainers + MockMvc) | `EventoIT`, `ParticipanteIT`, `ItemIT`, `ContribuicaoIT`, `CoberturaIT`, `RestricoesIT`, `PendenciaIT` | Fluxos completos via HTTP contra PostgreSQL real, incluindo a consulta nativa, os códigos de erro, o avanço do relógio para testar prazos e a **concorrência no limite do item** |

**Frontend: 26 testes** (`npm test`, Vitest + Testing Library): lista "O que falta?" (ordem, bloqueios com motivo, "eu levo" total e parcial), resumo de cobertura, formulário de participante (máscara, CPF inválido, payload enviado, erro da API) e cliente da API (erro padronizado, 204, API fora do ar).

## Uso de IA

**Ferramenta:** Claude Code (assistente de código da Anthropic), usado durante todo o desenvolvimento. Sou responsável pelo código entregue e pelas decisões documentadas aqui.

**Em quais etapas ajudou:**

- **Leitura do enunciado e levantamento das ambiguidades:** a lista de pontos abertos (status, prazo × status, meta, cobertura, alérgenos) foi construída em conjunto e depois transformada nas decisões acima.
- **Implementação:** geração da maior parte do código do backend e do frontend, seguindo a ordem de commits do histórico (cada funcionalidade com seus testes).
- **Testes:** escrita dos cenários de teste e execução local da suíte, incluindo os testes de integração com Testcontainers.
- **Verificação da interface:** navegação automatizada (Playwright) pelo fluxo principal para conferir o comportamento real e o layout no celular.
- **Documentação:** rascunho deste README.

**Uma sugestão aceita:** travar o item com lock pessimista (`SELECT ... FOR UPDATE`) antes de validar a soma das contribuições. Aceitei porque a regra "a soma não pode ultrapassar a quantidade necessária" é exatamente o tipo de validação que quebra sob concorrência. Para não aceitar só na teoria, o teste de concorrência foi executado **com e sem o lock**: sem ele, mais contribuições do que o permitido eram aceitas; com ele, exatamente 2 de 4.

**Sugestões alteradas ou rejeitadas:**

- **Rejeitada: mudar o status para `ENCERRADO` automaticamente com um job agendado (`@Scheduled`) quando o prazo vence.** Preferi verificar o prazo a cada requisição: não existe janela entre o vencimento e a execução do job, o status continua sendo uma decisão do organizador, e os testes ficam determinísticos com o relógio injetado.
- **Alterada: a fórmula de cobertura.** A primeira proposta combinava, dentro da mesma categoria, a média dos itens com a meta. Na prática isso gerava casos difíceis de explicar (meta atingida, mas categoria abaixo de 100%). Mudei para: **com meta, a meta define a categoria; sem meta, vale a média ponderada dos itens**, e os obrigatórios continuam cobrados à parte no indicador "pronto".
- **Rejeitada: aplicar o padrão Strategy à cobertura**, com várias estratégias intercambiáveis. Existe uma única fórmula, e uma classe pura e bem testada é mais simples de entender e manter. Abstração sem uma segunda implementação real seria só indireção.
- **Ajuste encontrado pelos testes:** o teste do formulário de participante mostrou que a mensagem "CPF inválido" estava dentro do `<label>`, o que mudava o nome acessível do campo para leitores de tela. A mensagem foi movida para fora do label e ligada ao campo por `aria-describedby`.

**Onde foi necessário maior julgamento próprio:**

- **Interpretação das regras abertas**: o que é a meta de uma categoria, se o organizador fica sujeito ao prazo e se conflitos de alérgenos devem bloquear contribuições. São decisões de produto, não de código, e foram pensadas a partir de como um evento de café da manhã realmente acontece.
- **A fórmula de cobertura**, em especial usar razão em vez de unidades e separar "percentual" de "pronto".
- **O que deixar de fora** (autenticação, notificações) para manter o escopo coerente com o desafio, registrando essas premissas.
