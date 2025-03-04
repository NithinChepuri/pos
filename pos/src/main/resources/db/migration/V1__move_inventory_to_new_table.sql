-- First create the new inventory table
CREATE TABLE IF NOT EXISTS inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL UNIQUE,
    quantity INT NOT NULL DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Copy inventory data from products to inventory table
INSERT INTO inventory (product_id, quantity)
SELECT id, inventory_count 
FROM products;

-- Remove inventory_count column from products table
ALTER TABLE products DROP COLUMN inventory_count; 