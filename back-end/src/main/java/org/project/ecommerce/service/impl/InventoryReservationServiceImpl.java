package org.project.ecommerce.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.project.ecommerce.constant.OrderStatus;
import org.project.ecommerce.constant.ReservationStatus;
import org.project.ecommerce.entities.InventoryReservation;
import org.project.ecommerce.entities.Order;
import org.project.ecommerce.entities.ProductVariant;
import org.project.ecommerce.exception.CustomException;
import org.project.ecommerce.repository.InventoryReservationRepository;
import org.project.ecommerce.repository.OrderRepository;
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
    private final OrderRepository orderRepository;

    private static final int RESERVATION_EXPIRY_MINUTES = 15;

    @Override
    @Transactional
    public InventoryReservation reserveInventory(UUID variantId, Integer quantity, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Đơn hàng không tồn tại", HttpStatus.NOT_FOUND.value()));

        ProductVariant variant = productVariantRepository.findByIdWithLock(variantId)
                .orElseThrow(() -> new CustomException("Sản phẩm không tồn tại", HttpStatus.NOT_FOUND.value()));

        var existingReservation = reservationRepository
                .findByVariantAndOrderAndStatus(variant, order, ReservationStatus.ACTIVE);

        if (existingReservation.isPresent()) {
            InventoryReservation reservation = existingReservation.get();
            int newQuantity = reservation.getQuantity() + quantity;

            int available = getAvailableStock(variantId);
            int currentReserved = reservation.getQuantity();
            int actualAvailable = available + currentReserved; // Cộng lại phần đã reserve của order này

            if (actualAvailable < newQuantity) {
                throw new CustomException(
                        "Chỉ còn " + actualAvailable + " sản phẩm có thể đặt (đã bao gồm của bạn)",
                        HttpStatus.BAD_REQUEST.value());
            }

            reservation.setQuantity(newQuantity);
            log.info("Updated reservation for order {} - variant {} - quantity: {} → {}",
                    orderId, variantId, currentReserved, newQuantity);
            return reservationRepository.save(reservation);
        }

        int available = getAvailableStock(variantId);
        if (available < quantity) {
            throw new CustomException(
                    "Rất tiếc! Chỉ còn " + available + " sản phẩm. Có người đang giữ hàng để thanh toán.",
                    HttpStatus.BAD_REQUEST.value());
        }

        InventoryReservation reservation = InventoryReservation.builder()
                .variant(variant)
                .quantity(quantity)
                .order(order)
                .status(ReservationStatus.ACTIVE)
                .build();

        InventoryReservation saved = reservationRepository.save(reservation);
        log.info("Created reservation for order {} - variant {} - quantity: {}",
                orderId, variantId, quantity);

        return saved;
    }

    @Override
    @Transactional
    public List<InventoryReservation> reserveMultipleItems(List<ReservationItem> items, UUID orderId) {
        List<InventoryReservation> reservations = new ArrayList<>();

        for (ReservationItem item : items) {
            InventoryReservation reservation = reserveInventory(
                    item.variantId(),
                    item.quantity(),
                    orderId
            );
            reservations.add(reservation);
        }

        return reservations;
    }

    @Override
    @Transactional
    public void releaseReservation(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Đơn hàng không tồn tại", HttpStatus.NOT_FOUND.value()));

        List<InventoryReservation> reservations = reservationRepository
                .findByOrderAndStatus(order, ReservationStatus.ACTIVE);

        if (reservations.isEmpty()) {
            log.info("No active reservations found for order {}", orderId);
            return;
        }

        reservations.forEach(r -> r.setStatus(ReservationStatus.EXPIRED));
        reservationRepository.saveAll(reservations);

        log.info("Released {} reservations for order {}", reservations.size(), orderId);
    }

    @Override
    @Transactional
    public void deleteReservationsByOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Đơn hàng không tồn tại", HttpStatus.NOT_FOUND.value()));

        List<InventoryReservation> reservations = reservationRepository.findByOrder(order);

        if (!reservations.isEmpty()) {
            reservationRepository.deleteAll(reservations);
            log.info("Deleted {} reservations for order {}", reservations.size(), orderId);
        }
    }

    @Override
    @Transactional
    public void completeReservation(UUID orderId) {
        // Tìm Order entity
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Đơn hàng không tồn tại", HttpStatus.NOT_FOUND.value()));

        List<InventoryReservation> reservations = reservationRepository
                .findByOrderAndStatus(order, ReservationStatus.ACTIVE);

        if (reservations.isEmpty()) {
            throw new CustomException("Đơn hàng đã quá thời gian checkout", HttpStatus.NOT_FOUND.value());
        }

        for (InventoryReservation reservation : reservations) {
            ProductVariant variant = productVariantRepository.findByIdWithLock(reservation.getVariant().getId())
                    .orElseThrow(() -> new CustomException("Sản phẩm không tồn tại", HttpStatus.NOT_FOUND.value()));

            int newStock = variant.getStockQuantity() - reservation.getQuantity();
            if (newStock < 0) {
                throw new CustomException(
                        "Lỗi hệ thống: Không đủ hàng để hoàn tất đơn",
                        HttpStatus.INTERNAL_SERVER_ERROR.value());
            }

            variant.setStockQuantity(newStock);
            productVariantRepository.save(variant);

            reservation.setStatus(ReservationStatus.COMPLETED);
        }

        reservationRepository.saveAll(reservations);
        log.info("Completed {} reservations for order {}", reservations.size(), orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getAvailableStock(UUID variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new CustomException("Sản phẩm không tồn tại", HttpStatus.NOT_FOUND.value()));

        Integer reservedQuantity = reservationRepository.sumReservedQuantityByVariantId(variantId);

        int available = variant.getStockQuantity() - (reservedQuantity != null ? reservedQuantity : 0);

        return Math.max(0, available);
    }

    @Override
    @Transactional
    public int releaseExpiredReservations() {
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
        Instant cleanupTime = Instant.now().minus(24, ChronoUnit.HOURS);
        
        reservationRepository.cleanupOldReservations(cleanupTime);
        
        log.info("Cleaned up old reservations (EXPIRED/COMPLETED older than 24 hours)");
        
        return 0;
    }

}
