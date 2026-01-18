package org.project.ecommerce.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.ecommerce.service.InventoryReservationService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks để quản lý Inventory Reservations
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class InventoryReservationScheduler {

    private final InventoryReservationService reservationService;

    /**
     * Tự động release các reservations đã HẾT HẠN (> 15 phút)
     * Chạy mỗi 2 phút
     */
    @Scheduled(fixedRate = 120000) // 2 phút = 120,000 ms
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

    /**
     * Cleanup các reservation đã EXPIRED/COMPLETED cũ (> 24 giờ)
     * Chạy mỗi ngày lúc 3:00 AM
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldReservations() {
        try {
            // TODO: Implement cleanup logic in service
            log.info("[SCHEDULER] Cleanup old reservations executed");
        } catch (Exception e) {
            log.error("[SCHEDULER] Error cleaning up old reservations", e);
        }
    }
}
