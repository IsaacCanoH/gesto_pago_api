CREATE TABLE gestopago_domicilios (
    id              SERIAL PRIMARY KEY,
    cliente_id      INTEGER NOT NULL UNIQUE,
    calle           VARCHAR(150) NOT NULL,
    numero_exterior VARCHAR(20) NOT NULL,
    numero_interior VARCHAR(20),
    colonia         VARCHAR(100) NOT NULL,
    municipio       VARCHAR(100) NOT NULL,
    estado          VARCHAR(100) NOT NULL,
    codigo_postal   VARCHAR(5) NOT NULL,
    pais            VARCHAR(60) NOT NULL,

    CONSTRAINT fk_domicilios_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES gestopago_clientes(id)
);