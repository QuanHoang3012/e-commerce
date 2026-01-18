package org.project.ecommerce.entities;

import org.project.ecommerce.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.project.ecommerce.constant.PaymentMethod;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_transactions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PaymentTransaction extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "transaction_code", unique = true)
    private String transactionCode;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(columnDefinition = "TEXT")
    private String transactionContent;

    private String status;
}
