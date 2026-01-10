-- Create products table
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DOUBLE NOT NULL,
    image VARCHAR(500) NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation DATETIME NOT NULL,
    latest_update DATETIME,
    created_by_id INTEGER,
    updated_by_id INTEGER,
    FOREIGN KEY (created_by_id) REFERENCES admins(id),
    FOREIGN KEY (updated_by_id) REFERENCES admins(id)
);

-- Create quote_requests table
CREATE TABLE IF NOT EXISTS quote_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id INTEGER,
    contact_name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(50),
    message TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    date_creation DATETIME NOT NULL,
    date_processed DATETIME,
    processed_by_id INTEGER,
    admin_notes TEXT,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (processed_by_id) REFERENCES admins(id)
);

-- Create shop_access table for controlling which user groups can access the shop
CREATE TABLE IF NOT EXISTS shop_access (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_group_id BIGINT NOT NULL UNIQUE,
    has_access BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation DATETIME NOT NULL,
    latest_update DATETIME,
    FOREIGN KEY (user_group_id) REFERENCES user_groups(id) ON DELETE CASCADE
);

CREATE INDEX idx_product_active ON products(is_active, is_available);
CREATE INDEX idx_quote_request_status ON quote_requests(status);
CREATE INDEX idx_quote_request_user ON quote_requests(user_id);

