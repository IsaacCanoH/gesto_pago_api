CREATE TABLE gestopago_usuarios (
    id                  SERIAL PRIMARY KEY,
    cliente_id          INTEGER NOT NULL,
    correo              VARCHAR(100) NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion      DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_actualizacion DATE NOT NULL DEFAULT CURRENT_DATE,

    CONSTRAINT fk_usuarios_cliente
        FOREIGN KEY (cliente_id) REFERENCES gestopago_clientes(id),
    CONSTRAINT uq_usuarios_cliente UNIQUE (cliente_id)
);

CREATE UNIQUE INDEX uq_usuarios_correo
    ON gestopago_usuarios (lower(correo));