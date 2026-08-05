package dev.altencir.ecommerce.inventory;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "stock_items", schema = "inventory")
class StockItem {
    @Id UUID productId;
    @Column(nullable = false) int available;
    @Column(nullable = false) int reserved;
    @Version long version;
    protected StockItem() {}
    StockItem(UUID id) { this.productId = id; }
    void adjust(int quantity) {
        if (available + quantity < 0) throw new IllegalArgumentException("Available stock cannot be negative");
        available += quantity;
    }
    boolean reserve(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (available < quantity) return false;
        available -= quantity; reserved += quantity; return true;
    }
    void release(int quantity) { reserved -= quantity; available += quantity; }
    void commit(int quantity) { reserved -= quantity; }
}

interface StockRepository extends org.springframework.data.jpa.repository.JpaRepository<StockItem, UUID> {
    @org.springframework.data.jpa.repository.Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select s from StockItem s where s.productId = :id")
    java.util.Optional<StockItem> lockById(UUID id);
}

@Entity
@Table(name = "adjustment_requests", schema = "inventory")
class AdjustmentRequest {
    @Id String idempotencyKey;
    @Column(nullable = false) String payloadHash;
    @Column(nullable = false) UUID productId;
    protected AdjustmentRequest() {}
    AdjustmentRequest(String key, String hash, UUID productId) { this.idempotencyKey = key; this.payloadHash = hash; this.productId = productId; }
}

interface AdjustmentRepository extends org.springframework.data.jpa.repository.JpaRepository<AdjustmentRequest, String> {}
