CREATE TABLE gestopago_cuentas (
    id                  SERIAL PRIMARY KEY,
    cliente_id          INTEGER NOT NULL,
    numero_cuenta       VARCHAR(20) NOT NULL UNIQUE,
    saldo               NUMERIC(15,2) NOT NULL
                        CHECK (saldo >= 0 AND saldo <> 'NaN'::numeric),
    estatus             VARCHAR(10) NOT NULL DEFAULT 'ACTIVA'
                        CHECK (estatus IN ('ACTIVA', 'INACTIVA')),
    fecha_creacion      DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_actualizacion DATE NOT NULL DEFAULT CURRENT_DATE,

    CONSTRAINT fk_cuentas_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES gestopago_clientes(id)
);

CREATE INDEX idx_cuentas_cliente
    ON gestopago_cuentas (cliente_id);