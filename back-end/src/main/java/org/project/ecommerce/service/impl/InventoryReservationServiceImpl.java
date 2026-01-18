package org.project.ecommerce.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.project.ecommerce.constant.ReservationStatus;
import org.project.ecommerce.entities.InventoryReservation;
import org.project.ecommerce.entities.ProductVariant;
import org.project.ecommerce.exception.CustomException;
import org.project.ecommerce.repository.InventoryReservationRepository;
import org.project.ecommerce.repository.ProductVariantRepository;
import org.project.ecommerce.service.InventoryReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryReservationServiceImpl implements InventoryReservationService {

    private final InventoryReservationRepository reservationRepository;
    private final ProductVariantRepository productVariantRepository;

    // Thời gian giữ hàng: 15 phút
    private static final int RESERVATION_EXPIRY_MINUTES = 6;

    @Override
    @Transactional
    public InventoryReservation reserveInventory(UUID variantId, Integer quantity, String cartId) {
        // 1. Lấy thông tin variant với PESSIMISTIC LOCK
        // 🔒 Lock row để tránh race condition khi nhiều người reserve cùng lúc
        ProductVariant variant = productVariantRepository.findByIdWithLock(variantId)
                .orElseThrow(() -> new CustomException("Sản phẩm không tồn tại", HttpStatus.NOT_FOUND.value()));

        // 2. Check xem cart này đã reserve variant này chưa
        var existingReservation = reservationRepository
                .findByVariantAndCartIdAndStatus(variant, cartId, ReservationStatus.ACTIVE);

        if (existingReservation.isPresent()) {
            // Nếu đã reserve rồi, update số lượng
            InventoryReservation reservation = existingReservation.get();
            int newQuantity = reservation.getQuantity() + quantity;

            // Check available stock
            int available = getAvailableStock(variantId);
            int currentReserved = reservation.getQuantity();
            int actualAvailable = available + currentReserved; // Cộng lại phần đã reserve của session này

            if (actualAvailable < newQuantity) {
                throw new CustomException(
                        "Chỉ còn " + actualAvailable + " sản phẩm có thể đặt (đã bao gồm của bạn)",
                        HttpStatus.BAD_REQUEST.value());
            }

            reservation.setQuantity(newQuantity);
            log.info("Updated reservation for cart {} - variant {} - quantity: {} → {}",
                    cartId, variantId, currentReserved, newQuantity);
            return reservationRepository.save(reservation);
        }

        // 3. Tạo reservation mới - Check available stock
        int available = getAvailableStock(variantId);
        if (available < quantity) {
            throw new CustomException(
                    "Rất tiếc! Chỉ còn " + available + " sản phẩm. Có người đang giữ hàng để thanh toán.",
                    HttpStatus.BAD_REQUEST.value());
        }

        // 4. Tạo reservation
        InventoryReservation reservation = InventoryReservation.builder()
                .variant(variant)
                .quantity(quantity)
                .cartId(cartId)
                .status(ReservationStatus.ACTIVE)
                .build();

        InventoryReservation saved = reservationRepository.save(reservation);
        log.info("Created reservation for cart {} - variant {} - quantity: {}",
                cartId, variantId, quantity);

        return saved;
    }

    @Override
    @Transactional
    public List<InventoryReservation> reserveMultipleItems(List<ReservationItem> items, String cartId) {
        List<InventoryReservation> reservations = new ArrayList<>();

        // Reserve từng item một
        for (ReservationItem item : items) {
            InventoryReservation reservation = reserveInventory(
                    item.variantId(),
                    item.quantity(),
                    cartId
            );
            reservations.add(reservation);
        }

        return reservations;
    }

    @Override
    @Transactional
    public void releaseReservation(String cartId) {
        List<InventoryReservation> reservations = reservationRepository
                .findByCartIdAndStatus(cartId, ReservationStatus.ACTIVE);

        if (reservations.isEmpty()) {
            log.info("No active reservations found for cart {}", cartId);
            return;
        }

        // Update status → EXPIRED
        reservations.forEach(r -> r.setStatus(ReservationStatus.EXPIRED));
        reservationRepository.saveAll(reservations);

        log.info("Released {} reservations for cart {}", reservations.size(), cartId);
    }

    @Override
    @Transactional
    public void completeReservation(String cartId) {
        List<InventoryReservation> reservations = reservationRepository
                .findByCartIdAndStatus(cartId, ReservationStatus.ACTIVE);

        if (reservations.isEmpty()) {
            throw new CustomException("Đơn hàng đã quá thời gian checkout", HttpStatus.NOT_FOUND.value());
        }

        // Update status → COMPLETED và trừ stock thật sự
        for (InventoryReservation reservation : reservations) {
            // 🔒 Lock variant khi trừ stock để tránh race condition
            ProductVariant variant = productVariantRepository.findByIdWithLock(reservation.getVariant().getId())
                    .orElseThrow(() -> new CustomException("Sản phẩm không tồn tại", HttpStatus.NOT_FOUND.value()));

            // Trừ stock quantity
            int newStock = variant.getStockQuantity() - reservation.getQuantity();
            if (newStock < 0) {
                // Không nên xảy ra nếu logic đúng, nhưng để safety check
                throw new CustomException(
                        "Lỗi hệ thống: Không đủ hàng để hoàn tất đơn",
                        HttpStatus.INTERNAL_SERVER_ERROR.value());
            }

            variant.setStockQuantity(newStock);
            productVariantRepository.save(variant);

            // Update reservation status
            reservation.setStatus(ReservationStatus.COMPLETED);
        }

        reservationRepository.saveAll(reservations);
        log.info("Completed {} reservations for cart {}", reservations.size(), cartId);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getAvailableStock(UUID variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new CustomException("Sản phẩm không tồn tại", HttpStatus.NOT_FOUND.value()));

        // Tính số lượng đã được reserve (ACTIVE)
        Integer reservedQuantity = reservationRepository.sumReservedQuantityByVariantId(variantId);

        // Available = Physical Stock - Reserved
        int available = variant.getStockQuantity() - (reservedQuantity != null ? reservedQuantity : 0);

        return Math.max(0, available); // Không trả về số âm
    }

    @Override
    @Transactional
    public int releaseExpiredReservations() {
        // Tính thời điểm hết hạn = hiện tại - 15 phút
        Instant expiryTime = Instant.now().minus(RESERVATION_EXPIRY_MINUTES, ChronoUnit.MINUTES);

        int count = reservationRepository.expireReservationsByTime(expiryTime);

        if (count > 0) {
            log.info("Auto-released {} expired reservations", count);
        }

        return count;
    }

    @Override
    @Transactional
    public int cleanupOldReservations() {
        // Xóa các reservation đã EXPIRED/COMPLETED và cũ hơn 24 giờ
        Instant cleanupTime = Instant.now().minus(24, ChronoUnit.HOURS);
        
        reservationRepository.cleanupOldReservations(cleanupTime);
        
        log.info("Cleaned up old reservations (EXPIRED/COMPLETED older than 24 hours)");
        
        return 0;
    }
}
