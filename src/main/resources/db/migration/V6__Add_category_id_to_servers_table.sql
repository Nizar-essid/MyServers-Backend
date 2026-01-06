-- Add category_id column to servers table
-- Servers can only be associated with leaf categories (categories without children)

ALTER TABLE servers ADD COLUMN category_id BIGINT NULL;

ALTER TABLE servers 
ADD CONSTRAINT fk_server_category 
FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL;

CREATE INDEX idx_server_category_id ON servers(category_id);

