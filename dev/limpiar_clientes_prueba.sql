-- Solo para la base local de pruebas. No es una migración Flyway.
-- Revisar los IDs y ejecutar manualmente DESPUÉS de aplicar V8 y ANTES de
-- registrar un cliente nuevo. Si los datos no coinciden, la transacción falla.
BEGIN;

DO $$
BEGIN
    IF current_database() <> 'gestopagodb'
       OR (SELECT COUNT(*) FROM gestopago_clientes) <> 2
       OR EXISTS (SELECT 1 FROM gestopago_clientes WHERE id NOT IN (1, 2))
       OR NOT EXISTS (
           SELECT 1
           FROM gestopago_usuarios
           WHERE cliente_id = 1
             AND lower(correo) = 'isaac.cano.hernandez@gmail.com'
       ) THEN
        RAISE EXCEPTION 'No se borró nada: la base no coincide con los dos clientes de prueba esperados';
    END IF;
END $$;

DELETE FROM gestopago_cuentas WHERE cliente_id IN (1, 2);
DELETE FROM gestopago_domicilios WHERE cliente_id IN (1, 2);
DELETE FROM gestopago_usuarios WHERE cliente_id IN (1, 2);
DELETE FROM gestopago_clientes WHERE id IN (1, 2);

COMMIT;
