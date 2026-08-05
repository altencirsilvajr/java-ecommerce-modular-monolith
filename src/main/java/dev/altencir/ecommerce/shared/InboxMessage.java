package dev.altencir.ecommerce.shared;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "inbox_messages", schema = "shared")
class InboxMessage {
    @Id String id;
    @Column(nullable = false) Instant processedAt;
    protected InboxMessage() {}
    InboxMessage(String id) { this.id = id; this.processedAt = Instant.now(); }
}

interface InboxRepository extends org.springframework.data.jpa.repository.JpaRepository<InboxMessage, String> {}
