# Como o desafio Sulwork Eventos foi feito, tudinho

Explicação completa e em ordem, para você contar na entrevista. Escrita a partir do README, do código e do histórico do Git.

> **Atenção:** o histórico que dá para ver cobre só os 51 commits mais recentes (de 27/10 em diante). O começo do backend não aparece nele. Essa fase está descrita aqui com base no README e no código.

---

## 1. O ponto de partida: entender o problema

O desafio pedia um sistema para organizar eventos de alimentação da empresa. Antes de programar, foram levantadas as **dúvidas do enunciado** e cada uma virou uma decisão registrada no README (são 12):

| Dúvida | Decisão |
|---|---|
| Como prazo e status se relacionam? | Participante só altera se o evento está `ABERTO` **e** o prazo não venceu |
| O que é a "meta" da categoria? | Quantidade mínima de unidades que a categoria deve reunir |
| Alérgeno deve bloquear a contribuição? | Não, só avisa |
| O organizador também fica sujeito ao prazo? | Não |
| Participante é por evento ou global? | Global (CPF único), com inscrição por evento |
| Precisa de login? | Não era pedido; registrado como premissa |

**Frase para a entrevista:** "Antes de programar, listei as dúvidas do enunciado e decidi cada uma, registrando no README o motivo."

---

## 2. As tecnologias e por que

| Camada | Escolha | Motivo (simples) |
|---|---|---|
| Backend | Java 21 + Spring Boot 3.5 | Padrão de mercado, traz web, banco e validação prontos |
| Banco | PostgreSQL 16 | Banco relacional robusto, com suporte a lock de linha |
| Migrations | Flyway | O schema fica versionado em arquivos SQL (5 migrations) |
| Frontend | React 18 + TypeScript + Vite | Telas interativas, com tipos para evitar erro |
| Testes | JUnit 5, Mockito, Testcontainers · Vitest | Unitário, integração com banco real e frontend |
| Execução | Docker Compose | Um comando sobe banco, API e site |

---

## 3. O banco de dados

As tabelas nasceram por migrations, em ordem:

1. `V1` evento
2. `V2` participante e inscrição
3. `V3` categoria e item
4. `V4` contribuição
5. `V5` descrição do evento passa a ser obrigatória

Regras garantidas **no próprio banco** (além do código):
- `UNIQUE (item_id, participante_id)` na contribuição.
- `CHECK (quantidade > 0)`.
- Chaves estrangeiras entre as tabelas.
- Prazo ≤ data do evento (`CHECK`).

**Por que duplicar no banco?** O código valida para dar uma mensagem boa ao usuário. O banco valida para garantir a integridade mesmo se duas requisições passarem juntas. As duas camadas se complementam.

---

## 4. O backend, camada por camada

### 4.1 Organização
Por **funcionalidade**: `evento`, `participante`, `inscricao`, `item`, `contribuicao`, `cobertura`, `alergeno`, `pendencia`, `dashboard`, `demo` e `common`. Dentro de cada um: entidade, repositório, service, controller e `dto`.

### 4.2 O caminho de uma requisição (exemplo: `PATCH /api/eventos/{id}/status`)

```
Requisição HTTP
   │
   ▼
EventoController        recebe, valida com @Valid, chama o service
   │
   ▼
EventoService           busca o evento, confere "ao menos um item" para abrir
   │
   ▼
Evento.transicionarPara regra de negócio dentro da entidade
   │
   ▼
StatusEvento            tabela de transições permitidas
   │
   ▼
EventoRepository / JPA  grava no PostgreSQL
   │
   ▼
EventoResponse (DTO)    resposta com campos derivados (aceitaAlteracoes...)
```

- **Controller:** só recebe e devolve. Não tem regra.
- **Service:** coordena. Usa `@Transactional` para o conjunto ser tudo ou nada.
- **Entidade:** guarda as regras que dependem só dela (`transicionarPara`, `garantirJanelaDeAlteracoes`).
- **Repository:** acesso ao banco com Spring Data JPA.
- **DTO:** o formato que sai e entra na API.

### 4.3 O evento e o seu ciclo de vida
Classes: `Evento`, `StatusEvento`, `EventoService`.
- Nasce em `RASCUNHO`.
- Transições numa tabela (`EnumMap`) dentro do enum.
- Guardas de tempo: abrir/reabrir exige prazo vigente; realizar exige que a data tenha chegado; abrir exige ao menos um item.
- Só rascunho pode ser excluído; os demais são cancelados.

### 4.4 Participantes e CPF
- CPF aceito com ou sem máscara, guardado só com dígitos, validado pelos dígitos verificadores (`CpfValidoValidator`).
- Rejeita sequências repetidas como `111.111.111-11`.
- A API devolve só `cpfMascarado`.
- A busca por CPF só aceita o número completo.

### 4.5 Inscrições (`InscricaoService`)
- Inscrever exige a janela aberta (evento `ABERTO` e prazo vigente).
- Inscrição duplicada devolve `409` com mensagem clara; o `UNIQUE` do banco protege contra corrida.
- Cancelar inscrição remove também as contribuições da pessoa naquele evento.

