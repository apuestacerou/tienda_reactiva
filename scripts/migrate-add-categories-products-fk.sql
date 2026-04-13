-- Neon: si ya tenías `products` sin categorías, ejecuta esto una vez.

CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_categories_slug ON categories (slug);

INSERT INTO categories (id, name, slug)
VALUES (gen_random_uuid(), 'General', 'general')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO categories (id, name, slug)
VALUES (gen_random_uuid(), 'Electrónica', 'electronica')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO categories (id, name, slug)
VALUES (gen_random_uuid(), 'Ropa', 'ropa')
ON CONFLICT (slug) DO NOTHING;

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS category_id UUID REFERENCES categories (id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_products_category ON products (category_id);
