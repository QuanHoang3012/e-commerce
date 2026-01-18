package org.project.ecommerce.constant;

public enum CartStatus {
    ACTIVE,      // Giỏ hàng đang hoạt động
    CHECKOUT,    // Đang trong quá trình thanh toán
    COMPLETED,   // Đã hoàn thành đơn hàng
    ABANDONED,   // Bị bỏ rơi
    CONVERTED    // Đã chuyển đổi thành đơn hàng
}
