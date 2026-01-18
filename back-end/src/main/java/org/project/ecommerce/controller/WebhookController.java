package org.project.ecommerce.controller;

import org.project.ecommerce.base.BaseController;
import org.project.ecommerce.base.BaseResponse;
import org.project.ecommerce.dto.request.SePayWebhookRequest;
import org.project.ecommerce.service.WebhookService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller nhận webhook từ payment gateway (SePay)
 * PUBLIC endpoint - không cần authentication
 */
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhook", description = "API webhook - Nhận callback từ cổng thanh toán")
public class WebhookController extends BaseController {

    private final WebhookService webhookService;

    /**
     * Nhận webhook từ SePay khi khách chuyển khoản thành công
     * 
     * Example payload:
     * {
     *   "transactionId": "TX123456789",
     *   "amount": 1500000,
     *   "content": "ABC-123-456",
     *   "timestamp": "2026-01-15T10:30:00Z",
     *   "bankCode": "MB",
     *   "senderName": "NGUYEN VAN A",
     *   "signature": "base64-encoded-hmac"
     * }
     */
    @PostMapping("/sepay")
    public BaseResponse<Void> handleSePayWebhook(@Valid @RequestBody SePayWebhookRequest webhook) {
        log.info("Received SePay webhook - Transaction: {} - Content: {}",
                webhook.getTransactionId(), webhook.getContent());

        webhookService.processSePayWebhook(webhook);

        log.info("SePay webhook processed successfully - Transaction: {}",
                webhook.getTransactionId());

        return wrapSuccess(null);
    }
}
