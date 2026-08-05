package dev.altencir.ecommerce.orders;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "purchase_orders", schema = "orders")
class PurchaseOrder {
    @Id UUID id;
    @Column(nullable = false) UUID customerId;
    @Column(nullable = false) UUID productId;
    @Column(nullable = false) String productName;
    @Column(nullable = false) int quantity;
    @Column(nullable = false, precision = 19, scale = 2) BigDecimal total;
    @Enumerated(EnumType.STRING) @Column(nullable = false) OrderStatus status;
    String failureReason;
    @Column(nullable = false) Instant createdAt;
    @Version long version;
    protected PurchaseOrder() {}
    PurchaseOrder(UUID customerId, UUID productId, String name, int quantity, BigDecimal total) {
        this.id = UUID.randomUUID(); this.customerId = customerId; this.productId = productId; this.productName = name;
        this.quantity = quantity; this.total = total; this.status = OrderStatus.AWAITING_STOCK; this.createdAt = Instant.now();
    }
    void stockReserved() { if (status == OrderStatus.AWAITING_STOCK) status = OrderStatus.AWAITING_PAYMENT; }
    void confirm() { if (status == OrderStatus.AWAITING_PAYMENT) status = OrderStatus.CONFIRMED; }
    void cancel(String reason) { if (status != OrderStatus.CONFIRMED) { status = OrderStatus.CANCELLED; failureReason = reason; } }
}

enum OrderStatus { AWAITING_STOCK, AWAITING_PAYMENT, CONFIRMED, CANCELLED }

interface OrderRepository extends org.springframework.data.jpa.repository.JpaRepository<PurchaseOrder, UUID> {}

@Entity
@Table(name = "idempotency_requests", schema = "orders")
class OrderRequestRecord {
    @Id String idempotencyKey;
    @Column(nullable = false) String payloadHash;
    @Column(nullable = false) UUID orderId;
    protected OrderRequestRecord() {}
    OrderRequestRecord(String key, String hash, UUID orderId) { this.idempotencyKey = key; this.payloadHash = hash; this.orderId = orderId; }
}

interface OrderRequestRepository extends org.springframework.data.jpa.repository.JpaRepository<OrderRequestRecord, String> {}
