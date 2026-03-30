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
     * @param orderId     Order ID
     * @return InventoryReservation đã tạo
     * @throws CustomException nếu không đủ hàng
     */
    InventoryReservation reserveInventory(UUID variantId, Integer quantity, UUID orderId);

    /**
     * Giữ hàng cho nhiều variants cùng lúc (bulk reserve)
     * Sử dụng khi checkout cả giỏ hàng
     *
     * @param items     List các item cần reserve (variantId, quantity)
     * @param orderId   Order ID
     * @return List các reservation đã tạo
     */
    List<InventoryReservation> reserveMultipleItems(List<ReservationItem> items, UUID orderId);

    /**
     * Release (nhả) hàng đã giữ khi:
     * - Khách không thanh toán trong 15 phút
     * - Khách cancel đơn
     *
     * @param orderId Order ID cần release
     */
    void releaseReservation(UUID orderId);

    /**
     * Xóa các reservation của một order
     * Dùng khi cancel checkout hoặc cleanup expired orders
     *
     * @param orderId Order ID cần xóa reservations
     */
    void deleteReservationsByOrder(UUID orderId);

    /**
     * Complete reservation khi thanh toán thành công
     * Cập nhật status từ ACTIVE → COMPLETED
     * Trừ stock_quantity thật sự
     *
     * @param orderId Order ID
     */
    void completeReservation(UUID orderId);

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
