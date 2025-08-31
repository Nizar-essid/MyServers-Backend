-- User Groups and Pricing System Database Schema
-- This script creates the necessary tables and relationships

-- Create user_groups table
CREATE TABLE IF NOT EXISTS user_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    discount_percentage DOUBLE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation DATETIME NOT NULL,
    latest_update DATETIME,
    created_by_id INTEGER,
    updated_by_id INTEGER,
    FOREIGN KEY (created_by_id) REFERENCES users(id),
    FOREIGN KEY (updated_by_id) REFERENCES users(id)
);

-- Create group_prices table
CREATE TABLE IF NOT EXISTS group_prices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_group_id BIGINT NOT NULL,
    server_id BIGINT NOT NULL,
    price DOUBLE NOT NULL,
    duration_days INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation DATETIME NOT NULL,
    latest_update DATETIME,
    created_by_id INTEGER,
    updated_by_id INTEGER,
    FOREIGN KEY (user_group_id) REFERENCES user_groups(id),
    FOREIGN KEY (server_id) REFERENCES servers(id),
    FOREIGN KEY (created_by_id) REFERENCES users(id),
    FOREIGN KEY (updated_by_id) REFERENCES users(id)
);

-- Create user_prices table
CREATE TABLE IF NOT EXISTS user_prices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INTEGER NOT NULL,
    server_id BIGINT NOT NULL,
    price DOUBLE NOT NULL,
    duration_days INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation DATETIME NOT NULL,
    latest_update DATETIME,
    created_by_id INTEGER,
    updated_by_id INTEGER,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (server_id) REFERENCES servers(id),
    FOREIGN KEY (created_by_id) REFERENCES users(id),
    FOREIGN KEY (updated_by_id) REFERENCES users(id)
);

-- Create user_balances table
CREATE TABLE IF NOT EXISTS user_balances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INTEGER NOT NULL UNIQUE,
    balance DOUBLE NOT NULL DEFAULT 0.0,
    total_deposited DOUBLE NOT NULL DEFAULT 0.0,
    total_spent DOUBLE NOT NULL DEFAULT 0.0,
    last_updated DATETIME NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create user_group_assignments table for ManyToMany relationship
CREATE TABLE IF NOT EXISTS user_group_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INTEGER NOT NULL,
    user_group_id BIGINT NOT NULL,
    assigned_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by_id INTEGER,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user_group_id) REFERENCES user_groups(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by_id) REFERENCES users(id),
    UNIQUE KEY unique_user_group (user_id, user_group_id)
);

-- Create balance_change_history table
CREATE TABLE IF NOT EXISTS balance_change_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INTEGER NOT NULL,
    admin_id INTEGER NULL,
    amount DOUBLE NOT NULL,
    previous_balance DOUBLE NOT NULL,
    new_balance DOUBLE NOT NULL,
    change_type ENUM('ADD', 'SET') NOT NULL,
    payment_status ENUM('PAID', 'UNPAID', 'CANCELLED') NOT NULL,
    description TEXT,
    cancellation_reason TEXT,
    change_date DATETIME NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (admin_id) REFERENCES users(id)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_user_groups_name ON user_groups(name);
CREATE INDEX IF NOT EXISTS idx_user_groups_active ON user_groups(is_active);
CREATE INDEX IF NOT EXISTS idx_group_prices_group_server ON group_prices(user_group_id, server_id);
CREATE INDEX IF NOT EXISTS idx_group_prices_active ON group_prices(is_active);
CREATE INDEX IF NOT EXISTS idx_user_prices_user_server ON user_prices(user_id, server_id);
CREATE INDEX IF NOT EXISTS idx_user_prices_active ON user_prices(is_active);
CREATE INDEX IF NOT EXISTS idx_user_balances_user ON user_balances(user_id);
CREATE INDEX IF NOT EXISTS idx_user_balances_active ON user_balances(is_active);
CREATE INDEX IF NOT EXISTS idx_user_group_assignments_user ON user_group_assignments(user_id);
CREATE INDEX IF NOT EXISTS idx_user_group_assignments_group ON user_group_assignments(user_group_id);
CREATE INDEX IF NOT EXISTS idx_user_group_assignments_active ON user_group_assignments(is_active);
CREATE INDEX IF NOT EXISTS idx_balance_change_history_user ON balance_change_history(user_id);
CREATE INDEX IF NOT EXISTS idx_balance_change_history_admin ON balance_change_history(admin_id);
CREATE INDEX IF NOT EXISTS idx_balance_change_history_date ON balance_change_history(change_date);
CREATE INDEX IF NOT EXISTS idx_balance_change_history_payment_status ON balance_change_history(payment_status);
CREATE INDEX IF NOT EXISTS idx_balance_change_history_change_type ON balance_change_history(change_type);
CREATE INDEX IF NOT EXISTS idx_balance_change_history_active ON balance_change_history(is_active);

