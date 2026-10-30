ALTER TABLE gestopago_pagos
    ADD COLUMN random_proveedor VARCHAR(64),
    ADD COLUMN signed_proveedor TEXT,
    ADD COLUMN fecha_envio TIMESTAMPTZ,
    ADD COLUMN fecha_ultima_confirmacion TIMESTAMPTZ,
    ADD COLUMN codigo_proveedor VARCHAR(10),
    ADD COLUMN mensaje_proveedor VARCHAR(500),
    ADD COLUMN id_transaccion_proveedor VARCHAR(60),
    ADD COLUMN numero_autorizacion VARCHAR(60),
    ADD COLUMN fecha_resolucion TIMESTAMPTZ;

CREATE INDEX idx_pagos_confirmacion
    ON gestopago_pagos (fecha_envio, fecha_ultima_confirmacion)
    WHERE estatus = 'PENDIENTE' AND fecha_envio IS NOT NULL;
