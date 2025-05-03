-- eKauplus Database Schema

-- Drop tables if they exist (in reverse order of creation to handle foreign keys)
DROP TABLE IF EXISTS user_activities CASCADE;
DROP TABLE IF EXISTS cart_items CASCADE;
DROP TABLE IF EXISTS marketplace_items_images CASCADE;
DROP TABLE IF EXISTS marketplace_items CASCADE;
DROP TABLE IF EXISTS products_tags CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS services CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;


-- Create roles table
CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(20) NOT NULL
);

-- Insert default roles
INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_MODERATOR');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');

-- Create users table
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(120) NOT NULL,
                       full_name VARCHAR(100),
                       phone VARCHAR(20),
                       address VARCHAR(200),
                       profile_image VARCHAR(255),
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP,
                       last_login_ip VARCHAR(50),
                       last_login_device VARCHAR(255),
                       last_login_browser VARCHAR(255),
                       last_login_date TIMESTAMP,
                       failed_login_attempts INTEGER DEFAULT 0,
                       account_locked BOOLEAN DEFAULT FALSE,
                       lock_time TIMESTAMP,
                       email_verified BOOLEAN DEFAULT FALSE,
                       verification_token VARCHAR(255),
                       verification_token_expiry_date TIMESTAMP,
                       reset_password_token VARCHAR(255),
                       reset_password_token_expiry_date TIMESTAMP
);

-- Create user_roles junction table
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_id INT NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                            FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- Create products table
CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          description TEXT,
                          price DECIMAL(12, 2) NOT NULL,
                          old_price DECIMAL(12, 2),
                          image VARCHAR(255),
                          category VARCHAR(50) NOT NULL,
                          brand VARCHAR(50),
                          stock INTEGER,
                          rating FLOAT,
                          review_count INTEGER,
                          is_new BOOLEAN DEFAULT FALSE,
                          discount_percentage INTEGER,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP
);

-- Create products_tags table
CREATE TABLE products_tags (
                               product_id BIGINT NOT NULL,
                               tag VARCHAR(50) NOT NULL,
                               PRIMARY KEY (product_id, tag),
                               FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);

-- Create services table
CREATE TABLE services (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          description TEXT,
                          price DECIMAL(12, 2) NOT NULL,
                          icon VARCHAR(50),
                          category VARCHAR(50),
                          duration_minutes INTEGER,
                          is_popular BOOLEAN DEFAULT FALSE,
                          is_available BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP
);

-- Create marketplace_items table
CREATE TABLE marketplace_items (
                                   id BIGSERIAL PRIMARY KEY,
                                   title VARCHAR(100) NOT NULL,
                                   description TEXT,
                                   price DECIMAL(12, 2) NOT NULL,
                                   user_id BIGINT NOT NULL,
                                   category VARCHAR(50),
                                   condition VARCHAR(50),
                                   location VARCHAR(50),
                                   status VARCHAR(20) DEFAULT 'ACTIVE',
                                   id_card_image VARCHAR(255),
                                   selling_fee_paid BOOLEAN DEFAULT FALSE,
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP,
                                   FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Create marketplace_items_images table
CREATE TABLE marketplace_items_images (
                                          marketplace_item_id BIGINT NOT NULL,
                                          image VARCHAR(255) NOT NULL,
                                          PRIMARY KEY (marketplace_item_id, image),
                                          FOREIGN KEY (marketplace_item_id) REFERENCES marketplace_items (id) ON DELETE CASCADE
);

-- Create cart_items table
CREATE TABLE cart_items (
                            id BIGSERIAL PRIMARY KEY,
                            user_id BIGINT NOT NULL,
                            item_type VARCHAR(20) NOT NULL,
                            item_id BIGINT NOT NULL,
                            quantity INTEGER NOT NULL,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP,
                            FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Create user_activities table
CREATE TABLE user_activities (
                                 id BIGSERIAL PRIMARY KEY,
                                 user_id BIGINT NOT NULL,
                                 activity_type VARCHAR(30) NOT NULL,
                                 ip_address VARCHAR(50),
                                 device VARCHAR(255),
                                 browser VARCHAR(255),
                                 details TEXT,
                                 success BOOLEAN,
                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_cart_items_user_id ON cart_items (user_id);
CREATE INDEX idx_marketplace_items_user_id ON marketplace_items (user_id);
CREATE INDEX idx_user_activities_user_id ON user_activities (user_id);
CREATE INDEX idx_products_category ON products (category);
CREATE INDEX idx_services_category ON services (category);
CREATE INDEX idx_marketplace_items_category ON marketplace_items (category);
CREATE INDEX idx_marketplace_items_status ON marketplace_items (status);

-- Create a function to update timestamp
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$

BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers to automatically update timestamps
CREATE TRIGGER update_users_timestamp
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER update_products_timestamp
    BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER update_services_timestamp
    BEFORE UPDATE ON services
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER update_marketplace_items_timestamp
    BEFORE UPDATE ON marketplace_items
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER update_cart_items_timestamp
    BEFORE UPDATE ON cart_items
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();