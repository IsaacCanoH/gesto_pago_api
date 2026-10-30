CREATE TABLE gestopago_pagos (
    id                      BIGSERIAL PRIMARY KEY,
    cuenta_id               INTEGER NOT NULL,
    producto_id             INTEGER NOT NULL,
    id_servicio_proveedor   BIGINT NOT NULL,
    id_producto_proveedor   BIGINT NOT NULL,
    monto_pago              NUMERIC(15, 2) NOT NULL,
    telefono                VARCHAR(20) NOT NULL,
    referencia              VARCHAR(100),
    upc                     VARCHAR(15) NOT NULL,
    hora_local              VARCHAR(14) NOT NULL,
    unidad                  VARCHAR(25) NOT NULL,
    estatus                 VARCHAR(10) NOT NULL DEFAULT 'PENDIENTE',
    version                 BIGINT NOT NULL DEFAULT 0,
    fecha_creacion          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pagos_cuenta
        FOREIGN KEY (cuenta_id) REFERENCES gestopago_cuentas(id),
    CONSTRAINT fk_pagos_producto
        FOREIGN KEY (producto_id) REFERENCES gestopago_productos(id),
    CONSTRAINT uq_pagos_upc UNIQUE (upc),
    CONSTRAINT ck_pagos_monto_positivo
        CHECK (monto_pago > 0 AND monto_pago <> 'NaN'::numeric),
    CONSTRAINT ck_pagos_telefono_longitud
        CHECK (char_length(telefono) BETWEEN 10 AND 20),
    CONSTRAINT ck_pagos_referencia_no_vacia
        CHECK (referencia IS NULL OR char_length(btrim(referencia)) > 0),
    CONSTRAINT ck_pagos_upc_longitud
        CHECK (char_length(upc) BETWEEN 3 AND 15),
    CONSTRAINT ck_pagos_hora_local_formato
        CHECK (hora_local ~ '^[0-9]{14}$'),
    CONSTRAINT ck_pagos_unidad_longitud
        CHECK (char_length(unidad) BETWEEN 3 AND 25),
    CONSTRAINT ck_pagos_estatus
        CHECK (estatus IN ('PENDIENTE', 'EXITOSO', 'FALLIDO'))
);

CREATE INDEX idx_pagos_cuenta_fecha
    ON gestopago_pagos (cuenta_id, fecha_creacion DESC);

CREATE INDEX idx_pagos_estatus_fecha
    ON gestopago_pagos (estatus, fecha_creacion);
