# 📚 Hướng Dẫn Test API E-Commerce

## 🚀 Chuẩn Bị

### 1. Cài Đặt Dependencies
```bash
cd back-end
mvn clean install
```

### 2. Khởi Động Server
```bash
mvn spring-boot:run
```

Server sẽ chạy tại: **http://localhost:8080**

### 3. Truy Cập Swagger UI
Mở trình duyệt và truy cập:
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **API Docs (JSON)**: http://localhost:8080/v3/api-docs

---

## 📖 Danh Sách API Endpoints

### 🛍️ **1. PRODUCTS API** (Quản Lý Sản Phẩm)

#### 1.1. Lấy Danh Sách Sản Phẩm (Catalog)
```http
GET /api/v1/products
```

**Query Parameters:**
- `page` (int, default=1): Số trang
- `size` (int, default=12): Số sản phẩm mỗi trang
- `category` (string, optional): **Slug của category** (vd: "hoodie", "t-shirt") - Lọc theo danh mục
- `minPrice` (decimal, optional): Giá tối thiểu
- `maxPrice` (decimal, optional): Giá tối đa
- `sort` (string[], default="createdAt,desc"): Sắp xếp (vd: "minPrice,asc" hoặc "minPrice,desc")

**Ví dụ:**
```bash
# Lấy tất cả sản phẩm (trang 1, 12 sản phẩm)
curl -X GET "http://localhost:8080/api/v1/products?page=1&size=12"

# Lọc theo category slug "hoodie", giá từ 200k-500k, sắp xếp theo giá tăng dần
curl -X GET "http://localhost:8080/api/v1/products?category=hoodie&minPrice=200000&maxPrice=500000&sort=minPrice,asc"

# Lọc theo category slug "t-shirt", sắp xếp giá giảm dần
curl -X GET "http://localhost:8080/api/v1/products?category=t-shirt&sort=minPrice,desc"

# Lấy trang 2, mỗi trang 20 sản phẩm
curl -X GET "http://localhost:8080/api/v1/products?page=2&size=20"
```

**Response Mẫu:**
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "name": "Basic Hoodie",
        "minPrice": 350000,
        "categoryName": "Hoodie",
        "categorySlug": "hoodie",
        "thumbnail": "/images/hoodie-thumb.jpg"
      }
    ],
    "currentPage": 1,
    "totalPages": 5,
    "totalItems": 60,
    "pageSize": 12
  }
}
```

#### 1.2. Xem Chi Tiết Sản Phẩm
```http
GET /api/v1/products/{productId}
``` (By ID)
```http
GET /api/v1/products/{id}
```

**Path Variable:**
- `id` (UUID): ID của sản phẩm

**Ví dụ:**
```bash
curl -X GET "http://localhost:8080/api/v1/products/550e8400-e29b-41d4-a716-446655440000"
```

**Response Mẫu:**
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Basic Hoodie",
    "description": "Áo hoodie basic chất liệu cotton cao cấp, form rộng thoải mái",
    "minPrice": 350000,
    "maxPrice": 450000,
    "categoryName": "Hoodie",
    "images": [
      "/images/hoodie-1.jpg",
      "/images/hoodie-2.jpg",
      "/images/hoodie-3.jpg"
    ],
    "variants": [
      {
        "id": "variant-uuid-1",
        "skuCode": "HOD-BLK-S",
        "size": "S",
        "color": "Black",
        "price": 350000,
        "stockQuantity": 50
      },
      {
        "id": "variant-uuid-2",
        "skuCode": "HOD-BLK-M",
        "size": "M",
        "color": "Black",
        "price": 370000,
        "stockQuantity": 30
      },
      {
        "id": "variant-uuid-3",
        "skuCode": "HOD-WHT-L",
        "size": "L",
        "color": "White",
        "price": 400000,
        "stockQuantity": 2
}
```

---

### 🛒 **2. CART API** (Quản Lý Giỏ Hàng)

#### 2.1. Xem Giỏ Hàng
```http
GET /api/v1/cart/{cartId}
```

**Path Variable:**
- `cartId` (UUID): ID giỏ hàng (session-based hoặc user-based)

**Ví dụ:**
```bash
curl -X GET "http://localhost:8080/api/v1/cart"
```

