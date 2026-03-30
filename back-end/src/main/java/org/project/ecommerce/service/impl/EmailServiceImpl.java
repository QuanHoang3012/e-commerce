package org.project.ecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.ecommerce.constant.PaymentMethod;
import org.project.ecommerce.entities.Order;
import org.project.ecommerce.entities.OrderItem;
import org.project.ecommerce.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Value("${spring.mail.from:noreply@yourshop.com}")
    private String fromEmail;
    
    @Value("${sepay.bank.name:MB Bank}")
    private String bankName;
    
    @Value("${sepay.bank.account}")
    private String bankAccount;
    
    @Value("${sepay.account.name}")
    private String accountName;

    @Override
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Simple email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to {}: {}", to, e.getMessage());
        }
    }

    @Override
    public void sendOrderConfirmation(Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(order.getCustomerEmail());
            message.setSubject("Đơn hàng #" + order.getTrackingNumber() + " đã được xác nhận");
            message.setText(buildEmailContent(order));
            
            mailSender.send(message);
            log.info("Email confirmation sent to {} for order {}", order.getCustomerEmail(), order.getTrackingNumber());
        } catch (Exception e) {
            log.error("Failed to send email to {} for order {}: {}", 
                order.getCustomerEmail(), order.getTrackingNumber(), e.getMessage());
            // Không throw exception để không làm fail toàn bộ checkout process
        }
    }

    private String buildEmailContent(Order order) {
        StringBuilder content = new StringBuilder();
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        // Header
        content.append("Xin chào ").append(order.getCustomerName()).append(",\n\n");
        content.append("Cảm ơn bạn đã đặt hàng tại shop!\n\n");
        
        // Order info
        content.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        content.append("📦 THÔNG TIN ĐỔN HÀNG\n");
        content.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        content.append("Mã đơn hàng: ").append(order.getTrackingNumber()).append("\n");
        content.append("Ngày đặt: ").append(
            order.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).format(dateFormat)
        ).append("\n");
        content.append("Tổng tiền: ").append(currencyFormat.format(order.getTotalAmount())).append("\n");
        content.append("Địa chỉ giao: ").append(order.getShippingAddress()).append("\n");
        content.append("Số điện thoại: ").append(order.getCustomerPhone()).append("\n\n");
        
        // Items
        content.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        content.append("📋 CHI TIẾT SẢN PHẨM\n");
        content.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        
        for (OrderItem item : order.getItems()) {
            content.append("• ").append(item.getVariant().getProduct().getName()).append("\n");
            content.append("  Số lượng: ").append(item.getQuantity());
            content.append(" x ").append(currencyFormat.format(item.getPriceAtPurchase()));
            content.append(" = ").append(currencyFormat.format(item.getSubtotal())).append("\n\n");
        }
        
        // Payment specific instructions
        content.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        if (order.getPaymentMethod() == PaymentMethod.COD) {
            content.append("💵 THANH TOÁN KHI NHẬN HÀNG (COD)\n");
            content.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
            content.append("Đơn hàng sẽ được giao trong 2-3 ngày.\n");
            content.append("Vui lòng chuẩn bị tiền mặt: ").append(currencyFormat.format(order.getTotalAmount())).append("\n\n");
        } else if (order.getPaymentMethod() == PaymentMethod.BANK_TRANSFER) {
            content.append("🏦 THANH TOÁN CHUYỂN KHOẢN\n");
            content.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
            content.append("Vui lòng chuyển khoản theo thông tin sau:\n\n");
            content.append("Ngân hàng: ").append(bankName).append("\n");
            content.append("Số tài khoản: ").append(bankAccount).append("\n");
            content.append("Tên tài khoản: ").append(accountName).append("\n");
            content.append("Số tiền: ").append(currencyFormat.format(order.getTotalAmount())).append("\n");
            content.append("Nội dung: ").append(order.getTrackingNumber()).append("\n\n");
            content.append("⚠️ LƯU Ý: Nhập đúng nội dung để hệ thống tự động xác nhận thanh toán!\n\n");
            content.append("Đơn hàng sẽ được xử lý ngay sau khi chúng tôi nhận được thanh toán.\n\n");
        }
        
        // Tracking link
        content.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        content.append("🔍 THEO DÕI ĐƠN HÀNG\n");
        content.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        content.append("Bấm vào link sau để theo dõi đơn hàng:\n");
        content.append("👉 ").append(baseUrl).append("/track/").append(order.getTrackingNumber()).append("\n\n");
        content.append("(Không cần đăng nhập, chỉ cần bấm link là xem được!)\n\n");
        
        // Footer
        content.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        content.append("Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ:\n");
        content.append("📞 Hotline: 1900-xxxx\n");
        content.append("📧 Email: support@yourshop.com\n\n");
        content.append("Trân trọng,\n");
        content.append("Shop Team\n");
        
        return content.toString();
    }
}
