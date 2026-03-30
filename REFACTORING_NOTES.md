# Checkout Flow Refactoring Notes

## Ngày: 21/01/2026

---

## 🎯 Mục Đích Refactoring

Tối ưu hóa checkout flow để **tránh tạo và xóa Order records không cần thiết** trong database.

---

## ❌ Vấn Đề Trước Khi Refactor

### Flow cũ:
```
initiateCheckout():
  1. Tạo Order (status=PENDING, customerName=null)  ⚠️
  2. Tạo CheckoutSession → link với Order
  3. Reserve inventory

confirmCheckout():
  1. Update Order (thêm thông tin khách hàng)
  2. Complete reservation
  
cancelCheckout():
  1. Delete Order  ❌ WASTE!
  2. Delete CheckoutSession
```

### Hậu quả:
- ❌ Database pollution: Nhiều Order records incomplete bị DELETE
- ❌ Waste resources: UUID/Auto-increment IDs cho Orders không bao giờ hoàn thành
- ❌ Audit logs đầy DELETE operations
- ❌ Order table có data không nhất quán (customerName = NULL)

---

## ✅ Giải Pháp Sau Khi Refactor

### Flow mới:
```
initiateCheckout():
  1. Tạo CheckoutSession only (không tạo Order)  ✅
  2. Reserve inventory dựa trên cart items
  
confirmCheckout():
  1. TẠO Order MỚI với đầy đủ thông tin khách hàng  ✅
  2. Complete reservation
  3. Link Order vào CheckoutSession
  
cancelCheckout():
  1. Delete CheckoutSession only  ✅
     (không có Order để xóa)
```

### Lợi ích:
- ✅ **Không xóa Order**: Order chỉ được tạo khi đã có đầy đủ thông tin
- ✅ **Database sạch hơn**: Không có Order records incomplete
- ✅ **Business logic rõ ràng**: Order = "Đơn hàng đã xác nhận", không phải "đang đặt"
- ✅ **Separation of concerns**: CheckoutSession = temporary, Order = permanent

---

## 📝 Chi Tiết Thay Đổi

### 1. CheckoutServiceImpl.initiateCheckout()

**Trước:**
```java
// Tạo Order ngay
Order order = Order.builder()
    .totalAmount(totalAmount)
    .status(OrderStatus.PENDING)
    .items(new ArrayList<>())
    .build();

// Copy cart items sang order items
for (CartItem cartItem : cart.getItems()) {
    OrderItem orderItem = ...
    order.getItems().add(orderItem);
}

Order savedOrder = orderRepository.save(order);

// Tạo session link với Order
CheckoutSession session = CheckoutSession.builder()
    .order(savedOrder)  // ⚠️
    ...
```

**Sau:**
```java
// Chỉ tính tổng tiền, KHÔNG tạo Order
BigDecimal totalAmount = cart.getItems().stream()
    .map(item -> item.getVariant().getPrice()
            .multiply(BigDecimal.valueOf(item.getQuantity())))
    .reduce(BigDecimal.ZERO, BigDecimal::add);

// Tạo session KHÔNG có Order
CheckoutSession session = CheckoutSession.builder()
    .sessionId(sessionId)
    .cart(cart)
    .order(null)  // ✅ Không tạo Order ngay
    .expiresAt(expiresAt)
    .isCompleted(false)
    .build();
```

### 2. CheckoutServiceImpl.confirmCheckout()

**Trước:**
```java
// Lấy Order đã tạo sẵn từ session
Order order = session.getOrder();

// Cập nhật thông tin
order.setCustomerName(request.getCustomerName());
order.setCustomerPhone(request.getCustomerPhone());
...
```

**Sau:**
```java
// TẠO Order MỚI với đầy đủ thông tin
Order order = Order.builder()
    .customerName(request.getCustomerName())
    .customerPhone(request.getCustomerPhone())
    .customerEmail(request.getCustomerEmail())
    .shippingAddress(request.getShippingAddress())
    .paymentMethod(request.getPaymentMethod())
    .totalAmount(totalAmount)
    .status(OrderUtils.determineInitialStatus(request.getPaymentMethod()))
    .items(new ArrayList<>())
    .build();

// Copy cart items sang order items (snapshot)
for (CartItem cartItem : cart.getItems()) {
    OrderItem orderItem = OrderItem.builder()
        .order(order)
        .variant(cartItem.getVariant())
        .quantity(cartItem.getQuantity())
        .priceAtPurchase(cartItem.getVariant().getPrice())
        .build();
    order.getItems().add(orderItem);
}

// Lưu Order và link vào session
Order savedOrder = orderRepository.save(order);
session.setOrder(savedOrder);
```

