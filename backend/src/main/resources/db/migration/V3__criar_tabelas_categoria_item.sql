CREATE TABLE categoria (
    id         BIGSERIAL PRIMARY KEY,
    evento_id  BIGINT       NOT NULL REFERENCES evento (id) ON DELETE CASCADE,
    nome       VARCHAR(60)  NOT NULL,
    meta       INTEGER,
    CONSTRAINT ck_categoria_meta CHECK (meta IS NULL OR meta > 0)
);

CREATE UNIQUE INDEX uk_categoria_evento_nome ON categoria (evento_id, lower(nome));

CREATE TABLE item (
    id                     BIGSERIAL PRIMARY KEY,
    evento_id              BIGINT       NOT NULL REFERENCES evento (id) ON DELETE CASCADE,
    categoria_id           BIGINT       NOT NULL REFERENCES categoria (id),
    nome                   VARCHAR(80)  NOT NULL,
    quantidade_necessaria  INTEGER      NOT NULL,
    unidade                VARCHAR(20),
    obrigatorio            BOOLEAN      NOT NULL,
    CONSTRAINT ck_item_quantidade CHECK (quantidade_necessaria > 0)
);

CREATE INDEX idx_item_evento ON item (evento_id);
CREATE INDEX idx_item_categoria ON item (categoria_id);

CREATE TABLE item_alergeno (
    item_id   BIGINT      NOT NULL REFERENCES item (id) ON DELETE CASCADE,
    alergeno  VARCHAR(30) NOT NULL,
    PRIMARY KEY (item_id, alergeno)
);
