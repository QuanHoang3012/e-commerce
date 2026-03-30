package org.project.ecommerce.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponse {

    // Bước 1 & 2: Đều trả về Order ID
    private UUID orderId;
    
    // Bước 1: Thông tin reservation
    private Instant reservedUntil;
    private Integer totalItems;
    private BigDecimal totalAmount;

    // Bước 2: Confirm Checkout message
    private String message;
}
