package dev.altencir.ecommerce.shared;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class CheckoutEvents {
    private CheckoutEvents() {}

    public record OrderPlaced(UUID eventId, UUID orderId, UUID customerId, UUID productId,
                              int quantity, BigDecimal total, Instant occurredAt) {}
    public record StockReserved(UUID eventId, UUID orderId, UUID productId,
                                int quantity, BigDecimal total, Instant occurredAt) {}
    public record StockRejected(UUID eventId, UUID orderId, String reason, Instant occurredAt) {}
    public record PaymentApproved(UUID eventId, UUID orderId, UUID productId,
                                  int quantity, Instant occurredAt) {}
    public record PaymentRejected(UUID eventId, UUID orderId, UUID productId,
                                  int quantity, String reason, Instant occurredAt) {}
}
