package org.project.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO nhận webhook từ SePay khi khách chuyển khoản
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SePayWebhookRequest {
    
    /**
     * Transaction ID từ SePay
     */
    @NotBlank(message = "Transaction ID không được để trống")
    private String transactionId;
    
    /**
     * Số tiền thực tế nhận được
     */
    @NotNull(message = "Số tiền không được để trống")
    private BigDecimal amount;
    
    /**
     * Nội dung chuyển khoản (tracking number)
     */
    @NotBlank(message = "Nội dung chuyển khoản không được để trống")
    private String content;
    
    /**
     * Thời gian giao dịch
     */
    @NotNull(message = "Timestamp không được để trống")
    private Instant timestamp;
    
    /**
     * Mã ngân hàng (VCB, MB, ACB, etc.)
     */
    private String bankCode;
    
    /**
     * Tên người chuyển khoản
     */
    private String senderName;
    
    /**
     * Số tài khoản người chuyển
     */
    private String senderAccount;
    
    /**
     * Signature để verify webhook (HMAC-SHA256)
     */
    private String signature;
}