**Response Mẫu:**
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "cartId": "cart-uuid-here",
    "items": [
      {
        "cartItemId": "item-uuid-1",
        "variantId": "variant-uuid-1",
        "productName": "Basic Hoodie",
        "variantInfo": "Size: M, Color: Black",
        "skuCode": "HOD-BLK-M",
        "price": 370000,
        "quantity": 2,
        "subtotal": 740000,
        "imageUrl": "/images/hoodie.jpg"
      }
    ],
    "totalAmount": 740000,
    "itemCount": 2,
    "status": "ACTIVE"
  }
}
```

#### 2.2. Thêm Sản Phẩm Vào Giỏ
```http
POST /api/v1/cart/{cartId}/items
```

**Path Variable:**
- `cartId` (UUID): ID giỏ hàng

**Request Body:**
```json
{
  "variantId": "variant-uuid-1",
  "quantity": 2
}
```

**Ví dụ:**
```bash
curl -X POST "http://localhost:8080/api/v1/cart/cart-uuid-here/items" \
  -H "Content-Type: application/json" \
  -d '{
    "variantId": "variant-uuid-1",
    "quantity": 2
  }'
```

#### 2.3. Cập Nhật Số Lượng
```http
PUT /api/v1/cart/{cartId}/items/{itemId}
```

**Request Body:**
```json
{
  "quantity": 3
}
```

**Ví dụ:**
```bash
curl -X PUT "http://localhost:8080/api/v1/cart/cart-uuid/items/item-uuid" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 3
  }'
```

#### 2.4. Xóa Sản Phẩm Khỏi Giỏ
```http
DELETE /api/v1/cart/{cartId}/items/{itemId}
```

**Ví dụ:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/cart/cart-uuid/items/item-uuid"
```

---

### 💳 **3. CHECKOUT API** (Thanh Toán)

#### 3.1. Khởi Tạo Checkout (Reserve Inventory)
```http
POST /api/checkout/initiate?cartId={cartId}
```

**Query Parameter:**
- `cartId` (UUID): ID giỏ hàng

**Response:**
- Trả về `sessionId` để giữ hàng trong 15 phút
- Khách hàng cần dùng `sessionId` này để confirm trong bước 2

**Ví dụ:**
```bash
curl -X POST "http://localhost:8080/api/checkout/initiate?cartId=cart-uuid-here"
```

**Response Mẫu:**
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "sessionId": "checkout-session-uuid",
    "totalItems": 2,
    "totalAmount": 740000,
    "reservedUntil": "2026-01-17T12:15:00Z"
  }
}
```

#### 3.2. Xác Nhận Checkout (Tạo Đơn Hàng)
```http
POST /api/checkout/confirm?sessionId={sessionId}
```

**Query Parameter:**
- `sessionId` (String): Session ID nhận được từ bước 3.1

**Request Body:**
```json
{
  "customerName": "Nguyễn Văn A",
  "customerPhone": "0901234567",
  "customerEmail": "customer@example.com",
  "shippingAddress": "123 Đường ABC, Quận 1, TP.HCM",
  "paymentMethod": "BANK_TRANSFER",
  "note": "Giao giờ hành chính"
}
```

**Payment Methods:**
- `BANK_TRANSFER`: Chuyển khoản (SePay)
- `COD`: Thanh toán khi nhận hàng

**Ví dụ:**
```bash
curl -X POST "http://localhost:8080/api/checkout/confirm?sessionId=checkout-session-uuid" \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Nguyễn Văn A",
    "customerPhone": "0901234567",
    "customerEmail": "customer@example.com",
    "shippingAddress": "123 Đường ABC, Quận 1, TP.HCM",
    "paymentMethod": "BANK_TRANSFER",
    "note": "Giao giờ hành chính"
  }'
```

**Response Mẫu (COD):**
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "orderId": "order-uuid",
    "trackingNumber": "ABC-123-456",
    "message": "Đặt hàng thành công! Mã đơn hàng: ABC-123-456. Đơn hàng sẽ được giao trong 2-3 ngày. Theo dõi tại: https://yoursite.com/track/ABC-123-456"
  }
}
```

