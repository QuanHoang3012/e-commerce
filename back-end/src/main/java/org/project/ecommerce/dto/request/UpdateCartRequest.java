package org.project.ecommerce.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdateCartRequest {
    private UUID itemId;
    private int quantity; // Số lượng mới
}
