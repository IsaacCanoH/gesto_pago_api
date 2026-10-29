ALTER TABLE gestopago_clientes
    ADD COLUMN pin_hash VARCHAR(255),
    ADD COLUMN pin_intentos_fallidos INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN pin_bloqueado_hasta TIMESTAMPTZ;