### 4.6 Categorias e itens (`ItemService`)
- Só mexe quando o evento está em organização (`RASCUNHO` ou `ABERTO`).
- Não deixa baixar a quantidade abaixo do que já foi contribuído.
- Não deixa excluir item que já tem contribuição.
- Cada item pode ter alérgenos (lista fechada).

### 4.7 Contribuições e o lock (`ContribuicaoService`)
O fluxo de `criar`, em ordem:
1. Confere a janela de alterações.
2. Confere se a pessoa está inscrita.
3. **Trava a linha do item** (`SELECT ... FOR UPDATE`).
4. Confere se a pessoa já contribui com esse item.
5. Confere se a soma não passa do necessário (`garantirLimite`).
6. Grava.

O passo 3 é o que impede duas pessoas, ao mesmo tempo, de ultrapassarem o limite.

### 4.8 Cobertura (`CalculadoraCobertura` + `CoberturaRepository`)
Uma consulta SQL nativa (`LEFT JOIN` + `SUM` + `GROUP BY`) traz os totais. Depois a calculadora aplica:
- Item: `min(contribuído/necessário, 1)`.
- Categoria sem meta: média ponderada (obrigatório peso 2, opcional peso 1).
- Categoria com meta: `min(total/meta, 1)`.
- Geral: média das categorias.
- "Pronto": obrigatórios completos **e** metas atingidas.
- Arredondamento para baixo.

### 4.9 "O que falta?" (`PendenciaService`)
- Reaproveita a cobertura e o cruzamento de alérgenos.
- Ordena por prioridade (ALTA → MÉDIA → BAIXA), depois pelo menos coberto, depois pela maior quantidade faltante.
- O "Eu levo" (`assumir`) passa pelas **mesmas regras e pelo mesmo lock**.

### 4.10 Alérgenos
Participantes e itens usam a **mesma lista fechada** (glúten, lactose, ovo, amendoim, castanhas, soja, peixe, crustáceos, gergelim). `GET /eventos/{id}/restricoes` cruza as restrições dos **inscritos** com os itens e diz o que é seguro para todos.

### 4.11 Dashboard
`GET /api/dashboard` junta indicadores reais: próximos eventos, contribuições registradas, itens obrigatórios sem dono e cobertura média dos eventos abertos. Sem eventos abertos, mostra "—", e não 0%.

### 4.12 Erros padronizados (`GlobalExceptionHandler`)

| Código | Quando | Exceção |
|---|---|---|
| 400 | Dados inválidos ou requisição mal formada | `MethodArgumentNotValidException` |
| 404 | Não encontrado | `RecursoNaoEncontradoException` |
| 409 | Conflito (CPF/inscrição duplicados, item com contribuições) | `ConflitoException` |
| 422 | Regra de negócio violada | `RegraNegocioException` |

Uma última barreira trata violações de constraint do banco (`DataIntegrityViolationException`) como `409`. Nenhuma resposta traz stack trace.

### 4.13 Relógio injetável (`Clock`)
Toda regra com "agora" usa o mesmo `Clock`. Nos testes, ele é trocado por um `RelogioDeTeste` ajustável.

### 4.14 Configurações importantes (`application.yml`)
- `ddl-auto: validate` (o Hibernate só confere o schema).
- `open-in-view: false`.
- `default_batch_fetch_size: 100` (resolve o N+1).
- Fuso `America/Sao_Paulo`.
- Actuator expondo só `health`.
- CORS vindo de variável de ambiente.

---

## 5. O frontend

- **React + TypeScript + Vite + React Router.**
- Pastas: `api` (cliente REST tipado), `components`, `pages`, `utils`.
- **Páginas:** Visão geral, Eventos, Evento (com abas: Itens, Participantes, Contribuições, Restrições, "O que falta?"), Minhas contribuições, Participantes.
- **`api/client.ts`:** uma função `request` central. Se a API estiver fora do ar, mostra uma mensagem explicando que o servidor gratuito pode levar até 1 minuto para acordar. Erros da API chegam num formato padrão (`ApiError`).
- **Seletor "Você é":** como não há login, ele identifica o participante atual. Guarda só o id no navegador.
- **Sem regra duplicada:** o frontend usa os campos derivados (`aceitaAlteracoes`, `transicoesPermitidas`) para habilitar ou desabilitar botões, e mostra a mensagem de erro da API.
- **Visual:** paleta azul Sulwork e fonte Inter. Capas ilustrativas dos eventos. Menu lateral no computador e menu inferior no celular. Contraste ajustado para passar o mínimo da WCAG AA.

---

## 6. Os testes

| Tipo | Exemplos | O que garantem |
|---|---|---|
| Unitário | `StatusEventoTest`, `EventoTest`, `CpfTest`, `CalculadoraCoberturaTest`, `AnalisadorRestricoesTest` | Regras puras, rápidas, sem banco |
| Unitário com Mockito | `ContribuicaoServiceTest` | Regras do service isoladas |
| Integração (`*IT`) | `EventoIT`, `ContribuicaoIT`, `CoberturaIT`, `PendenciaIT`, `DashboardIT`... | Fluxos completos por HTTP contra PostgreSQL real |
| Frontend (Vitest) | Lista "O que falta?", formulários, modal, cliente da API | Telas e regras de exibição |

