package org.project.ecommerce.entities;

import jakarta.persistence.*;
import lombok.*;
import org.project.ecommerce.base.BaseEntity;
import org.project.ecommerce.constant.OrderStatus;
import org.project.ecommerce.constant.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Order extends BaseEntity {
    @Column(name = "tracking_number", unique = true)
    private String trackingNumber;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    @PrePersist
    protected void onCreate() {
        if (this.trackingNumber == null) {
            this.trackingNumber = UUID.randomUUID().toString();
        }
    }
}
