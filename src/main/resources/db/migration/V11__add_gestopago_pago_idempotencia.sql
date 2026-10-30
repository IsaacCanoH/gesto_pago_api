ALTER TABLE gestopago_pagos
    ADD COLUMN solicitud_id UUID UNIQUE;

ALTER TABLE gestopago_pagos
    ADD CONSTRAINT ck_pagos_formulario_enviado
    CHECK (fecha_envio IS NULL
        OR (random_proveedor IS NOT NULL AND signed_proveedor IS NOT NULL));
