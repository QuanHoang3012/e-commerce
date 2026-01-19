# TÀI LIỆU THIẾT KẾ CƠ SỞ DỮ LIỆU - HỆ THỐNG E-COMMERCE

## Ngày tạo: 20/01/2026

---

## MỤC LỤC

1. [Tổng Quan Hệ Thống](#1-tổng-quan-hệ-thống)
2. [Sơ Đồ ERD Tổng Thể](#2-sơ-đồ-erd-tổng-thể)
3. [Chi Tiết Các Bảng](#3-chi-tiết-các-bảng)
4. [Giải Thích Thiết Kế](#4-giải-thích-thiết-kế)
5. [Ràng Buộc và Index](#5-ràng-buộc-và-index)

---

## 1. TỔNG QUAN HỆ THỐNG

Hệ thống e-commerce được thiết kế với **11 bảng chính**, phân chia thành các nhóm chức năng:

### Nhóm Sản Phẩm (Product Management)
- `products` - Thông tin sản phẩm chính
- `product_variants` - Biến thể sản phẩm (size, màu sắc)
- `product_images` - Hình ảnh sản phẩm
- `category` - Danh mục sản phẩm (cây phân cấp)

### Nhóm Giỏ Hàng (Shopping Cart)
- `cart` - Giỏ hàng
- `cart_items` - Chi tiết sản phẩm trong giỏ

### Nhóm Đơn Hàng (Order Management)
- `orders` - Đơn hàng
- `order_items` - Chi tiết sản phẩm trong đơn hàng
- `payment_transactions` - Giao dịch thanh toán

### Nhóm Checkout & Inventory
- `checkout_sessions` - Phiên checkout
- `inventory_reservations` - Đặt chỗ tồn kho

---

## 2. SƠ ĐỒ ERD TỔNG THỂ

```
┌─────────────────┐
│    category     │
│─────────────────│
│ id (UUID)       │◄────┐ Self-referencing
│ name            │     │ (parent-child)
│ slug            │     │
│ parent_id (FK)  ├─────┘
│ created_at      │
│ updated_at      │
└────────┬────────┘
         │ 1
         │
         │ N
┌────────▼────────────┐       1       ┌──────────────────────┐
│     products        │◄──────────────┤  product_variants    │
│─────────────────────│               │──────────────────────│
│ id (UUID)           │               │ id (UUID)            │
│ name                │               │ product_id (FK)      │
│ description         │               │ sku_code (UNIQUE)    │
│ min_price           │               │ size                 │
│ category_id (FK)    │               │ color                │
│ created_at          │               │ price                │
│ updated_at          │               │ stock_quantity       │
└────────┬────────────┘               │ created_at           │
         │ 1                          │ updated_at           │
         │                            └──────────┬───────────┘
         │ N                                     │ 1
┌────────▼────────────┐                          │
│  product_images     │                          │ N
│─────────────────────│              ┌───────────▼───────────┐
│ id (UUID)           │              │     cart_items        │
│ product_id (FK)     │              │───────────────────────│
│ image_url           │              │ id (UUID)             │
│ is_thumbnail        │              │ cart_id (FK)          │
│ display_order       │      ┌───────┤ variant_id (FK)       │
│ created_at          │      │       │ quantity              │
│ updated_at          │      │       │ created_at            │
└─────────────────────┘      │       │ updated_at            │
                             │       └───────────────────────┘
         ┌───────────────────┘                   △
         │ N                                     │ N
         │                                       │
         │ 1                         ┌───────────┴───────────┐
┌────────▼────────────┐               │        cart           │
│   order_items       │               │───────────────────────│
│─────────────────────│               │ id (UUID)             │
│ id (UUID)           │               │ user_id               │
│ order_id (FK)       │               │ status (ENUM)         │
│ variant_id (FK)     │               │ last_active_at        │
│ quantity            │               │ created_at            │
│ price_at_purchase   │               │ updated_at            │
│ subtotal            │               └───────┬───────────────┘
│ created_at          │                       │ 1
│ updated_at          │                       │
└────────┬────────────┘                       │ 1
         │ N                      ┌────────────▼────────────┐
         │                        │  checkout_sessions      │
         │ 1                      │─────────────────────────│
┌────────▼────────────┐           │ id (UUID)               │
│      orders         │           │ session_id (UNIQUE)     │
│─────────────────────│           │ cart_id                 │
│ id (UUID)           │           │ order_id (FK)           │◄──┐
│ tracking_number     │◄──────────┤ expires_at              │   │
│ customer_name       │           │ is_completed            │   │ 1:1
│ customer_phone      │           │ created_at              │   │
│ customer_email      │           │ updated_at              │   │
│ shipping_address    │           └─────────────────────────┘   │
│ total_amount        │                                         │
│ payment_method      │           ┌─────────────────────────┐   │
│ status (ENUM)       │           │ inventory_reservations  │   │
│ created_at          │           │─────────────────────────│   │
│ updated_at          │           │ id (UUID)               │   │
└────────┬────────────┘           │ variant_id (FK)         ├───┘
         │ 1                      │ quantity                │
         │                        │ cart_id                 │
         │ N                      │ status (ENUM)           │
┌────────▼────────────┐           │ created_at              │
│ payment_transactions│           │ updated_at              │
│─────────────────────│           └─────────────────────────┘
│ id (UUID)           │
│ order_id (FK)       │
│ transaction_code    │
│ amount              │
│ payment_method      │
│ transaction_content │
│ status              │
│ created_at          │
│ updated_at          │
└─────────────────────┘
```

---

## 3. CHI TIẾT CÁC BẢNG

> **Ghi chú:** Tất cả các bảng đều có 3 cột chung:
> - `id` (UUID, PRIMARY KEY) - Sử dụng UUID thay vì INT để tăng bảo mật, phù hợp hệ thống phân tán
> - `created_at` (TIMESTAMP) - Thời điểm tạo record
> - `updated_at` (TIMESTAMP) - Thời điểm cập nhật gần nhất

---

### 3.1. Bảng `category` (Danh Mục Sản Phẩm)

**Cấu trúc bảng:**

- **`id`** (UUID, PRIMARY KEY): ID duy nhất
- **`name`** (VARCHAR(255), NOT NULL): Tên danh mục
- **`slug`** (VARCHAR(255), UNIQUE, NOT NULL): URL-friendly identifier
- **`parent_id`** (UUID, FOREIGN KEY → category.id): Danh mục cha (null = root)
- **`created_at`** (TIMESTAMP, NOT NULL): Thời điểm tạo
- **`updated_at`** (TIMESTAMP, NOT NULL): Thời điểm cập nhật

**Quan hệ:**
- **Self-referencing (1:N):** `parent_id` → `category.id` (Cây phân cấp)
  - ON DELETE: SET NULL (khi xóa category cha, các category con sẽ trở thành root)
  - ON UPDATE: CASCADE
- **One-to-Many:** 1 category có nhiều products
  - ON DELETE: RESTRICT (không cho xóa category còn products)
  - ON UPDATE: CASCADE

**Constraints bổ sung:**
- CHECK: `parent_id != id` (không cho category tự tham chiếu chính nó)

**Lý do thiết kế:**

**Mục đích:** Tạo cây danh mục nhiều cấp (hierarchical structure)

**Ưu điểm:**
- ✅ Flexible depth (không giới hạn cấp)
- ✅ Dễ query "All sub-categories"
- ✅ Breadcrumb navigation (Electronics > Computers > Laptops)
- ✅ Filter cascade (chọn "Electronics" → show tất cả sub-categories)

**Query ví dụ:**
```sql
-- Get all sub-categories of "Electronics"
WITH RECURSIVE category_tree AS (
    SELECT * FROM category WHERE id = 'electronics-id'
    UNION ALL
    SELECT c.* FROM category c
    INNER JOIN category_tree ct ON c.parent_id = ct.id
)
SELECT * FROM category_tree;
```

**Ví dụ cấu trúc:**
```
Electronics (parent_id = null)
  ├── Laptops (parent_id = Electronics.id)
  │     ├── Gaming Laptops
  │     └── Business Laptops
  └── Phones (parent_id = Electronics.id)
```

---

### 3.2. Bảng `products` (Sản Phẩm)

**Cấu trúc bảng:**

- **`id`** (UUID, PRIMARY KEY): ID duy nhất
- **`name`** (VARCHAR(255), NOT NULL): Tên sản phẩm
- **`description`** (TEXT): Mô tả chi tiết
- **`min_price`** (DECIMAL(10,2)): Giá thấp nhất của variants
- **`category_id`** (UUID, FOREIGN KEY → category.id): Danh mục
- **`created_at`** (TIMESTAMP, NOT NULL): Thời điểm tạo
- **`updated_at`** (TIMESTAMP, NOT NULL): Thời điểm cập nhật

**Quan hệ:**
- **Many-to-One:** N products → 1 category
  - ON DELETE: RESTRICT (không cho xóa category còn products)
  - ON UPDATE: CASCADE
- **One-to-Many:** 1 product → N product_variants
  - ON DELETE: CASCADE (xóa product → xóa tất cả variants)
  - ON UPDATE: CASCADE
- **One-to-Many:** 1 product → N product_images
  - ON DELETE: CASCADE (xóa product → xóa tất cả images)
  - ON UPDATE: CASCADE

**Lý do thiết kế:**
- **min_price:** Hiển thị nhanh giá "từ XX VNĐ" mà không cần query variants
- **Không lưu price trực tiếp:** Vì 1 sản phẩm có nhiều variants với giá khác nhau

---

### 3.3. Bảng `product_variants` (Biến Thể Sản Phẩm)

**Cấu trúc bảng:**

- **`id`** (UUID, PRIMARY KEY): ID duy nhất
- **`product_id`** (UUID, FOREIGN KEY → products.id, NOT NULL): Sản phẩm gốc
- **`sku_code`** (VARCHAR(255), UNIQUE, NOT NULL): Mã SKU duy nhất
- **`size`** (VARCHAR(50)): Kích thước (S, M, L, XL...)
- **`color`** (VARCHAR(50)): Màu sắc
- **`price`** (DECIMAL(10,2), NOT NULL): Giá bán
- **`stock_quantity`** (INTEGER, NOT NULL): Số lượng tồn kho
- **`created_at`** (TIMESTAMP, NOT NULL): Thời điểm tạo
- **`updated_at`** (TIMESTAMP, NOT NULL): Thời điểm cập nhật

**Quan hệ:**
- **Many-to-One:** N variants → 1 product
  - ON DELETE: CASCADE
  - ON UPDATE: CASCADE
- **One-to-Many:** 1 variant → N cart_items
  - ON DELETE: CASCADE (xóa variant → xóa cart_items)
  - ON UPDATE: CASCADE
- **One-to-Many:** 1 variant → N order_items
  - ON DELETE: RESTRICT (không cho xóa variant đã có trong orders)
  - ON UPDATE: CASCADE
- **One-to-Many:** 1 variant → N inventory_reservations
  - ON DELETE: CASCADE
  - ON UPDATE: CASCADE

**Constraints bổ sung:**
- CHECK: `price > 0` (giá phải dương)
- CHECK: `stock_quantity >= 0` (tồn kho không âm)
- DEFAULT: `stock_quantity = 0`

**Lý do thiết kế:**
- **Tại sao tách bảng variants?**
  - 1 sản phẩm có nhiều phiên bản (size, màu khác nhau)
  - Mỗi variant có giá và tồn kho riêng
  - Dễ quản lý SKU và inventory
  - Tránh duplicate data nếu lưu tất cả vào bảng products

**Ví dụ:**
```
Product: "Áo thun nam"
  Variant 1: Size M, Màu Đen, SKU: TS-M-BLK, Price: 200k, Stock: 50
  Variant 2: Size L, Màu Trắng, SKU: TS-L-WHT, Price: 200k, Stock: 30
  Variant 3: Size XL, Màu Đỏ, SKU: TS-XL-RED, Price: 220k, Stock: 20
```

---

### 3.4. Bảng `product_images` (Hình Ảnh Sản Phẩm)

**Cấu trúc bảng:**

- **`id`** (UUID, PRIMARY KEY): ID duy nhất
- **`product_id`** (UUID, FOREIGN KEY → products.id, NOT NULL): Sản phẩm
- **`image_url`** (VARCHAR(500), NOT NULL): URL hình ảnh
- **`is_thumbnail`** (BOOLEAN): Ảnh đại diện hay không
- **`display_order`** (INTEGER): Thứ tự hiển thị
- **`created_at`** (TIMESTAMP, NOT NULL): Thời điểm tạo
- **`updated_at`** (TIMESTAMP, NOT NULL): Thời điểm cập nhật

**Quan hệ:**
- **Many-to-One:** N images → 1 product
  - ON DELETE: CASCADE (xóa product → xóa tất cả images)
  - ON UPDATE: CASCADE

**Constraints bổ sung:**
- DEFAULT: `is_thumbnail = FALSE`
- DEFAULT: `display_order = 999`
- CHECK: `display_order >= 0`

**Lý do thiết kế:**
- **Tại sao tách bảng images?**
  - 1 sản phẩm có nhiều ảnh (gallery)
  - Dễ quản lý thứ tự hiển thị
  - Tối ưu loading (có thể lazy load ảnh phụ)
  - Tránh lưu JSON/Array trong 1 column

---

### 3.5. Bảng `cart` (Giỏ Hàng)

**Cấu trúc bảng:**

- **`id`** (UUID, PRIMARY KEY): ID duy nhất
- **`user_id`** (UUID, NULLABLE): ID người dùng (null = guest)
- **`status`** (ENUM, NOT NULL): Trạng thái giỏ hàng
- **`last_active_at`** (TIMESTAMP): Lần tương tác cuối
- **`created_at`** (TIMESTAMP, NOT NULL): Thời điểm tạo
- **`updated_at`** (TIMESTAMP, NOT NULL): Thời điểm cập nhật

**ENUM `status`:**
- `ACTIVE` - Giỏ hàng đang hoạt động
- `CHECKOUT` - Đang trong quá trình thanh toán
- `COMPLETED` - Đã hoàn thành đơn hàng
- `ABANDONED` - Bị bỏ rơi
- `CONVERTED` - Đã chuyển đổi thành đơn hàng

**Quan hệ:**
- **One-to-Many:** 1 cart → N cart_items
  - ON DELETE: CASCADE (xóa cart → xóa tất cả items)
  - ON UPDATE: CASCADE
- **One-to-One/Many:** 1 cart → 1/N checkout_sessions
  - ON DELETE: SET NULL
  - ON UPDATE: CASCADE

**Constraints bổ sung:**
- DEFAULT: `status = 'ACTIVE'`
- DEFAULT: `last_active_at = CURRENT_TIMESTAMP`
- INDEX: `idx_last_active` trên `last_active_at` (để cleanup carts cũ)

**Lý do thiết kế:**
- **user_id nullable:** Hỗ trợ cả guest checkout (dùng cookie để tracking)
- **status field:** Phân biệt cart đang active vs đã convert thành order
- **last_active_at:** Xóa cart cũ/abandoned sau thời gian nhất định

---

### 3.6. Bảng `cart_items` (Chi Tiết Giỏ Hàng)

**Cấu trúc bảng:**

- **`id`** (UUID, PRIMARY KEY): ID duy nhất
- **`cart_id`** (UUID, FOREIGN KEY → cart.id, NOT NULL): Giỏ hàng
- **`variant_id`** (UUID, FOREIGN KEY → product_variants.id, NOT NULL): Biến thể sản phẩm
- **`quantity`** (INTEGER, NOT NULL): Số lượng
- **`created_at`** (TIMESTAMP, NOT NULL): Thời điểm tạo
- **`updated_at`** (TIMESTAMP, NOT NULL): Thời điểm cập nhật

**Quan hệ:**
- **Many-to-One:** N cart_items → 1 cart
  - ON DELETE: CASCADE
  - ON UPDATE: CASCADE
- **Many-to-One:** N cart_items → 1 product_variant
  - ON DELETE: CASCADE
  - ON UPDATE: CASCADE

**Constraints bổ sung:**
- CHECK: `quantity > 0` (số lượng phải dương)
- UNIQUE: (`cart_id`, `variant_id`) - Một cart không có 2 dòng cùng variant

**Lý do thiết kế:**
- **Tại sao tách bảng cart_items?**
  - 1 cart chứa nhiều sản phẩm
  - Normalize data structure (1NF)
  - Dễ dàng CRUD từng item
  - Tránh lưu JSON/Array trong 1 column

---

### 3.7. Bảng `orders` (Đơn Hàng)

**Cấu trúc bảng:**

- **`id`** (UUID, PRIMARY KEY): ID duy nhất
- **`tracking_number`** (VARCHAR(255), UNIQUE): Mã vận đơn (auto-generated)
- **`customer_name`** (VARCHAR(255)): Tên khách hàng
- **`customer_phone`** (VARCHAR(20)): Số điện thoại
- **`customer_email`** (VARCHAR(255)): Email
- **`shipping_address`** (TEXT): Địa chỉ giao hàng
- **`total_amount`** (DECIMAL(10,2)): Tổng tiền
- **`payment_method`** (ENUM): Phương thức thanh toán
- **`status`** (ENUM, NOT NULL): Trạng thái đơn hàng
- **`created_at`** (TIMESTAMP, NOT NULL): Thời điểm tạo
- **`updated_at`** (TIMESTAMP, NOT NULL): Thời điểm cập nhật

**ENUM `payment_method`:**
- `COD` - Giao hàng thu tiền
- `BANK_TRANSFER` - Chuyển khoản (SePay)

**ENUM `status`:**
- `PENDING` - Chờ xác nhận
- `CONFIRMED` - Đã xác nhận
- `PAID` - Đã thanh toán (chỉ BANK_TRANSFER)
- `PROCESSING` - Đang chuẩn bị hàng
- `SHIPPING` - Đang giao hàng
- `DELIVERED` - Đã giao hàng
- `COMPLETED` - Hoàn thành
- `CANCELLED` - Đã hủy

**Quan hệ:**
- **One-to-Many:** 1 order → N order_items
  - ON DELETE: RESTRICT (không cho xóa order có items)
  - ON UPDATE: CASCADE
- **One-to-Many:** 1 order → N payment_transactions
  - ON DELETE: RESTRICT (không cho xóa order có transactions)
  - ON UPDATE: CASCADE
- **One-to-One:** 1 order ↔ 1 checkout_session
  - ON DELETE: SET NULL
  - ON UPDATE: CASCADE

**Constraints bổ sung:**
- CHECK: `total_amount >= 0`
- DEFAULT: `status = 'PENDING'`
- DEFAULT: `tracking_number = uuid_generate_v4()::text` (auto-generate)

**Lý do thiết kế:**
- **Không có user_id (nullable):** Hỗ trợ guest checkout
- **Lưu customer info:** Snapshot data tại thời điểm mua (user có thể đổi info sau)
- **tracking_number:** Dễ tra cứu cho khách hàng

---

### 3.8. Bảng `order_items` (Chi Tiết Đơn Hàng)

**Cấu trúc bảng:**

- **`id`** (UUID, PRIMARY KEY): ID duy nhất
- **`order_id`** (UUID, FOREIGN KEY → orders.id, NOT NULL): Đơn hàng
- **`variant_id`** (UUID, FOREIGN KEY → product_variants.id, NOT NULL): Biến thể sản phẩm
- **`quantity`** (INTEGER, NOT NULL): Số lượng
- **`price_at_purchase`** (DECIMAL(10,2), NOT NULL): Giá tại thời điểm mua
- **`subtotal`** (DECIMAL(10,2), NOT NULL): Tổng tiền (quantity × price)
- **`created_at`** (TIMESTAMP, NOT NULL): Thời điểm tạo
- **`updated_at`** (TIMESTAMP, NOT NULL): Thời điểm cập nhật

**Quan hệ:**
- **Many-to-One:** N order_items → 1 order
  - ON DELETE: RESTRICT
  - ON UPDATE: CASCADE
- **Many-to-One:** N order_items → 1 product_variant
  - ON DELETE: RESTRICT (giữ lại history, không xóa được variant)
  - ON UPDATE: CASCADE

**Constraints bổ sung:**
- CHECK: `quantity > 0`
- CHECK: `price_at_purchase > 0`
- CHECK: `subtotal = quantity * price_at_purchase` (computed column)
- IMMUTABLE: `price_at_purchase`, `subtotal` (không cho update sau khi tạo)

**Lý do thiết kế:**
- **price_at_purchase:** 
  - **QUAN TRỌNG!** Lưu giá tại thời điểm mua
  - Tránh việc sau này giá sản phẩm thay đổi làm sai lệch doanh thu
  - Đảm bảo invoice chính xác
- **subtotal:** Tính sẵn để tránh tính lại nhiều lần

---

### 3.9. Bảng `payment_transactions` (Giao Dịch Thanh Toán)

**Cấu trúc bảng:**

- **`id`** (UUID, PRIMARY KEY): ID duy nhất
- **`order_id`** (UUID, FOREIGN KEY → orders.id, NOT NULL): Đơn hàng
- **`transaction_code`** (VARCHAR(255), UNIQUE): Mã giao dịch từ payment gateway
- **`amount`** (DECIMAL(10,2)): Số tiền
- **`payment_method`** (ENUM): Phương thức thanh toán
- **`transaction_content`** (TEXT): Nội dung giao dịch
- **`status`** (VARCHAR(50)): Trạng thái (SUCCESS, FAILED, PENDING)
- **`created_at`** (TIMESTAMP, NOT NULL): Thời điểm tạo
- **`updated_at`** (TIMESTAMP, NOT NULL): Thời điểm cập nhật

**Quan hệ:**
- **Many-to-One:** N transactions → 1 order
  - ON DELETE: RESTRICT (không cho xóa order có transactions)
  - ON UPDATE: CASCADE

**Constraints bổ sung:**
- CHECK: `amount >= 0`
- INDEX: `idx_transaction_status` trên `status`
- INDEX: `idx_created_at` trên `created_at` (cho reporting)

**Lý do thiết kế:**
- **Tại sao tách bảng transaction?**
  - 1 đơn hàng có thể có nhiều lần thanh toán (thất bại → retry)
  - Audit trail cho việc thanh toán
  - Tracking webhook từ payment gateway (SePay)
  - Reconciliation với bank statement

---

### 3.10. Bảng `checkout_sessions` (Phiên Checkout)

**Cấu trúc bảng:**

- **`id`** (UUID, PRIMARY KEY): ID duy nhất
- **`session_id`** (VARCHAR(255), UNIQUE, NOT NULL): Session ID unique
- **`cart_id`** (UUID, NOT NULL): Cart gốc khởi tạo session
- **`order_id`** (UUID, FOREIGN KEY → orders.id): Order đã tạo
- **`expires_at`** (TIMESTAMP, NOT NULL): Thời gian hết hạn
- **`is_completed`** (BOOLEAN, DEFAULT FALSE): Đã hoàn tất chưa
- **`created_at`** (TIMESTAMP, NOT NULL): Thời điểm tạo
- **`updated_at`** (TIMESTAMP, NOT NULL): Thời điểm cập nhật

**Indexes:**
- `idx_session_id` trên `session_id`
- `idx_cart_id` trên `cart_id`

**Quan hệ:**
- **Many-to-One:** N sessions → 1 cart (1 cart có thể có nhiều lần checkout)
  - ON DELETE: CASCADE
  - ON UPDATE: CASCADE
- **One-to-One:** 1 session → 1 order
  - ON DELETE: SET NULL
  - ON UPDATE: CASCADE

**Constraints bổ sung:**
- DEFAULT: `is_completed = FALSE`
- DEFAULT: `expires_at = CURRENT_TIMESTAMP + INTERVAL '30 minutes'`
- CHECK: `expires_at > created_at`
- INDEX: `idx_expires_at` trên `expires_at` (cho cleanup job)

**Lý do thiết kế:**
- **Tại sao cần bảng checkout_sessions?**
  - Track từng lần initiate checkout (user có thể thoát rồi quay lại)
  - Implement TTL (time-to-live) cho checkout process
  - Snapshot data tại thời điểm checkout
  - Prevent double submission
  - Analytics: Conversion rate, abandoned checkout rate

---

### 3.11. Bảng `inventory_reservations` (Đặt Chỗ Tồn Kho)

**Cấu trúc bảng:**

- **`id`** (UUID, PRIMARY KEY): ID duy nhất
- **`variant_id`** (UUID, FOREIGN KEY → product_variants.id, NOT NULL): Biến thể sản phẩm
- **`quantity`** (INTEGER, NOT NULL): Số lượng đặt chỗ
- **`cart_id`** (VARCHAR(255), NOT NULL): Cart ID từ cookie
- **`status`** (ENUM, DEFAULT 'ACTIVE'): Trạng thái reservation
- **`created_at`** (TIMESTAMP, NOT NULL): Thời điểm tạo
- **`updated_at`** (TIMESTAMP, NOT NULL): Thời điểm cập nhật

**ENUM `status`:**
- `ACTIVE` - Đang giữ chỗ
- `COMPLETED` - Đã hoàn thành (đã order)
- `EXPIRED` - Hết hạn

**Indexes:**
- `idx_cart_id_status` trên (`cart_id`, `status`)
- `idx_variant_status` trên (`variant_id`, `status`)

**Quan hệ:**
- **Many-to-One:** N reservations → 1 product_variant
  - ON DELETE: CASCADE
  - ON UPDATE: CASCADE

**Constraints bổ sung:**
- CHECK: `quantity > 0`
- DEFAULT: `status = 'ACTIVE'`
- INDEX: `idx_created_status` trên (`created_at`, `status`) - Cleanup expired reservations
- Trigger: Auto-expire reservations sau 15-30 phút

**Lý do thiết kế:**
- **Tại sao cần bảng inventory_reservations?**
  - **Vấn đề race condition:** 2 người cùng mua 1 sản phẩm cuối cùng
  - **Giải pháp:** Khi add to cart → tạo reservation (giữ chỗ tạm thời)
  - **TTL:** Sau 15-30 phút không checkout → auto release
  - **Prevent overselling:** Stock actual = stock_quantity - SUM(active reservations)
  - **Flow:**
    1. Add to cart → Create reservation (ACTIVE)
    2. Checkout success → Update reservation (COMPLETED)
    3. Timeout/Cancel → Update reservation (EXPIRED)
    4. Scheduler job: Clean up expired reservations

---

## 4. GIẢI THÍCH THIẾT KẾ

### 4.1. Tại Sao Tách Bảng `product_variants` Từ `products`?

**Vấn đề nếu không tách:**

Nếu lưu tất cả trong bảng `products`:

| id | name | description | size | color | price | stock |
|----|------|-------------|------|-------|-------|-------|
| 1 | Áo thun | ... | M | Đen | 200k | 50 |
| 2 | Áo thun | ... | L | Trắng | 200k | 30 |
| 3 | Áo thun | ... | XL | Đỏ | 220k | 20 |

**Nhược điểm:**
- ❌ Duplicate `name` và `description` (vi phạm 2NF)
- ❌ Khó quản lý: Update tên sản phẩm phải update 3 rows
- ❌ Không có khái niệm "1 sản phẩm có nhiều variants"
- ❌ Khó query "Tất cả variants của 1 sản phẩm"

**Giải pháp với 2 bảng:**

```
products:
| id | name | description |
| 1  | Áo thun | Áo thun nam cotton... |

product_variants:
| id | product_id | size | color | price | stock | sku_code |
| 1  | 1          | M    | Đen   | 200k  | 50    | TS-M-BLK |
| 2  | 1          | L    | Trắng | 200k  | 30    | TS-L-WHT |
| 3  | 1          | XL   | Đỏ    | 220k  | 20    | TS-XL-RED |
```

**Ưu điểm:**
- ✅ Normalize data (2NF/3NF)
- ✅ Dễ quản lý inventory từng variant
- ✅ SKU tracking chính xác
- ✅ Query hiệu quả hơn

---

### 4.2. Tại Sao Cần Bảng `product_images`?

**Vấn đề nếu lưu trong `products`:**

```sql
-- Cách 1: Multiple columns
products (id, name, image1, image2, image3, image4, image5)
❌ Giới hạn số lượng ảnh
❌ Lãng phí space nếu ít ảnh
❌ Khó sort/reorder

-- Cách 2: JSON array
products (id, name, images JSON)
❌ Khó query/filter
❌ Không thể join
❌ Không có referential integrity
❌ Khó quản lý thumbnail
```

**Giải pháp với bảng riêng:**

```
product_images:
| id | product_id | image_url | is_thumbnail | display_order |
| 1  | 1          | img1.jpg  | TRUE         | 1             |
| 2  | 1          | img2.jpg  | FALSE        | 2             |
| 3  | 1          | img3.jpg  | FALSE        | 3             |
```

**Ưu điểm:**
- ✅ Unlimited số lượng ảnh
- ✅ Dễ reorder (update display_order)
- ✅ Dễ xác định thumbnail
- ✅ Query riêng thumbnail cho performance
- ✅ Lazy loading ảnh phụ

---

### 4.1. Tại Sao Tách Bảng `product_variants` Từ `products`?

**Vấn đề nếu lưu JSON trong `cart`:**

```sql
cart (id, user_id, items JSON)
-- items = [{"variant_id": 1, "quantity": 2}, {"variant_id": 3, "quantity": 1}]
```

**Nhược điểm:**
- ❌ Vi phạm 1NF (non-atomic value)
- ❌ Không thể foreign key constraint
- ❌ Khó query (WHERE JSON_CONTAINS...)
- ❌ Khó update quantity 1 item
- ❌ Không thể join với product_variants

**Giải pháp:**

```
cart:
| id | user_id | status |

cart_items:
| id | cart_id | variant_id | quantity |
| 1  | 1       | 5          | 2        |
| 2  | 1       | 7          | 1        |
```

**Ưu điểm:**
- ✅ CRUD từng item dễ dàng
- ✅ Foreign key constraints
- ✅ Join với variants để get thông tin sản phẩm
- ✅ Aggregate queries (SUM, COUNT) hiệu quả

---

### 4.3. Tại Sao `order_items` Phải Lưu `price_at_purchase`?

**Vấn đề nếu không lưu:**

```sql
-- Tính tổng tiền order
SELECT SUM(oi.quantity * pv.price)
FROM order_items oi
JOIN product_variants pv ON oi.variant_id = pv.id
WHERE order_id = 123;
```

**Tình huống:**
- User A đặt hàng ngày 01/01/2026, giá = 200k
- Ngày 15/01/2026, shop giảm giá còn 150k
- User A xem lại order → Tổng tiền bị SAI (tính theo giá mới 150k)

**Giải pháp:**

```sql
order_items (order_id, variant_id, quantity, price_at_purchase, subtotal)
```

**Ưu điểm:**
- ✅ **Historical accuracy:** Giá không đổi theo thời gian
- ✅ **Invoice integrity:** In lại hóa đơn sau nhiều năm vẫn đúng
- ✅ **Revenue reporting:** Báo cáo doanh thu chính xác
- ✅ **Compliance:** Pháp lý yêu cầu lưu giá tại thời điểm bán

---

### 4.4. Tại Sao Cần Bảng `checkout_sessions`?

**Vấn đề cần giải quyết:**

1. **User có thể checkout nhiều lần từ 1 cart:**
   - Lần 1: Initiate → Thoát
   - Lần 2: Initiate lại → Payment failed
   - Lần 3: Success

2. **Prevent double submission:**
   - User click "Đặt hàng" 2 lần nhanh
   - Cần check session đã tạo order chưa

3. **TTL (Time-to-Live):**
   - Session tồn tại 30 phút
   - Quá thời gian → Expired → Release inventory reservation

**Thiết kế:**

```
checkout_sessions:
| id | session_id | cart_id | order_id | expires_at | is_completed |
| 1  | sess_abc   | cart_1  | NULL     | 12:30 PM   | FALSE        |
| 2  | sess_xyz   | cart_1  | order_5  | 01:00 PM   | TRUE         |
```

**Flow:**

```
1. User click "Checkout" 
   → Create checkout_session (session_id = random UUID)
   → Create inventory_reservations

2. User điền thông tin, click "Đặt hàng"
   → Check session.is_completed (prevent double order)
   → Create order
   → Update session: order_id = new_order.id, is_completed = TRUE
   → Update reservations: status = COMPLETED

3. Nếu user thoát giữa chừng
   → Scheduler job check expires_at
   → Release reservations
```

**Ưu điểm:**
- ✅ Track conversion funnel
- ✅ Prevent race conditions
- ✅ Analytics: Abandoned checkout rate
- ✅ Retry mechanism (resume checkout)

---

### 4.5. Tại Sao Cần Bảng `inventory_reservations`?

**Vấn đề Race Condition:**

**Scenario:**
- Product X có stock = 1
- User A add to cart lúc 10:00:00
- User B add to cart lúc 10:00:01
- Cả 2 đều thấy "Available"
- User A checkout lúc 10:05 → Success
- User B checkout lúc 10:06 → Out of stock! ❌

**Giải pháp:**

```
product_variants:
| id | sku_code | stock_quantity |
| 1  | SKU-001  | 10             |

inventory_reservations:
| id | variant_id | quantity | cart_id | status  | created_at |
| 1  | 1          | 3        | cart_A  | ACTIVE  | 10:00      |
| 2  | 1          | 5        | cart_B  | ACTIVE  | 10:01      |
| 3  | 1          | 2        | cart_C  | EXPIRED | 09:30      |
| 4  | 1          | 1        | cart_D  | COMPLETED | 09:00    |
```

**Tính Available Stock:**

```sql
SELECT 
    pv.stock_quantity - COALESCE(SUM(ir.quantity), 0) AS available_stock
FROM product_variants pv
LEFT JOIN inventory_reservations ir 
    ON pv.id = ir.variant_id 
    AND ir.status = 'ACTIVE'
WHERE pv.id = 1
GROUP BY pv.id;

-- Result: 10 - (3 + 5) = 2 (available)
```

**Flow:**

```
1. User add to cart (quantity = 2)
   → Check available_stock >= 2
   → Create reservation (ACTIVE, expires in 15 min)

2. Sau 15 phút:
   → Scheduler job: Update status = EXPIRED
   → Available stock tăng lên

3. User checkout:
   → Update reservation: status = COMPLETED
   → Decrease product_variants.stock_quantity
```

**Ưu điểm:**
- ✅ Prevent overselling
- ✅ Fair allocation (first-come-first-served)
- ✅ Auto-release after timeout
- ✅ Audit trail

---

### 4.6. Tại Sao Cần Bảng `payment_transactions`?

**Tình huống:**

1. User đặt hàng, chọn "Chuyển khoản"
2. Payment gateway trả về: PENDING
3. 5 phút sau: Payment failed
4. User retry payment → PENDING
5. Payment success

**Nếu không có bảng này:**
- ❌ Không track được payment history
- ❌ Webhook từ payment gateway update vào đâu?
- ❌ Không biết user đã retry bao nhiêu lần
- ❌ Khó reconcile với bank statement

**Giải pháp:**

```
payment_transactions:
| id | order_id | transaction_code | amount | status   | created_at |
| 1  | order_5  | TXN_001          | 500k   | PENDING  | 10:00      |
| 2  | order_5  | TXN_001          | 500k   | FAILED   | 10:05      |
| 3  | order_5  | TXN_002          | 500k   | PENDING  | 10:10      |
| 4  | order_5  | TXN_002          | 500k   | SUCCESS  | 10:15      |
```

**Ưu điểm:**
- ✅ Complete audit trail
- ✅ Support multiple payment attempts
- ✅ Webhook updates
- ✅ Reconciliation với bank
- ✅ Refund tracking

---

## 5. RÀNG BUỘC VÀ INDEX

### 5.1. Foreign Key Constraints

| Bảng | Foreign Key | References |
|------|-------------|------------|
| `category` | `parent_id` | `category(id)` |
| `products` | `category_id` | `category(id)` |
| `product_variants` | `product_id` | `products(id)` |
| `product_images` | `product_id` | `products(id)` |
| `cart_items` | `cart_id` | `cart(id)` |
| `cart_items` | `variant_id` | `product_variants(id)` |
| `order_items` | `order_id` | `orders(id)` |
| `order_items` | `variant_id` | `product_variants(id)` |
| `payment_transactions` | `order_id` | `orders(id)` |
| `checkout_sessions` | `order_id` | `orders(id)` |
| `inventory_reservations` | `variant_id` | `product_variants(id)` |

### 5.2. Unique Constraints

| Bảng | Columns |
|------|---------|
| `category` | `slug` |
| `product_variants` | `sku_code` |
| `orders` | `tracking_number` |
| `checkout_sessions` | `session_id` |
| `payment_transactions` | `transaction_code` |

### 5.3. Indexes (Đã Định Nghĩa)

| Bảng | Index Name | Columns | Lý Do |
|------|------------|---------|-------|
| `checkout_sessions` | `idx_session_id` | `session_id` | Lookup nhanh session |
| `checkout_sessions` | `idx_cart_id` | `cart_id` | Query sessions của 1 cart |
| `inventory_reservations` | `idx_cart_id_status` | `cart_id, status` | Check reservations của cart |
| `inventory_reservations` | `idx_variant_status` | `variant_id, status` | Tính available stock |

### 5.4. Indexes Đề Xuất Thêm

```sql
-- Product queries
CREATE INDEX idx_product_category ON products(category_id);
CREATE INDEX idx_product_variant_product ON product_variants(product_id);
CREATE INDEX idx_product_image_product ON product_images(product_id);

-- Cart queries
CREATE INDEX idx_cart_user ON cart(user_id);
CREATE INDEX idx_cart_status ON cart(status);
CREATE INDEX idx_cart_item_cart ON cart_items(cart_id);
CREATE INDEX idx_cart_item_variant ON cart_items(variant_id);

-- Order queries
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_created ON orders(created_at);
CREATE INDEX idx_order_customer_email ON orders(customer_email);
CREATE INDEX idx_order_item_order ON order_items(order_id);
CREATE INDEX idx_order_item_variant ON order_items(variant_id);

-- Payment queries
CREATE INDEX idx_payment_order ON payment_transactions(order_id);
CREATE INDEX idx_payment_status ON payment_transactions(status);

-- Category queries
CREATE INDEX idx_category_parent ON category(parent_id);
CREATE INDEX idx_category_slug ON category(slug);
```

---

## 6. TỔNG KẾT

### Điểm Mạnh Của Thiết Kế

1. **Normalization:** Tuân thủ 3NF, tránh duplicate data
2. **Scalability:** Dễ mở rộng thêm tính năng (reviews, wishlists...)
3. **Data Integrity:** Foreign keys, constraints đầy đủ
4. **Performance:** Indexes hợp lý cho queries phổ biến
5. **Audit Trail:** Timestamps, transaction logs
6. **Business Logic:** 
   - Snapshot pricing (price_at_purchase)
   - Inventory management (reservations)
   - Multi-level categories
   - Guest checkout support

### Có Thể Cải Thiện

1. **Soft Delete:** Thêm `deleted_at` cho việc khôi phục
2. **Versioning:** Track changes history cho products
3. **Multi-currency:** Hỗ trợ nhiều loại tiền tệ
4. **Multi-language:** i18n cho product names/descriptions
5. **Reviews/Ratings:** Bảng product_reviews
6. **Wishlist:** Bảng user_wishlists
7. **Promotions:** Bảng coupons, discounts
8. **Shipping:** Chi tiết shipping methods, zones, rates

---

**Người thiết kế:** [Tên của bạn]  
**Ngày:** 20/01/2026  
**Phiên bản:** 1.0

