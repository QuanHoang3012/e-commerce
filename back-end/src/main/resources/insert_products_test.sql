-- Insert Categories
INSERT INTO category (id, name, slug, created_at, updated_at) values 
('11111111-1111-1111-1111-111111111111', 'Thời trang nam', 'thoi-trang-nam', NOW(), NOW());

INSERT INTO category (id, name, slug, parent_id, created_at, updated_at) values 
('11111111-1111-1111-1111-111111111112', 'Áo Nam', 'ao-nam', '11111111-1111-1111-1111-111111111111', NOW(), NOW());

-- Insert Products
-- Product 1: Áo Thun Basic
INSERT INTO products (id, name, description, min_price, category_id, created_at, updated_at) VALUES
('22222222-2222-2222-2222-222222222221', 'Áo Thun Basic Cotton', 'Áo thun cotton 100% thấm hút mồ hôi, kiểu dáng basic dễ phối đồ.', 150000, '11111111-1111-1111-1111-111111111112', NOW(), NOW());

-- Product 2: Áo Sơ Mi Oxford
INSERT INTO products (id, name, description, min_price, category_id, created_at, updated_at) VALUES
('22222222-2222-2222-2222-222222222222', 'Áo Sơ Mi Oxford', 'Áo sơ mi vải Oxford dày dặn, form regular fit lịch lãm.', 350000, '11111111-1111-1111-1111-111111111112', NOW(), NOW());

-- Product 3: Áo Polo
INSERT INTO products (id, name, description, min_price, category_id, created_at, updated_at) VALUES
('22222222-2222-2222-2222-222222222223', 'Áo Polo Coolmate', 'Áo Polo công nghệ mới, thoáng mát.', 250000, '11111111-1111-1111-1111-111111111112', NOW(), NOW());


-- Insert Product Variants
-- Variants for Product 1 (Áo Thun Basic)
INSERT INTO product_variants (id, product_id, sku_code, size, color, price, stock_quantity, version, created_at, updated_at) VALUES
('33333333-3333-3333-3333-333333333301', '22222222-2222-2222-2222-222222222221', 'AT-BASIC-W-S', 'S', 'White', 150000, 50, 0, NOW(), NOW()),
('33333333-3333-3333-3333-333333333302', '22222222-2222-2222-2222-222222222221', 'AT-BASIC-W-M', 'M', 'White', 150000, 50, 0, NOW(), NOW()),
('33333333-3333-3333-3333-333333333303', '22222222-2222-2222-2222-222222222221', 'AT-BASIC-B-L', 'L', 'Black', 160000, 30, 0, NOW(), NOW());

-- Variants for Product 2 (Áo Sơ Mi Oxford)
INSERT INTO product_variants (id, product_id, sku_code, size, color, price, stock_quantity, version, created_at, updated_at) VALUES
('33333333-3333-3333-3333-333333333304', '22222222-2222-2222-2222-222222222222', 'ASM-OX-BL-M', 'M', 'Blue', 350000, 20, 0, NOW(), NOW()),
('33333333-3333-3333-3333-333333333305', '22222222-2222-2222-2222-222222222222', 'ASM-OX-WH-L', 'L', 'White', 350000, 20, 0, NOW(), NOW());

-- Variants for Product 3 (Áo Polo)
INSERT INTO product_variants (id, product_id, sku_code, size, color, price, stock_quantity, version, created_at, updated_at) VALUES
('33333333-3333-3333-3333-333333333306', '22222222-2222-2222-2222-222222222223', 'AP-CM-NV-XL', 'XL', 'Navy', 250000, 100, 0, NOW(), NOW());


-- Insert Product Images
-- Images for Product 1
INSERT INTO product_images (id, product_id, image_url, is_thumbnail, display_order, created_at, updated_at) VALUES
(gen_random_uuid(), '22222222-2222-2222-2222-222222222221', 'https://example.com/images/ao-thun-white-1.jpg', true, 1, NOW(), NOW()),
(gen_random_uuid(), '22222222-2222-2222-2222-222222222221', 'https://example.com/images/ao-thun-black-1.jpg', false, 2, NOW(), NOW());

-- Images for Product 2
INSERT INTO product_images (id, product_id, image_url, is_thumbnail, display_order, created_at, updated_at) VALUES
(gen_random_uuid(), '22222222-2222-2222-2222-222222222222', 'https://example.com/images/ao-somi-blue.jpg', true, 1, NOW(), NOW());

-- Images for Product 3
INSERT INTO product_images (id, product_id, image_url, is_thumbnail, display_order, created_at, updated_at) VALUES
(gen_random_uuid(), '22222222-2222-2222-2222-222222222223', 'https://example.com/images/ao-polo-navy.jpg', true, 1, NOW(), NOW());
