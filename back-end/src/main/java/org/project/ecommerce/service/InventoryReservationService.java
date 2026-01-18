package org.project.ecommerce.service;

import java.util.List;
import java.util.UUID;

import org.project.ecommerce.entities.InventoryReservation;

/**
 * Service quản lý việc GIỮ HÀNG (Inventory Reservation)
 * Giải quyết vấn đề race condition khi nhiều người mua cùng lúc
 */
public interface InventoryReservationService {

    /**
     * Giữ hàng khi khách hàng bắt đầu checkout (từ giỏ hàng sang thanh toán)
     * Giữ trong 15 phút, sau đó tự động release
     *
     * @param variantId   ID của product variant cần giữ
     * @param quantity    Số lượng cần giữ
     * @param cartId      Cart ID từ cookie
     * @return InventoryReservation đã tạo
     * @throws CustomException nếu không đủ hàng
     */
    InventoryReservation reserveInventory(UUID variantId, Integer quantity, String cartId);

    /**
     * Giữ hàng cho nhiều variants cùng lúc (bulk reserve)
     * Sử dụng khi checkout cả giỏ hàng
     *
     * @param items     List các item cần reserve (variantId, quantity)
     * @param cartId    Cart ID từ cookie
     * @return List các reservation đã tạo
     */
    List<InventoryReservation> reserveMultipleItems(List<ReservationItem> items, String cartId);

    /**
     * Release (nhả) hàng đã giữ khi:
     * - Khách không thanh toán trong 15 phút
     * - Khách cancel đơn
     *
     * @param cartId Cart ID cần release
     */
    void releaseReservation(String cartId);

    /**
     * Complete reservation khi thanh toán thành công
     * Cập nhật status từ ACTIVE → COMPLETED
     * Trừ stock_quantity thật sự
     *
     * @param cartId Cart ID
     */
    void completeReservation(String sessionId);

    /**
     * Check số lượng hàng THỰC SỰ có thể bán
     * Available Stock = Physical Stock - Reserved Stock
     *
     * @param variantId ID của variant
     * @return Số lượng có thể bán
     */
    Integer getAvailableStock(UUID variantId);

    /**
     * Tự động release các reservation đã HẾT HẠN (> 15 phút)
     * Gọi bởi Scheduled Task
     *
     * @return Số lượng reservations đã được release
     */
    int releaseExpiredReservations();

    /**
     * Xóa các reservation đã EXPIRED/COMPLETED cũ (> 24 giờ)
     * Gọi bởi Scheduled Task để dọn dẹp database
     *
     * @return Số lượng records đã xóa
     */
    int cleanupOldReservations();

    /**
     * DTO cho bulk reservation
     */
    record ReservationItem(UUID variantId, Integer quantity) {
    }
}
