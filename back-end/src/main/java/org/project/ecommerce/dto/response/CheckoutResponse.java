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

    // Bước 1: Initiate Checkout
    private String sessionId;
    private Instant reservedUntil;
    private Integer totalItems;
    private BigDecimal totalAmount;

    // Bước 2: Confirm Checkout → Trả về Order
    private UUID orderId;
    private String message;
}
