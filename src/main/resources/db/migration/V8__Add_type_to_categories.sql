-- Add type column to categories: SERVER (for servers/codes) or SHOP (for products)
ALTER TABLE categories ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'SERVER';
CREATE INDEX idx_categories_type ON categories(type);