**Response Mẫu (BANK_TRANSFER):**
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "orderId": "order-uuid",
    "trackingNumber": "ABC-123-456",
    "totalAmount": 740000,
    "message": "Đặt hàng thành công! Mã đơn hàng: ABC-123-456. Vui lòng chuyển khoản 740000 VNĐ với nội dung: ABC-123-456. Đơn hàng sẽ được xử lý sau khi nhận được thanh toán. Theo dõi tại: https://yoursite.com/track/ABC-123-456"
  }
}
```

#### 3.3. Hủy Checkout
```http
POST /api/checkout/cancel?sessionId={sessionId}
```

**Query Parameter:**
- `sessionId` (String): Session ID cần hủy

**Ví dụ:**
```bash
curl -X POST "http://localhost:8080/api/checkout/cancel?sessionId=checkout-session-uuid"
```

**Response:**
```json
{
  "status": 200,
  "message": "Success",
  "data": null
}
```

---

### 📦 **4. ORDER TRACKING API** (Theo Dõi Đơn Hàng)

#### 4.1. Tracking Đơn Hàng (PUBLIC - Không Cần Đăng Nhập)
```http
GET /api/orders/track/{trackingNumber}
```

**Path Variable:**
- `trackingNumber` (String): Mã tracking từ email

**Ví dụ:**
```bash
# Tracking bằng tracking number
curl -X GET "http://localhost:8080/api/orders/track/TRK-2026-0116-ABCD"
```

**Response Mẫu:**
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "orderId": "order-uuid",
    "trackingNumber": "TRK-2026-0116-ABCD",
    "customerName": "Nguyễn Văn A",
    "customerPhone": "0901234567",
    "customerEmail": "customer@example.com",
    "shippingAddress": "123 Đường ABC, Quận 1, TP.HCM",
    "totalAmount": 740000,
    "paymentMethod": "BANK_TRANSFER",
    "status": "SHIPPING",
    "orderDate": "2026-01-16T10:00:00Z",
    "lastUpdated": "2026-01-16T14:30:00Z",
    "items": [
      {
        "productName": "Basic Hoodie",
        "variantInfo": "Size: M, Color: Black",
        "skuCode": "HOD-BLK-M",
        "quantity": 2,
        "price": 370000,
        "subtotal": 740000
      }
    ],
    "timeline": {
      "pending": {
        "completed": true,
        "timestamp": "2026-01-16T10:00:00Z",
        "note": "Đơn hàng đã được tạo"
      },
      "confirmed": {
        "completed": true,
        "timestamp": "2026-01-16T10:15:00Z",
        "note": "Đơn hàng đã được xác nhận"
      },
      "paid": {
        "completed": true,
        "timestamp": "2026-01-16T11:00:00Z",
        "note": "Thanh toán thành công"
      },
      "processing": {
        "completed": true,
        "timestamp": "2026-01-16T12:00:00Z",
        "note": "Đang chuẩn bị hàng"
      },
      "shipping": {
        "completed": true,
        "timestamp": "2026-01-16T14:30:00Z",
        "note": "Đang giao hàng"
      },
      "delivered": {
        "completed": false,
        "timestamp": null,
        "note": null
      },
      "completed": {
        "completed": false,
        "timestamp": null,
        "note": null
      }
    }
  }
}
```

---

### 🔔 **5. WEBHOOK API** (Payment Gateway Callback)

#### 5.1. SePay Webhook (Xác Nhận Thanh Toán Chuyển Khoản)
```http
POST /api/webhooks/sepay
```

**Mô tả:**
- Endpoint PUBLIC (không cần authentication)
- SePay tự động gọi khi khách chuyển khoản thành công
- Hệ thống verify signature và cập nhật đơn hàng: PENDING → PAID

**Request Body:**
```json
{
  "transactionId": "TX123456789",
  "amount": 740000,
  "content": "ABC-123-456",
  "timestamp": "2026-01-17T10:30:00Z",
  "bankCode": "MB",
  "senderName": "NGUYEN VAN A",
  "senderAccount": "0123456789",
  "signature": "base64-encoded-hmac-sha256"
}
```

**Ví dụ:**
```bash
curl -X POST "http://localhost:8080/api/webhooks/sepay" \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "TX123456789",
    "amount": 740000,
    "content": "ABC-123-456",
    "timestamp": "2026-01-17T10:30:00Z",
    "bankCode": "MB",
    "senderName": "NGUYEN VAN A"
  }'
```

**Response:**
```json
{
  "status": 200,
  "message": "Success",
  "data": null
}
```

**Luồng hoạt động:**
1. Khách chuyển khoản với nội dung: `ABC-123-456` (tracking number)
2. SePay nhận tiền → gọi webhook này
3. Hệ thống verify signature
4. Extract tracking number từ content
5. Tìm order và verify amount
6. Cập nhật order: PENDING → PAID
7. Gửi email xác nhận (nếu có)

---

## 🧪 Kịch Bản Test Hoàn Chỉnh

### Kịch Bản 1: Mua Hàng Thành Công (COD)

#### Bước 1: Xem Danh Sách Sản Phẩm
```bash
curl -X GET "http://localhost:8080/api/v1/products?page=1&size=10"
```
→ Lấy `productId` của sản phẩm muốn mua

#### Bước 2: Xem Chi Tiết Sản Phẩm
```bash
curl -X GET "http://localhost:8080/api/v1/products/550e8400-e29b-41d4-a716-446655440000"
```
→ Lấy `variantId` của variant muốn mua (size, color)

#### Bước 3: Thêm Vào Giỏ Hàng
```bash
# Giả sử cartId = "3fa85f64-5717-4562-b3fc-2c963f66afa6"

curl -X POST "http://localhost:8080/api/v1/cart/3fa85f64-5717-4562-b3fc-2c963f66afa6/items" \
  -H "Content-Type: application/json" \
  -d '{
    "variantId": "variant-uuid-1",
    "quantity": 2
  }'
```

