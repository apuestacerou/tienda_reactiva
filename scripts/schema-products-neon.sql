-- Tabla que usa la aplicación (Product / ProductEntity).
-- En Neon: ejecutar si necesitas recrear solo el catálogo.
-- Columnas extra en la BD (categoría, fechas, etc.) no rompen la app si existen sin mapear.

CREATE TABLE IF NOT EXISTS products (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(4000),
    price       NUMERIC(19, 2) NOT NULL,
    stock       INTEGER NOT NULL,
    image_path  VARCHAR(1024),
    CONSTRAINT ck_products_price_non_negative CHECK (price >= 0),
    CONSTRAINT ck_products_stock_non_negative CHECK (stock >= 0)
);

CREATE INDEX IF NOT EXISTS idx_products_name ON products (name);
