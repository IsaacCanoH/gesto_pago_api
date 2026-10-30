ALTER TABLE gestopago_cuentas
    ADD COLUMN saldo_retenido NUMERIC(15, 2) NOT NULL DEFAULT 0;

ALTER TABLE gestopago_cuentas
    ADD CONSTRAINT ck_cuentas_saldo_retenido
    CHECK (saldo_retenido >= 0
        AND saldo_retenido <= saldo
        AND saldo_retenido <> 'NaN'::numeric);
