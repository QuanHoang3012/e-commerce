# 🛍️ E-Commerce Full Stack Application

> Modern E-Commerce platform built with Spring Boot (Backend) and React/Next.js (Frontend)

---

## 📖 Tổng quan

Hệ thống E-Commerce full-stack với các tính năng:
- 🛒 Quản lý sản phẩm với variants (size, màu sắc)
- 🛍️ Giỏ hàng thông minh (Cookie-based)
- 💳 Thanh toán với inventory reservation
- 📦 Quản lý đơn hàng và tracking
- 🔐 Admin API với API Key authentication
- 📧 Email notifications
- 💰 Payment gateway integration (SePay)

---

## 🏗️ Kiến trúc hệ thống

```
┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
│                 │         │                 │         │                 │
│   Frontend      │────────▶│   Backend API   │────────▶│   PostgreSQL    │
│   (React/Next)  │  HTTP   │  (Spring Boot)  │  JDBC   │    Database     │
│                 │         │                 │         │                 │
└─────────────────┘         └─────────────────┘         └─────────────────┘
                                    │
                                    │ SMTP
                                    ▼
                            ┌─────────────────┐
                            │  Email Service  │
                            │     (Gmail)     │
                            └─────────────────┘
```

---

## 📁 Cấu trúc thư mục

```
e-commerce/
├── back-end/                 # Spring Boot REST API
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   ├── pom.xml
│   ├── .env.example
│   └── README.md            # Backend documentation
│
├── front-end/               # React/Next.js Frontend (Coming soon)
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── README.md
│
└── README.md               # This file
```

---

## 🚀 Tech Stack

### **Backend (Spring Boot)**

| Technology | Version | Purpose |
|-----------|---------|---------|
| Java | 17+ | Programming Language |
| Spring Boot | 4.0.1 | Backend Framework |
| Spring Data JPA | - | ORM & Database |
| PostgreSQL | 14+ | Database |
| Maven | 3.8+ | Build Tool |
| Lombok | - | Boilerplate Reduction |
| Swagger/OpenAPI | 3.0 | API Documentation |
| Spring Mail | - | Email Service |

### **Frontend (React/Next.js)** *(Coming Soon)*

| Technology | Version | Purpose |
|-----------|---------|---------|
| React | 18+ | UI Library |
| Next.js | 14+ | React Framework |
| TypeScript | 5+ | Type Safety |
| Tailwind CSS | 3+ | Styling |
| Axios | - | HTTP Client |
| React Query | - | State Management |

---

## 🛠️ Cài đặt và chạy dự án

### **Prerequisites**

Đảm bảo đã cài đặt:
- ✅ Java JDK 17+
- ✅ Node.js 18+ (cho Frontend)
- ✅ PostgreSQL 14+
- ✅ Maven 3.8+

### **1. Clone repository**

```bash
git clone https://github.com/QuanHoang3012/e-commerce.git
cd e-commerce
```

### **2. Setup Backend**

```bash
cd back-end

# Copy và cấu hình .env
cp .env.example .env
# Sửa thông tin database, email, API keys trong .env

# Tạo database
psql -U postgres -c "CREATE DATABASE ecommerce_db;"

# Build và chạy
mvn clean install
mvn spring-boot:run
```

Backend sẽ chạy tại: `http://localhost:8080`

📚 **Chi tiết:** Xem [back-end/README.md](back-end/README.md)

### **3. Setup Frontend** *(Coming Soon)*

```bash
cd front-end

# Install dependencies
npm install

# Run development server
npm run dev
```

Frontend sẽ chạy tại: `http://localhost:3000`

---

## 📚 API Documentation

Sau khi chạy backend, truy cập Swagger UI:

```
http://localhost:8080/swagger-ui/index.html
```

### **API Endpoints Overview:**

**Public APIs:**
- `GET /api/v1/products` - Danh sách sản phẩm
- `GET /api/v1/products/{id}` - Chi tiết sản phẩm
- `POST /api/v1/cart/items` - Thêm vào giỏ hàng
- `POST /api/checkout/initiate` - Khởi tạo thanh toán
- `GET /api/v1/orders/{id}` - Tracking đơn hàng

**Admin APIs (Yêu cầu API Key):**
- `GET /api/v1/orders` - Danh sách đơn hàng
- `PUT /api/v1/orders/{id}/status` - Cập nhật trạng thái