### 3. CheckoutServiceImpl.cancelCheckout()

**Trước:**
```java
// Xóa order và session
if (session.getOrder() != null) {
    orderRepository.delete(session.getOrder());  // ❌
}
checkoutSessionRepository.delete(session);
```

**Sau:**
```java
// Chỉ xóa session (không có Order để xóa)
checkoutSessionRepository.delete(session);  // ✅
```

### 4. Thêm Cleanup Method

**Mới thêm:**
```java
@Override
@Transactional
public int cleanupExpiredSessions() {
    Instant now = Instant.now();
    List<CheckoutSession> expiredSessions = checkoutSessionRepository
            .findByIsCompletedAndExpiresAtBefore(false, now);

    for (CheckoutSession session : expiredSessions) {
        reservationService.releaseReservation(session.getSessionId());
        checkoutSessionRepository.delete(session);
    }
    
    return expiredSessions.size();
}
```

### 5. Repository Method Mới

**CheckoutSessionRepository:**
```java
List<CheckoutSession> findByIsCompletedAndExpiresAtBefore(
    Boolean isCompleted, Instant expiresAt);
```

### 6. Scheduled Task Mới

**InventoryReservationScheduler:**
```java
@Scheduled(fixedRate = 120000) // 2 phút
public void cleanupExpiredSessions() {
    int cleaned = checkoutService.cleanupExpiredSessions();
    if (cleaned > 0) {
        log.info("[SCHEDULER] Cleaned up {} expired checkout sessions", cleaned);
    }
}
```

---

## 🔄 Workflow So Sánh

### Trước Refactor:
```
User initiate checkout
  → Create Order (id=1, customerName=NULL)
  → Create CheckoutSession (links to Order #1)
  → Reserve inventory

User cancels
  → DELETE Order #1  ❌ WASTE
  → DELETE CheckoutSession
```

### Sau Refactor:
```
User initiate checkout
  → Create CheckoutSession only
  → Reserve inventory

User cancels
  → DELETE CheckoutSession only  ✅ CLEAN
```

---

## 📊 Impact Analysis

### Database Impact:
| Aspect | Before | After |
|--------|--------|-------|
| Order table pollution | ❌ High (many deleted records) | ✅ Zero |
| Order records incomplete | ❌ Yes (customerName=NULL) | ✅ No |
| Unnecessary DELETE ops | ❌ Yes | ✅ No |
| ID space waste | ❌ Yes | ✅ No |

### Code Quality:
| Aspect | Before | After |
|--------|--------|-------|
| Business logic clarity | ⚠️ Order = "đang đặt"? | ✅ Order = "đã đặt" |
| Separation of concerns | ⚠️ Mixed | ✅ Clear |
| Data consistency | ⚠️ Order can be incomplete | ✅ Order always complete |

---

## 🧪 Testing Checklist

- [ ] Test `initiateCheckout`: Không tạo Order record
- [ ] Test `confirmCheckout`: Tạo Order mới với đầy đủ thông tin
- [ ] Test `cancelCheckout`: Chỉ xóa CheckoutSession
- [ ] Test scheduled cleanup: Tự động xóa expired sessions
- [ ] Test inventory reservation: Vẫn hoạt động đúng
- [ ] Verify Order table: Không có records với customerName=NULL

---

## 🚀 Migration Notes

### Backward Compatibility:
- ⚠️ Code mới không tương thích với sessions cũ đã có `order_id`
- 💡 Giải pháp: Cleanup tất cả sessions cũ trước khi deploy

### Deployment Steps:
1. Backup database
2. Deploy code mới
3. Chạy cleanup script để xóa expired sessions cũ:
   ```sql
   DELETE FROM checkout_sessions 
   WHERE is_completed = false 
     AND expires_at < NOW();
   ```
4. Monitor logs để đảm bảo scheduled tasks chạy đúng

---

## 📌 Key Takeaways

1. ✅ **Tạo entity muộn hơn = sạch hơn**
   - Chỉ tạo Order khi có đầy đủ dữ liệu
   
2. ✅ **Staging table pattern**
   - CheckoutSession = temporary staging
   - Order = permanent result
   
3. ✅ **Separation of concerns**
   - Session tracks process
   - Order stores result
   
4. ✅ **Không xóa business data**
   - Order không nên bị DELETE
   - Chỉ xóa tracking/session data

---

**Refactored by:** AI Assistant  
**Reviewed by:** [Tên của bạn]  
**Date:** 21/01/2026