-- Create applications table
CREATE TABLE IF NOT EXISTS applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    version VARCHAR(50) NOT NULL,
    size VARCHAR(20) NOT NULL,
    icon VARCHAR(500) NOT NULL,
    download_url VARCHAR(500) NOT NULL,
    category ENUM('STREAMING', 'SOCIAL_MEDIA', 'GAMING', 'PRODUCTIVITY', 'SECURITY') NOT NULL,
    is_popular BOOLEAN DEFAULT FALSE,
    download_count BIGINT DEFAULT 0,
    rating DOUBLE DEFAULT 0.0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert sample applications
INSERT INTO applications (name, description, version, size, icon, download_url, category, is_popular, download_count, rating, created_at, updated_at) VALUES
('Netflix', 'Regardez des films, séries TV et documentaires en streaming', '8.95.0', '45.2 MB', '/assets/images/netflix.jpg', '/assets/apps/netflix.apk', 'STREAMING', TRUE, 15420, 4.5, NOW(), NOW()),
('YouTube', 'Regardez et partagez des vidéos du monde entier', '18.45.1', '52.8 MB', '/assets/images/youtube.jpg', '/assets/apps/youtube.apk', 'STREAMING', TRUE, 28950, 4.7, NOW(), NOW()),
('WhatsApp', 'Messagerie instantanée sécurisée avec chiffrement de bout en bout', '2.23.24.78', '38.5 MB', '/assets/images/whatsapp.jpg', '/assets/apps/whatsapp.apk', 'SOCIAL_MEDIA', TRUE, 45680, 4.6, NOW(), NOW()),
('Instagram', 'Partagez vos photos et vidéos avec vos amis', '302.0.0.50.119', '41.2 MB', '/assets/images/instagram.jpg', '/assets/apps/instagram.apk', 'SOCIAL_MEDIA', FALSE, 23450, 4.3, NOW(), NOW()),
('TikTok', 'Découvrez et créez des vidéos courtes créatives', '32.5.3', '89.7 MB', '/assets/images/tiktok.jpg', '/assets/apps/tiktok.apk', 'SOCIAL_MEDIA', FALSE, 18760, 4.2, NOW(), NOW()),
('Spotify', 'Écoutez de la musique et des podcasts en streaming', '8.8.20.456', '67.3 MB', '/assets/images/spotify.jpg', '/assets/apps/spotify.apk', 'STREAMING', FALSE, 12340, 4.4, NOW(), NOW()),
('Telegram', 'Messagerie rapide et sécurisée avec de nombreuses fonctionnalités', '10.2.3', '55.8 MB', '/assets/images/telegram.jpg', '/assets/apps/telegram.apk', 'SOCIAL_MEDIA', FALSE, 9870, 4.5, NOW(), NOW()),
('VPN Secure', 'Protégez votre vie privée avec un VPN sécurisé', '3.2.1', '28.4 MB', '/assets/images/vpn.jpg', '/assets/apps/vpn-secure.apk', 'SECURITY', FALSE, 5430, 4.1, NOW(), NOW());

-- Create indexes for applications table
CREATE INDEX IF NOT EXISTS idx_applications_name ON applications(name);
CREATE INDEX IF NOT EXISTS idx_applications_category ON applications(category);
CREATE INDEX IF NOT EXISTS idx_applications_popular ON applications(is_popular);
CREATE INDEX IF NOT EXISTS idx_applications_active ON applications(is_active);