📖 **Full API Documentation:** Xem [back-end/README.md#api-documentation](back-end/README.md#api-documentation)

---

## 🔐 Authentication & Security

### **Backend:**
- Admin APIs sử dụng **API Key** authentication
- Header: `X-Admin-Key: your-secret-key`
- Cart sử dụng **HttpOnly Cookie** (`CART_ID`)
- Webhook sử dụng **HMAC Signature** verification

### **Frontend:** *(Coming Soon)*
- JWT-based authentication
- Protected routes
- Session management

---

## 🗄️ Database Schema

### **Main Tables:**

| Table | Description |
|-------|-------------|
| `category` | Danh mục sản phẩm (tree structure) |
| `products` | Sản phẩm |
| `product_variants` | Biến thể (SKU, size, color, price, stock) |
| `product_images` | Hình ảnh sản phẩm |
| `cart` | Giỏ hàng |
| `cart_items` | Chi tiết giỏ hàng |
| `checkout_session` | Phiên thanh toán |
| `orders` | Đơn hàng |
| `order_items` | Chi tiết đơn hàng |
| `payment_transaction` | Giao dịch thanh toán |
| `inventory_reservation` | Đặt chỗ tồn kho (15 phút) |

---

## 🎯 Features

### ✅ Đã hoàn thành (Backend)

- [x] Product catalog với filter & pagination
- [x] Cart management với Cookie
- [x] Inventory reservation system (15 phút)
- [x] Checkout flow (3 bước)
- [x] Order tracking
- [x] Admin order management
- [x] Email notifications
- [x] Payment webhook integration
- [x] API documentation (Swagger)
- [x] API Key authentication

### 🚧 Đang phát triển (Frontend)

- [ ] Product listing page
- [ ] Product detail page
- [ ] Shopping cart UI
- [ ] Checkout flow
- [ ] Order tracking page
- [ ] Admin dashboard
- [ ] User authentication

---

## 🔧 Development

### **Backend Development**

```bash
cd back-end

# Run với hot reload
mvn spring-boot:run

# Run tests
mvn test

# Build production JAR
mvn clean package
java -jar target/e-commerce-0.0.1-SNAPSHOT.jar
```

### **Frontend Development** *(Coming Soon)*

```bash
cd front-end

# Development mode
npm run dev

# Build production
npm run build
npm start

# Run tests
npm test
```

---

## 🚀 Deployment

### **Backend (Spring Boot)**

**Option 1: JAR File**
```bash
mvn clean package
java -jar target/e-commerce-0.0.1-SNAPSHOT.jar
```

**Option 2: Docker** *(Coming Soon)*
```bash
docker build -t ecommerce-backend .
docker run -p 8080:8080 ecommerce-backend
```

### **Frontend (Next.js)** *(Coming Soon)*

```bash
npm run build
npm start
```

---

## 📊 Performance & Optimization

### **Backend:**
- ✅ Database indexing (category_id, sku_code, order_status)
- ✅ JPA lazy loading
- ✅ Connection pooling (HikariCP)
- ✅ Query optimization với pagination
- ✅ Scheduled task cho inventory cleanup

### **Frontend:** *(Planned)*
- Server-side rendering (Next.js)
- Image optimization
- Code splitting
- Caching strategy

---

## 🧪 Testing

### **Backend:**
```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# Coverage report
mvn clean test jacoco:report
```

### **Frontend:** *(Coming Soon)*
```bash
npm test
npm run test:e2e
```

---

## 📝 Environment Variables

### **Backend (.env)**
```properties
# Database
DB_URL=jdbc:postgresql://localhost:5432/ecommerce_db
DB_USERNAME=postgres
DB_PASSWORD=your_password

# Email (Gmail)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Admin
ADMIN_API_KEY=your-secret-key

# Payment
SEPAY_WEBHOOK_SECRET=webhook-secret
SEPAY_BANK_ACCOUNT=account-number
```

### **Frontend (.env.local)** *(Coming Soon)*
```properties
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_STRIPE_KEY=pk_test_...
```

---

## 🐛 Troubleshooting

### **Backend Issues:**

**Database connection error:**
```bash
# Kiểm tra PostgreSQL
sudo service postgresql status

# Reset database
psql -U postgres -c "DROP DATABASE ecommerce_db;"
psql -U postgres -c "CREATE DATABASE ecommerce_db;"
```

**Port already in use:**
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/AmazingFeature`
3. Commit your changes: `git commit -m 'Add some AmazingFeature'`
4. Push to the branch: `git push origin feature/AmazingFeature`
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License.

---

## 👥 Team

- **Backend Developer:** Hoàng Quân
- **Frontend Developer:** Coming Soon
- **DevOps:** Coming Soon

---

## 📞 Contact & Support

- **Email:** hoangquan15012004@gmail.com
- **GitHub:** [@QuanHoang3012](https://github.com/QuanHoang3012)
- **Issues:** [GitHub Issues](https://github.com/QuanHoang3012/e-commerce/issues)

---

## 📚 Documentation

- **Backend API:** [back-end/README.md](back-end/README.md)
- **Frontend:** [front-end/README.md](front-end/README.md) *(Coming Soon)*
- **API Reference:** http://localhost:8080/swagger-ui/index.html
- **Database Schema:** [docs/database-schema.md](docs/database-schema.md) *(Coming Soon)*

---

## 🗺️ Roadmap

### **Phase 1: Backend (✅ Completed)**
- [x] Core API development
- [x] Database design
- [x] Authentication system
- [x] Payment integration

### **Phase 2: Frontend (🚧 In Progress)**
- [ ] UI/UX design
- [ ] Component development
- [ ] State management
- [ ] Integration with Backend API

### **Phase 3: DevOps (📅 Planned)**
- [ ] Docker containerization
- [ ] CI/CD pipeline
- [ ] Cloud deployment (AWS/Azure)
- [ ] Monitoring & logging

### **Phase 4: Advanced Features (📅 Planned)**
- [ ] Product reviews & ratings
- [ ] Wishlist
- [ ] Discount & coupon system
- [ ] Analytics dashboard
- [ ] Real-time notifications

---

**Happy Coding! 🚀**
