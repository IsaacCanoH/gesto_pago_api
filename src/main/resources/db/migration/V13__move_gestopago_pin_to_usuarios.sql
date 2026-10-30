ALTER TABLE gestopago_usuarios
    ADD COLUMN pin_hash VARCHAR(255),
    ADD COLUMN pin_intentos_fallidos INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN pin_bloqueado_hasta TIMESTAMPTZ;

UPDATE gestopago_usuarios AS usuario
SET pin_hash = cliente.pin_hash,
    pin_intentos_fallidos = cliente.pin_intentos_fallidos,
    pin_bloqueado_hasta = cliente.pin_bloqueado_hasta
FROM gestopago_clientes AS cliente
WHERE usuario.cliente_id = cliente.id;

ALTER TABLE gestopago_clientes
    DROP COLUMN pin_hash,
    DROP COLUMN pin_intentos_fallidos,
    DROP COLUMN pin_bloqueado_hasta;
