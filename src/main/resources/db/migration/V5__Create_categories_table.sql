-- Create categories table for hierarchical server categorization
-- Categories can have parent-child relationships (self-referencing)

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    parent_id BIGINT NULL,
    date_creation DATETIME NOT NULL,
    latest_update DATETIME NULL,
    created_by_id_user INTEGER NULL,
    updated_by_id_user INTEGER NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by_id_user) REFERENCES users(id),
    FOREIGN KEY (updated_by_id_user) REFERENCES users(id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_is_active (is_active),
    INDEX idx_sort_order (sort_order)
);

-- Add comment to explain the table
ALTER TABLE categories COMMENT = 'Hierarchical categories for server organization. Only leaf categories (without children) can contain servers.';

