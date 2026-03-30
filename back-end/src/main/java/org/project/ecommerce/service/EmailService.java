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

    /**
     * Gửi email đơn giản (plain text)
     * @param to    Email người nhận
     * @param subject Tiêu đề
     * @param body    Nội dung
     */
    void sendSimpleEmail(String to, String subject, String body);
}
