package org.project.ecommerce.service;

import org.project.ecommerce.entities.Order;

/**
 * Service để gửi email thông báo cho khách hàng
 */
public interface EmailService {
    
    /**
     * Gửi email xác nhận đơn hàng
     * @param order Đơn hàng vừa được tạo
     */
    void sendOrderConfirmation(Order order);
}
