package org.project.ecommerce.utils;

import org.project.ecommerce.constant.OrderStatus;
import org.project.ecommerce.constant.PaymentMethod;
import org.project.ecommerce.entities.Order;

public class OrderUtils {

    /**
     * Xác định trạng thái ban đầu của đơn hàng dựa vào payment method
     */
    public static OrderStatus determineInitialStatus(PaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case COD -> OrderStatus.CONFIRMED; // COD → tự động confirmed, chờ giao hàng
            case BANK_TRANSFER -> OrderStatus.PENDING; // Chuyển khoản → chờ thanh toán
        };
    }

    /**
     * Tạo message phù hợp với payment method
     */
    public static String buildSuccessMessage(Order order, PaymentMethod paymentMethod) {
        String trackingLink = "https://yoursite.com/track/" + order.getId();

        return switch (paymentMethod) {
            case COD -> String.format(
                    "Đặt hàng thành công! Mã đơn hàng: %s. " +
                            "Đơn hàng sẽ được giao trong 2-3 ngày. " +
                            "Theo dõi tại: %s",
                    order.getId(), trackingLink);
            case BANK_TRANSFER -> String.format(
                    "Đặt hàng thành công! Mã đơn hàng: %s. " +
                            "Vui lòng chuyển khoản %s VNĐ với nội dung: %s. " +
                            "Đơn hàng sẽ được xử lý sau khi nhận được thanh toán. " +
                            "Theo dõi tại: %s",
                    order.getId(),
                    order.getTotalAmount(),
                    order.getId(),
                    trackingLink);
        };
    }
}
