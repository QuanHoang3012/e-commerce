package org.project.ecommerce.config;

import org.project.ecommerce.service.CheckoutService;
import org.project.ecommerce.service.InventoryReservationService;
import org.project.ecommerce.service.OrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled tasks để quản lý Inventory Reservations và Orders
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryReservationScheduler {

    private final InventoryReservationService reservationService;
    private final OrderService orderService;


    @Scheduled(fixedRate = 60000) // 1 phút = 60,000 ms
    public void releaseExpiredReservations() {
        try {
            int released = reservationService.releaseExpiredReservations();
            if (released > 0) {
                log.info("[SCHEDULER] Released {} expired inventory reservations", released);
            }
        } catch (Exception e) {
            log.error("[SCHEDULER] Error releasing expired reservations", e);
        }
    }


    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldReservations() {
        try {
            reservationService.cleanupOldReservations();
            log.info("[SCHEDULER] Cleanup old reservations completed");
        } catch (Exception e) {
            log.error("[SCHEDULER] Error cleaning up old reservations", e);
        }
    }

    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredOrders() {
        try {
            int cleaned = orderService.cleanupExpiredOrders();
            if (cleaned > 0) {
                log.info("[SCHEDULER] Cleaned up {} expired PENDING orders", cleaned);
            }
        } catch (Exception e) {
            log.error("[SCHEDULER] Error cleaning up expired orders", e);
        }
    }
}