A base dos testes de integração (`IntegrationTest`) sobe o Spring Boot com um **PostgreSQL de verdade via Testcontainers**, limpa o banco antes de cada teste e usa o relógio de teste.

O teste mais importante: **4 contribuições simultâneas de 4 unidades num item de 10, só 2 passam**. Foi rodado com e sem o lock para provar que ele protege.

---

## 7. A publicação

| Parte | Onde | Detalhe |
|---|---|---|
| Frontend | Vercel | Deploy a cada push na `main`; `vercel.json` com cabeçalhos de segurança |
| Backend | Render (Docker) | `render.yaml`; health check em `/actuator/health`; senhas no painel (`sync: false`) |
| Banco | Neon (PostgreSQL) | Schema criado pelo Flyway na subida |

**Dockerfile do backend:** build em duas etapas (Maven compila, depois só o JRE roda). Usa um usuário sem privilégios e flags de JVM para o plano gratuito: `-XX:MaxRAMPercentage=75`, `-XX:TieredStopAtLevel=1` e `-XX:+UseSerialGC`.

**Docker Compose (local):** sobe `db`, `backend` e `frontend` (nginx), com health check em cada um e o backend esperando o banco ficar saudável.

**Perfil `demo`:** popula dados de exemplo quando o banco está vazio.

---

## 8. O problema em produção (a história para contar)

1. Depois do deploy, cada ação demorava cerca de **10 segundos**.
2. Os testes passavam; localmente tudo era rápido.
3. Medi as consultas da página do evento: **71 por ação** (N+1).
4. Configurei o carregamento em lote (`default_batch_fetch_size`): caiu para **30**.
5. Ajustei a JVM para o servidor gratuito (0,1 CPU).
6. Lição: observar o comportamento em produção, e não só os testes.

---

## 9. Proteção de dados e segurança

- **LGPD:** restrição alimentar é dado sensível de saúde. Usada só para segurança alimentar. CPF mascarado em todas as respostas.
- **Busca por CPF:** só o número completo, para não permitir descobrir CPFs por pedaços.
- **Entrada:** Bean Validation com tamanhos máximos e teto de 100.000 em quantidades.
- **Banco:** consultas parametrizadas (sem concatenar texto do usuário).
- **Site:** CSP, `X-Frame-Options: DENY`, `nosniff`, Referrer-Policy e Permissions-Policy (Vercel e nginx).
- **Segredos:** senha do banco só no painel do Render; `.env` ignorado pelo Git.
- **Limitação conhecida:** sem autenticação. Em produção: login corporativo com papéis, limite de requisições e auditoria.

---

## 10. A linha do tempo (do histórico do Git)

| Data | O que aconteceu |
|---|---|
| 27/10 | Publicação, correção do N+1, ajuste da JVM, dashboard, layout com menu lateral, lista de eventos com busca e filtros |
| 28/10 | Descrição do evento obrigatória, selo "Aberto · prazo encerrado", tempo restante até o prazo, tela "Minhas contribuições", visual inspirado no Stitch, visão geral, CPF mascarado, documentação de LGPD |
| 29/10 | Paleta azul Sulwork + fonte Inter, capas ilustrativas, painel lateral do item, busca por CPF só completo, teto de 100.000, cabeçalhos de segurança, `.env` ignorado, documentação da revisão de segurança |

---

## 11. Como falar do uso de IA (com honestidade)

- **Ferramenta:** Claude Code, usado em todo o desenvolvimento.
- **Onde ajudou:** levantar as dúvidas do enunciado, gerar boa parte do código, escrever testes, verificar a interface com Playwright, configurar o deploy, diagnosticar o problema de produção, revisar a segurança e rascunhar a documentação.
- **Suas decisões e correções:**
  - Rejeitou um job agendado para encerrar eventos (prefiriu checar o prazo por requisição).
  - Rejeitou um padrão Strategy desnecessário na cobertura.
  - Simplificou a fórmula de cobertura.
  - Cortou do protótipo visual o que não existia no domínio (login, notificações, valores em R$).
  - Notou a lentidão em produção e direcionou a investigação.
  - Aceitou o lock pessimista e **validou rodando o teste com e sem ele**.
- **O que dizer:** "A IA gerou boa parte do código. Eu defini as regras, revisei, testei e decidi o que entrava."

---

## 12. Resumo em 30 segundos

> "Comecei levantando as dúvidas do enunciado e decidindo as regras. Fiz o backend em Java com Spring Boot e PostgreSQL, com a regra de negócio dentro das entidades e testes unitários e de integração com banco real. Tratei concorrência travando o item para ninguém passar do limite. Depois fiz o frontend em React, publiquei na Vercel, no Render e no Neon, corrigi uma lentidão que só apareceu em produção (de 71 para 30 consultas) e fechei com uma revisão de segurança e de proteção de dados."
