CREATE TABLE IF NOT EXISTS gestopago_tokens (
    id                  SERIAL PRIMARY KEY,
    id_distribuidor     INTEGER         NOT NULL,
    codigo_dispositivo  VARCHAR(100)    NOT NULL,
    token               TEXT            NOT NULL,
    token_type          VARCHAR(50),
    expires_in          BIGINT,
    fecha_creacion      DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_actualizacion DATE NOT NULL DEFAULT CURRENT_DATE,
    activo              BOOLEAN         NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_gestopago_tokens UNIQUE (id_distribuidor, codigo_dispositivo)
);
