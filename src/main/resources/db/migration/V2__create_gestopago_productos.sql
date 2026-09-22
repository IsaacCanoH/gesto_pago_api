CREATE TABLE IF NOT EXISTS gestopago_productos (
    id                      SERIAL PRIMARY KEY,
    id_producto             BIGINT       NOT NULL,
    id_servicio             BIGINT,
    id_cat_tipo_servicio    BIGINT,
    servicio                VARCHAR(255) NOT NULL,
    producto                VARCHAR(500) NOT NULL,
    tipo_front              INTEGER,
    has_digito_verificador  BOOLEAN,
    precio                  NUMERIC(15, 2),
    show_ayuda              BOOLEAN,
    tipo_referencia         VARCHAR(50),
    leyenda                 TEXT,
    activo                  BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion          TIMESTAMP    NOT NULL DEFAULT NOW(),
    fecha_actualizacion     TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_gestopago_productos_id_producto UNIQUE (id_producto)
);