CREATE TABLE contribuicao (
    id               BIGSERIAL PRIMARY KEY,
    item_id          BIGINT    NOT NULL REFERENCES item (id),
    participante_id  BIGINT    NOT NULL REFERENCES participante (id),
    quantidade       INTEGER   NOT NULL,
    criado_em        TIMESTAMP NOT NULL,
    atualizado_em    TIMESTAMP NOT NULL,
    CONSTRAINT ck_contribuicao_quantidade CHECK (quantidade > 0),
    CONSTRAINT uk_contribuicao_item_participante UNIQUE (item_id, participante_id)
);

CREATE INDEX idx_contribuicao_participante ON contribuicao (participante_id);
