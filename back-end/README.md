# 🛒 E-Commerce Backend Application

> Spring Boot REST API cho hệ thống E-Commerce với tính năng quản lý sản phẩm, giỏ hàng, đơn hàng và thanh toán

---

## 📋 Mục lục

- [Tech Stack](#-tech-stack)
- [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
- [Cài đặt và chạy dự án](#-cài-đặt-và-chạy-dự-án)
- [Cấu hình Database](#-cấu-hình-database)
- [Seed dữ liệu mẫu](#-seed-dữ-liệu-mẫu)
- [API Documentation](#-api-documentation)
- [Admin API Authentication](#-admin-api-authentication)
- [Project Structure](#-project-structure)

---

## 🚀 Tech Stack

| Technology | Version | Mô tả |
|-----------|---------|-------|
| **Java** | 17+ | Programming Language |
| **Spring Boot** | 4.0.1 | Backend Framework |
| **Spring Data JPA** | - | ORM & Database Integration |
| **PostgreSQL** | 14+ | Relational Database |
| **Maven** | 3.8+ | Build Tool & Dependency Management |
| **Lombok** | - | Reduce Boilerplate Code |
| **Swagger/OpenAPI** | 3.0 | API Documentation |
| **Jakarta Validation** | - | Input Validation |
| **Spring Mail** | - | Email Service |

---

## 💻 Yêu cầu hệ thống

Trước khi bắt đầu, đảm bảo máy của bạn đã cài đặt:

### 1. **Java Development Kit (JDK)**
- ✅ **JDK 17** hoặc cao hơn
- Kiểm tra version:
  ```bash
  java -version
  ```
- Download tại: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) hoặc [OpenJDK](https://adoptium.net/)

### 2. **Apache Maven**
- ✅ **Maven 3.8+**
- Kiểm tra version:
  ```bash
  mvn -version
  ```
- Download tại: [Apache Maven](https://maven.apache.org/download.cgi)

### 3. **PostgreSQL Database**
- ✅ **PostgreSQL 14+**
- Kiểm tra version:
  ```bash
  psql --version
  ```
- Download tại: [PostgreSQL](https://www.postgresql.org/download/)

### 4. **IDE (Khuyến nghị)**
- IntelliJ IDEA / Eclipse / VS Code với Java Extension Pack

---

## 📦 Cài đặt và chạy dự án

### **Bước 1: Clone repository**
```bash
git clone <repository-url>
cd e-commerce/back-end
```

### **Bước 2: Tạo database PostgreSQL**

Mở PostgreSQL và tạo database mới:

```sql
-- Kết nối vào PostgreSQL
psql -U postgres

-- Tạo database
CREATE DATABASE hung_hypebeast_db;

### **Bước 3: Cấu hình environment variables**

1. Copy file `.env.example` thành `.env`:
   ```bash
   cp .env.example .env
   ```

2. Mở file `.env` và cập nhật thông tin:
   ```properties
   # Database Configuration
   DB_URL=jdbc:postgresql://localhost:5432/ecommerce_db
   DB_USERNAME=postgres
   DB_PASSWORD=your_database_password

   # Email Configuration (Gmail SMTP)
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-gmail-app-password
   MAIL_FROM=noreply@yourshop.com

   # Application Configuration
   APP_BASE_URL=http://localhost:8080
   SERVER_PORT=8080

   # SePay Configuration (Optional - for payment)
   SEPAY_WEBHOOK_SECRET=your-sepay-secret
   SEPAY_BANK_ACCOUNT=1234567890
   SEPAY_BANK_NAME=MB Bank
   SEPAY_ACCOUNT_NAME=YOUR NAME

   # Admin API Key (for protected endpoints)
   ADMIN_API_KEY=your-super-secret-admin-key-2024
   ```

### **Bước 4: Build project**

```bash
# Clean và build project
mvn clean install

# Hoặc build mà không chạy tests
mvn clean install -DskipTests
```

### **Bước 5: Chạy ứng dụng**

#### **Cách 1: Sử dụng Maven**
```bash
mvn spring-boot:run
```

#### **Cách 2: Chạy JAR file**
```bash
# Build JAR
mvn clean package

# Run JAR
java -jar target/e-commerce-0.0.1-SNAPSHOT.jar
```

#### **Cách 3: Từ IDE**
- Mở project trong IntelliJ IDEA / Eclipse
- Tìm và chạy file `ECommerceApplication.java`
- Click **Run** ▶️

### **Bước 6: Kiểm tra server đã chạy**

Mở trình duyệt và truy cập:
```
http://localhost:8080
```

Nếu thấy trang hoặc API response → **Server đã chạy thành công!** ✅

---

## 🗄️ Cấu hình Database

### **Schema tự động tạo**

Khi chạy lần đầu, Spring Boot sẽ **tự động tạo các bảng** trong database (vì `spring.jpa.hibernate.ddl-auto=update`).

### **Các bảng chính:**

| Bảng | Mô tả |
|------|-------|
| `category` | Danh mục sản phẩm |
| `products` | Sản phẩm |
| `product_variants` | Biến thể sản phẩm (size, màu sắc) |
| `product_images` | Hình ảnh sản phẩm |
| `cart` | Giỏ hàng |
| `cart_items` | Chi tiết giỏ hàng |
| `checkout_session` | Phiên thanh toán |
| `orders` | Đơn hàng |
| `order_items` | Chi tiết đơn hàng |
| `payment_transaction` | Giao dịch thanh toán |
| `inventory_reservation` | Đặt chỗ tồn kho |

---

## 🌱 Seed dữ liệu mẫu

Để test hệ thống, bạn cần insert dữ liệu mẫu vào database.

### **Cách 1: Sử dụng file SQL (Khuyến nghị)**

```bash
# Kết nối vào database
psql -U postgres -d hung_hypebeast_db

# Import dữ liệu mẫu
\i src/main/resources/insert_products_test.sql

# Kiểm tra dữ liệu
SELECT * FROM category;
SELECT * FROM products;
SELECT * FROM product_variants;

### **Cách 2: Copy-paste SQL**

Mở file `src/main/resources/insert_products_test.sql`, copy nội dung và chạy trong SQL client (pgAdmin, DBeaver, hoặc psql).

### **Dữ liệu mẫu bao gồm:**
- ✅ 2 danh mục (Category)
- ✅ 3 sản phẩm (Products)
- ✅ 6 biến thể (Product Variants với size, màu sắc)
- ✅ Hình ảnh sản phẩm (Product Images)

---

## 📚 API Documentation

Sau khi chạy server, truy cập **Swagger UI** để xem và test API:

```
http://localhost:8080/swagger-ui/index.html
```

### **Danh sách API đầy đủ:**

#### **1. Product APIs (Sản phẩm)**

**GET** `/api/v1/products` - Lấy danh sách sản phẩm
- **Mô tả:** Hỗ trợ filter, phân trang, sort
- **Auth:** ❌ Public
- **Query params:**
  - `page` - Số trang (default: 1)
  - `size` - Số items/trang (default: 12)
  - `category` - Lọc theo category slug
  - `minPrice` - Giá tối thiểu
  - `maxPrice` - Giá tối đa
  - `sort` - Sắp xếp (ví dụ: `createdAt,desc` hoặc `basePrice,asc`)

**GET** `/api/v1/products/{id}` - Chi tiết sản phẩm
- **Mô tả:** Chi tiết sản phẩm với SKU, size, màu
- **Auth:** ❌ Public

---

#### **2. Cart APIs (Giỏ hàng)**

> **Lưu ý:** Cart sử dụng Cookie `CART_ID` để lưu trữ, không cần authentication.

**GET** `/api/v1/cart` - Xem giỏ hàng hiện tại
- **Auth:** ❌ Public

**POST** `/api/v1/cart/items` - Thêm sản phẩm vào giỏ
- **Auth:** ❌ Public

**PUT** `/api/v1/cart/items/{itemId}` - Cập nhật số lượng sản phẩm
- **Auth:** ❌ Public

**DELETE** `/api/v1/cart/items/{itemId}` - Xóa sản phẩm khỏi giỏ
- **Auth:** ❌ Public

---

#### **3. Checkout APIs (Thanh toán)**

**POST** `/api/checkout/initiate?cartId={UUID}` - Bước 1: Khởi tạo checkout
- **Mô tả:** Reserve inventory, giữ hàng trong 15 phút
- **Auth:** ❌ Public

**POST** `/api/checkout/confirm?sessionId={sessionId}` - Bước 2: Xác nhận và tạo đơn hàng
- **Mô tả:** Complete reservation, tạo Order, trừ stock
- **Auth:** ❌ Public

**POST** `/api/checkout/cancel?sessionId={sessionId}` - Hủy checkout session
- **Mô tả:** Release reservation
- **Auth:** ❌ Public

---

#### **4. Order APIs (Đơn hàng)**

**GET** `/api/v1/orders/{orderId}` - Tracking đơn hàng
- **Mô tả:** PUBLIC - Khách hàng tracking đơn hàng
- **Auth:** ❌ Public

**GET** `/api/v1/orders` - Lấy danh sách tất cả đơn hàng (ADMIN)
- **Auth:** ✅ **Yêu cầu API Key**
- **Query params:**
  - `status` - Lọc theo trạng thái (PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED)
  - `page` - Số trang (default: 1)
  - `size` - Số items/trang (default: 20)
  - `sort` - Sắp xếp (ví dụ: `createdAt,desc`)

**PUT** `/api/v1/orders/{orderId}/status` - Cập nhật trạng thái đơn hàng (ADMIN)
- **Auth:** ✅ **Yêu cầu API Key**

---

#### **5. Webhook APIs (Payment callback)**

**POST** `/api/webhooks/sepay` - Nhận callback từ SePay khi thanh toán
- **Auth:** ❌ Public (dùng signature để xác thực)
- **Lưu ý:** Webhook endpoint dùng HMAC signature để xác thực, không cần API key

---

## 🔐 Admin API Authentication

Một số API dành cho Admin được bảo vệ bằng **API Key**.

### **Cách sử dụng:**

1. **Lấy API Key** từ file `.env`:
   ```
   ADMIN_API_KEY=your-super-secret-admin-key-2024
   ```

2. **Thêm vào Header** khi gọi API:
   ```
   Header Name: X-Admin-Key
   Header Value: your-super-secret-admin-key-2024
   ```

### **Ví dụ với cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/orders" \
     -H "X-Admin-Key: your-super-secret-admin-key-2024"
```

### **Ví dụ với Postman:**
1. Chọn tab **Headers**
2. Thêm:
   - Key: `X-Admin-Key`
   - Value: `your-super-secret-admin-key-2024`
3. Gửi request

### **Các endpoint cần API Key:**
- ✅ `GET /api/v1/orders` - Lấy tất cả đơn hàng
- ✅ `PUT /api/v1/orders/{orderId}/status` - Cập nhật trạng thái đơn hàng

---

## 📁 Project Structure

```
back-end/
├── src/
│   ├── main/
│   │   ├── java/org/project/ecommerce/
│   │   │   ├── base/              # Base classes (Controller, Entity, Response)
│   │   │   ├── config/            # Spring Configuration
│   │   │   │   ├── JpaConfig.java
│   │   │   │   ├── SwaggerConfig.java
│   │   │   │   ├── WebConfig.java
│   │   │   │   └── InventoryReservationScheduler.java
│   │   │   ├── constant/          # Enums (OrderStatus, PaymentMethod...)
│   │   │   ├── controller/        # REST Controllers
│   │   │   │   ├── ProductController.java
│   │   │   │   ├── CartController.java
│   │   │   │   ├── OrderController.java
│   │   │   │   ├── CheckoutController.java
│   │   │   │   └── WebhookController.java
│   │   │   ├── dto/               # Data Transfer Objects
│   │   │   │   ├── request/
│   │   │   │   └── response/
│   │   │   ├── entities/          # JPA Entities
│   │   │   ├── exception/         # Custom Exceptions
│   │   │   ├── mapper/            # DTO Mappers
│   │   │   ├── repository/        # Spring Data JPA Repositories
│   │   │   ├── security/          # Security (API Key Interceptor)
│   │   │   │   ├── RequireApiKey.java
│   │   │   │   └── ApiKeyInterceptor.java
│   │   │   ├── service/           # Business Logic
│   │   │   └── utils/             # Utility Classes
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── insert_products_test.sql
│   │       └── db_migration_sessionid_to_cartid.sql
│   └── test/                      # Unit & Integration Tests
├── .env.example                   # Environment variables template
├── pom.xml                        # Maven dependencies
└── README.md                      # Documentation
```

---

## 🔧 Các lệnh hữu ích

### **Maven Commands**
```bash
# Clean build
mvn clean

# Compile
mvn compile

# Run tests
mvn test

# Package JAR
mvn package

# Skip tests
mvn install -DskipTests

# Run application
mvn spring-boot:run
```

### **Database Commands**
```bash
# Kết nối database
psql -U postgres -d ecommerce_db

# List tables
\dt

# Describe table
\d products

# Exit
\q
```

---

## ⚙️ Cấu hình nâng cao

### **Thay đổi port server**
Trong file `.env`:
```properties
SERVER_PORT=9090
```

### **Bật/tắt SQL logging**
Trong `application.properties`:
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### **Cấu hình email với Gmail**
1. Đăng nhập Gmail
2. Vào [App Passwords](https://myaccount.google.com/apppasswords)
3. Tạo App Password mới
4. Copy password vào `.env` → `MAIL_PASSWORD`

---

## 🐛 Troubleshooting

### **1. Lỗi kết nối database**
```
Error: Connection refused: connect
```
**Giải pháp:**
- Kiểm tra PostgreSQL đã chạy: `sudo service postgresql status`
- Kiểm tra thông tin `.env` đúng với database
- Kiểm tra firewall/port 5432

### **2. Lỗi port đã được sử dụng**
```
Error: Port 8080 is already in use
```
**Giải pháp:**
- Đổi port trong `.env`: `SERVER_PORT=9090`
- Hoặc kill process đang dùng port:
  ```bash
  # Windows
  netstat -ano | findstr :8080
  taskkill /PID <PID> /F
  
  # Linux/Mac
  lsof -ti:8080 | xargs kill -9
  ```

### **3. Lỗi Maven dependency**
```
Error: Could not resolve dependencies
```
**Giải pháp:**
```bash
mvn clean install -U
```

---

## 📞 Liên hệ & Hỗ trợ

- **Email:** hoangquan15012004@gmail.com
- **GitHub Issues:** [Create Issue](https://github.com/QuanHoang3012/e-commerce/issues)

---
