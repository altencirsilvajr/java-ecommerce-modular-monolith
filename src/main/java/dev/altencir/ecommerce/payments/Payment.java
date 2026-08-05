package dev.altencir.ecommerce.payments;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments", schema = "payments", uniqueConstraints = @UniqueConstraint(columnNames = "orderId"))
class Payment {
    @Id UUID id;
    @Column(nullable = false) UUID orderId;
    @Column(nullable = false, precision = 19, scale = 2) BigDecimal amount;
    @Column(nullable = false) String status;
    @Column(nullable = false) Instant processedAt;
    protected Payment() {}
    Payment(UUID orderId, BigDecimal amount, String status) {
        this.id = UUID.randomUUID(); this.orderId = orderId; this.amount = amount; this.status = status; this.processedAt = Instant.now();
    }
}

interface PaymentRepository extends org.springframework.data.jpa.repository.JpaRepository<Payment, UUID> {
    java.util.Optional<Payment> findByOrderId(UUID orderId);
}
