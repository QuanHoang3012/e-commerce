package org.project.ecommerce.constant;

public enum OrderStatus {
    PENDING,           // Chờ xác nhận (COD) hoặc chờ thanh toán (BANK_TRANSFER)
    CONFIRMED,         // Đã xác nhận đơn hàng
    PAID,              // Đã thanh toán (chỉ cho BANK_TRANSFER)
    PROCESSING,        // Đang chuẩn bị hàng
    SHIPPING,          // Đang giao hàng
    DELIVERED,         // Đã giao hàng
    COMPLETED,         // Hoàn thành (đã nhận tiền)
    CANCELLED          // Đã hủy
}
