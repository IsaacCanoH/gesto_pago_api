CREATE TABLE gestopago_clientes (
    id                   SERIAL PRIMARY KEY,
    nombre               VARCHAR(50) NOT NULL,
    segundo_nombre       VARCHAR(50),
    apellido_paterno     VARCHAR(50) NOT NULL,
    apellido_materno     VARCHAR(50) NOT NULL,
    fecha_nacimiento     DATE NOT NULL,
    curp                 VARCHAR(18) NOT NULL UNIQUE,
    rfc                  VARCHAR(13) NOT NULL UNIQUE,
    sexo                 VARCHAR(20) NOT NULL,
    nacionalidad         VARCHAR(60) NOT NULL,
    estado_civil         VARCHAR(20) NOT NULL,
    telefono_movil       VARCHAR(10) NOT NULL,
    telefono_alternativo VARCHAR(10),
    ocupacion            VARCHAR(100) NOT NULL,
    empresa              VARCHAR(150) NOT NULL,
    ingreso_mensual      NUMERIC(15,2) NOT NULL
                         CHECK (ingreso_mensual > 0
                                AND ingreso_mensual <> 'NaN'::numeric),
    es_activa            BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion      DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_actualizacion DATE NOT NULL DEFAULT CURRENT_DATE
);