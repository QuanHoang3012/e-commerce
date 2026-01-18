package org.project.ecommerce.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class AddToCartRequest {
    private UUID variantId;
    private int quantity;
}
