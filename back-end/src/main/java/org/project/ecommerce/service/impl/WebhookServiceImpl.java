package org.project.ecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.ecommerce.constant.OrderStatus;
import org.project.ecommerce.dto.request.SePayWebhookRequest;
import org.project.ecommerce.entities.Order;
import org.project.ecommerce.exception.CustomException;
import org.project.ecommerce.repository.OrderRepository;
import org.project.ecommerce.service.EmailService;
import org.project.ecommerce.service.WebhookService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookServiceImpl implements WebhookService {

    private final OrderRepository orderRepository;
    private final EmailService emailService;

    @Value("${sepay.webhook.secret}")
    private String webhookSecret;

    @Override
    @Transactional
    public void processSePayWebhook(SePayWebhookRequest webhook) {
        log.info("Processing SePay webhook - Transaction: {} - Content: {} - Amount: {}",
                webhook.getTransactionId(), webhook.getContent(), webhook.getAmount());

        // 1. Verify signature (nếu có)
        if (webhook.getSignature() != null && !webhook.getSignature().isEmpty()) {
            verifyWebhookSignature(webhook);
        }

        // 2. Parse tracking number từ content (loại bỏ khoảng trắng, chữ hoa/thường)
        String trackingNumber = extractTrackingNumber(webhook.getContent());
        
        if (trackingNumber == null || trackingNumber.isEmpty()) {
            log.error("Cannot extract tracking number from content: {}", webhook.getContent());
            throw new CustomException("Nội dung chuyển khoản không hợp lệ", HttpStatus.BAD_REQUEST.value());
        }

        // 3. Tìm đơn hàng
        Order order = orderRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> {
                    log.error("Order not found for tracking number: {}", trackingNumber);
                    return new CustomException(
                            "Không tìm thấy đơn hàng với mã: " + trackingNumber,
                            HttpStatus.NOT_FOUND.value()
                    );
                });

        // 4. Verify amount
        if (!isAmountMatching(order.getTotalAmount(), webhook.getAmount())) {
            log.error("Amount mismatch - Expected: {} - Received: {}",
                    order.getTotalAmount(), webhook.getAmount());
            throw new CustomException(
                    String.format("Số tiền không khớp. Cần: %s - Nhận được: %s",
                            order.getTotalAmount(), webhook.getAmount()),
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        // 5. Check current status
        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Order {} is not in PENDING status. Current: {}. Skipping update.",
                    trackingNumber, order.getStatus());
            // Không throw exception để webhook không bị retry
            return;
        }

        // 6. Update order status: PENDING → PAID
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        log.info("Order {} updated to PAID. Transaction: {}",
                trackingNumber, webhook.getTransactionId());

        // 7. Send email notification (optional)
        try {
            emailService.sendOrderConfirmation(order);
            log.info("Payment confirmation email sent for order {}", trackingNumber);
        } catch (Exception e) {
            log.error("Failed to send payment confirmation email: {}", e.getMessage());
            // Không fail transaction nếu email lỗi
        }
    }

    /**
     * Verify webhook signature using HMAC-SHA256
     */
    private void verifyWebhookSignature(SePayWebhookRequest webhook) {
        try {
            String payload = webhook.getTransactionId() +
                    webhook.getAmount().toPlainString() +
                    webhook.getContent() +
                    webhook.getTimestamp().toString();

            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            hmac.init(secretKey);
            
            byte[] hash = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = Base64.getEncoder().encodeToString(hash);

            if (!calculatedSignature.equals(webhook.getSignature())) {
                log.error("Webhook signature verification failed");
                throw new CustomException(
                        "Webhook signature không hợp lệ",
                        HttpStatus.UNAUTHORIZED.value()
                );
            }
        } catch (Exception e) {
            log.error("Error verifying webhook signature: {}", e.getMessage());
            throw new CustomException(
                    "Lỗi xác thực webhook",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    /**
     * Extract tracking number từ nội dung chuyển khoản
     * Ví dụ: "ABC-123-456" hoặc "thanh toan ABC-123-456" → "ABC-123-456"
     */
    private String extractTrackingNumber(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }

        // Remove extra spaces and convert to uppercase
        String normalized = content.trim().toUpperCase();

        // Pattern: ABC-123-456 (UUID-like format)
        // Simple extraction: find first UUID-like pattern
        String[] parts = normalized.split("\\s+");
        for (String part : parts) {
            // Check if part looks like a tracking number (contains hyphens and alphanumeric)
            if (part.matches("[A-Z0-9]{3,}-[A-Z0-9]{3,}-[A-Z0-9]{3,}.*")) {
                return part.split("[^A-Z0-9-]")[0]; // Get the first valid tracking number
            }
        }

        // If no pattern found, return the whole content (assume it's the tracking number)
        return normalized.replaceAll("[^A-Z0-9-]", "");
    }

    /**
     * So sánh số tiền (cho phép sai lệch nhỏ do làm tròn)
     */
    private boolean isAmountMatching(BigDecimal expected, BigDecimal received) {
        if (expected == null || received == null) {
            return false;
        }
        
        // Allow 1 VND difference for rounding
        BigDecimal diff = expected.subtract(received).abs();
        return diff.compareTo(BigDecimal.ONE) <= 0;
    }
}
