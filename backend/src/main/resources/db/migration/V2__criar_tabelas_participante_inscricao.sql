CREATE TABLE participante (
    id                         BIGSERIAL PRIMARY KEY,
    nome                       VARCHAR(120) NOT NULL,
    cpf                        VARCHAR(11)  NOT NULL,
    observacoes_alimentares    VARCHAR(500),
    CONSTRAINT uk_participante_cpf UNIQUE (cpf)
);

CREATE TABLE participante_restricao (
    participante_id  BIGINT      NOT NULL REFERENCES participante (id) ON DELETE CASCADE,
    alergeno         VARCHAR(30) NOT NULL,
    PRIMARY KEY (participante_id, alergeno)
);

CREATE TABLE inscricao (
    id               BIGSERIAL PRIMARY KEY,
    evento_id        BIGINT    NOT NULL REFERENCES evento (id) ON DELETE CASCADE,
    participante_id  BIGINT    NOT NULL REFERENCES participante (id),
    inscrito_em      TIMESTAMP NOT NULL,
    CONSTRAINT uk_inscricao_evento_participante UNIQUE (evento_id, participante_id)
);

CREATE INDEX idx_inscricao_participante ON inscricao (participante_id);
