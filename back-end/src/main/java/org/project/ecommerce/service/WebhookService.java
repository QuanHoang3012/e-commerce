package org.project.ecommerce.service;

import org.project.ecommerce.dto.request.SePayWebhookRequest;

/**
 * Service xử lý webhook từ SePay
 */
public interface WebhookService {
    
    /**
     * Xử lý webhook từ SePay khi nhận được thanh toán
     * @param webhook Thông tin webhook từ SePay
     */
    void processSePayWebhook(SePayWebhookRequest webhook);
}
