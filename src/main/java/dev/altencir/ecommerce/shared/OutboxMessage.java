package dev.altencir.ecommerce.shared;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_messages", schema = "shared")
class OutboxMessage {
    @Id UUID id;
    @Column(nullable = false) String eventType;
    @Column(nullable = false, length = 10000) String payload;
    @Column(nullable = false) Instant occurredAt;
    Instant publishedAt;
    protected OutboxMessage() {}
    OutboxMessage(UUID id, String eventType, String payload) {
        this.id = id; this.eventType = eventType; this.payload = payload; this.occurredAt = Instant.now();
    }
    void published() { this.publishedAt = Instant.now(); }
}

interface OutboxRepository extends org.springframework.data.jpa.repository.JpaRepository<OutboxMessage, UUID> {
    java.util.List<OutboxMessage> findTop50ByPublishedAtIsNullOrderByOccurredAtAsc();
}
