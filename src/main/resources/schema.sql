CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(4000),
    price DECIMAL(19, 2) NOT NULL,
    stock INTEGER NOT NULL,
    image_path VARCHAR(1024),
    CONSTRAINT ck_products_price_non_negative CHECK (price >= 0),
    CONSTRAINT ck_products_stock_non_negative CHECK (stock >= 0)
);

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_users_role CHECK (role IN ('CUSTOMER', 'ADMIN'))
);

CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(19, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_orders_status CHECK (status IN ('PENDING', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    CONSTRAINT ck_orders_total CHECK (total_amount >= 0)
);

CREATE TABLE IF NOT EXISTS order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products (id),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(19, 2) NOT NULL,
    CONSTRAINT ck_order_items_qty CHECK (quantity > 0),
    CONSTRAINT ck_order_items_price CHECK (unit_price >= 0)
);

CREATE INDEX IF NOT EXISTS idx_products_name ON products (name);
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_orders_user ON orders (user_id);
CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items (order_id);