#### Bước 4: Xem Giỏ Hàng
```bash
curl -X GET "http://localhost:8080/api/v1/cart/3fa85f64-5717-4562-b3fc-2c963f66afa6"
```

#### Bước 5: Khởi Tạo Checkout (Reserve Inventory)
```bash
curl -X POST "http://localhost:8080/api/checkout/initiate?cartId=3fa85f64-5717-4562-b3fc-2c963f66afa6"
```
→ Nhận `sessionId` và `reservedUntil`

**Response:**
```json
{
  "data": {
    "sessionId": "checkout-session-uuid",
    "totalItems": 2,
    "totalAmount": 740000,
    "reservedUntil": "2026-01-17T12:15:00Z"
  }
}
```

#### Bước 6: Xác Nhận Checkout (COD)
```bash
curl -X POST "http://localhost:8080/api/checkout/confirm?sessionId=checkout-session-uuid" \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Nguyễn Văn A",
    "customerPhone": "0901234567",
    "customerEmail": "test@example.com",
    "shippingAddress": "123 Đường ABC, Quận 1, TP.HCM",
    "paymentMethod": "COD"
  }'
```
→ Nhận `trackingNumber` và message

**Response:**
```json
{
  "data": {
    "orderId": "order-uuid",
    "trackingNumber": "ABC-123-456",
    "message": "Đặt hàng thành công! Mã đơn hàng: ABC-123-456..."
  }
}
```

#### Bước 7: Tracking Đơn Hàng
```bash
curl -X GET "http://localhost:8080/api/orders/track/ABC-123-456"
```

---

## 📝 Test Trên Swagger UI

### Cách Sử dụng Swagger UI:

1. **Mở Swagger UI**: http://localhost:8080/swagger-ui/index.html

2. **Chọn API Group**: 
   - Products
   - Cart
   - Checkout
   - Orders
   - Webhook

3. **Test API**:
   - Click vào endpoint muốn test
   - Click nút **"Try it out"**
   - Nhập parameters/body
   - Click **"Execute"**
   - Xem kết quả trong **"Response"**

### Ví dụ Test Tracking API trên Swagger:

1. Mở **Orders** group
2. Click vào `GET /api/orders/track/{trackingNumber}`
3. Click **"Try it out"**
4. Nhập `trackingNumber`: `TRK-2026-0116-ABCD`
5. Click **"Execute"**
6. Xem response bên dưới

---

## 🔍 Test Với Postman

### Import Collection:

Tạo file `E-Commerce-APIs.postman_collection.json`:

```json
{
  "info": {
    "name": "E-Commerce APIs",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Products",
      "item": [
        {
          "name": "Get Products List",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8080/api/v1/products?page=1&size=12",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "v1", "products"],
              "query": [
                {"key": "page", "value": "1"},
                {"key": "size", "value": "12"}
              ]
            }
          }
        }
      ]
    },
    {
      "name": "Orders",
      "item": [
        {
          "name": "Track Order",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8080/api/orders/track/{{trackingNumber}}",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "orders", "track", "{{trackingNumber}}"]
            }
          }
        }
      ]
    }
  ]
}
```

Import vào Postman và test!

---

## ⚠️ Lưu Ý Quan Trọng

### 1. Database Setup
Đảm bảo database đã được setup với dữ liệu mẫu:
```bash
# Chạy script insert products
psql -U your_user -d ecommerce_db -f src/main/resources/insert_products_test.sql
```

### 2. Email Service
- Email service cần được cấu hình trong `application.properties`
- Nếu chưa config email, có thể comment phần gửi email trong code

### 3. Inventory Reservation
- Reservation tự động hết hạn sau 15 phút
- Có scheduler chạy background để cleanup

### 4. Error Handling
- Status 404: Không tìm thấy resource
- Status 400: Dữ liệu đầu vào không hợp lệ
- Status 500: Lỗi server

---

## 🎯 Checklist Test

- [ ] Test xem danh sách sản phẩm với phân trang
- [ ] Test filter sản phẩm theo category và giá
- [ ] Test xem chi tiết sản phẩm
- [ ] Test thêm sản phẩm vào giỏ
- [ ] Test cập nhật số lượng trong giỏ
- [ ] Test xóa sản phẩm khỏi giỏ
- [ ] Test checkout workflow hoàn chỉnh
- [ ] Test tracking đơn hàng bằng tracking number
- [ ] Test các trường hợp lỗi (404, 400, 500)
- [ ] Test reservation timeout (sau 15 phút)

---

## 📞 Hỗ Trợ

Nếu gặp vấn đề, kiểm tra:
1. Server đã chạy chưa?
2. Database đã connect chưa?
3. Log trong console có lỗi gì không?
4. Swagger UI có load được không?

**Happy Testing! 🚀**
