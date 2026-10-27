CREATE TABLE evento (
    id                BIGSERIAL PRIMARY KEY,
    nome              VARCHAR(120)  NOT NULL,
    descricao         VARCHAR(1000),
    tipo              VARCHAR(30)   NOT NULL,
    data_hora         TIMESTAMP     NOT NULL,
    prazo_alteracoes  TIMESTAMP     NOT NULL,
    status            VARCHAR(20)   NOT NULL,
    criado_em         TIMESTAMP     NOT NULL,
    CONSTRAINT ck_evento_prazo CHECK (prazo_alteracoes <= data_hora)
);

CREATE INDEX idx_evento_status ON evento (status);
CREATE INDEX idx_evento_data_hora ON evento (data_hora);
