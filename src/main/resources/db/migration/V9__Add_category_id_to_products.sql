-- Link products to shop categories (leaf categories with type = 'SHOP')
ALTER TABLE products ADD COLUMN category_id BIGINT NULL;
ALTER TABLE products ADD CONSTRAINT fk_products_category
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL;
CREATE INDEX idx_products_category_id ON products(category_id);
