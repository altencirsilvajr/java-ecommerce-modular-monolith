package dev.altencir.ecommerce.orders;

import dev.altencir.ecommerce.catalog.CatalogQuery;
import dev.altencir.ecommerce.shared.CheckoutEvents;
import dev.altencir.ecommerce.shared.ReliableEvents;
import dev.altencir.ecommerce.shared.WebSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
class OrderService {
    private final OrderRepository orders; private final OrderRequestRepository requests;
    private final CatalogQuery catalog; private final ReliableEvents events;
    OrderService(OrderRepository orders, OrderRequestRepository requests, CatalogQuery catalog, ReliableEvents events) {
        this.orders = orders; this.requests = requests; this.catalog = catalog; this.events = events;
    }

    @Transactional
    OrderView checkout(UUID customerId, String key, UUID productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        String hash = sha(productId + ":" + quantity);
        var previous = requests.findById(key);
        if (previous.isPresent()) {
            if (!previous.get().payloadHash.equals(hash)) throw new WebSupport.Conflict("Idempotency key reused with another payload");
            return view(orders.findById(previous.get().orderId).orElseThrow());
        }
        var product = catalog.find(productId).orElseThrow(() -> new WebSupport.NotFound("Product not found"));
        BigDecimal total = product.price().multiply(BigDecimal.valueOf(quantity));
        var order = orders.save(new PurchaseOrder(customerId, productId, product.name(), quantity, total));
        requests.save(new OrderRequestRecord(key, hash, order.id));
        UUID eventId = UUID.randomUUID();
        events.appendAndPublish(eventId, new CheckoutEvents.OrderPlaced(eventId, order.id, customerId, productId, quantity, total, Instant.now()));
        return view(order);
    }

    @Transactional(readOnly = true)
    OrderView get(UUID orderId, UUID requester, boolean admin) {
        var order = orders.findById(orderId).orElseThrow(() -> new WebSupport.NotFound("Order not found"));
        if (!admin && !order.customerId.equals(requester)) throw new WebSupport.Forbidden("Order belongs to another customer");
        return view(order);
    }

    @Transactional
    void stockReserved(UUID id) { orders.findById(id).ifPresent(PurchaseOrder::stockReserved); }
    @Transactional
    void confirm(UUID id) { orders.findById(id).ifPresent(PurchaseOrder::confirm); }
    @Transactional
    void cancel(UUID id, String reason) { orders.findById(id).ifPresent(order -> order.cancel(reason)); }

    private static String sha(String input) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }
    static OrderView view(PurchaseOrder o) { return new OrderView(o.id, o.customerId, o.productId, o.productName, o.quantity, o.total, o.status, o.failureReason, o.createdAt); }
    record OrderView(UUID id, UUID customerId, UUID productId, String productName, int quantity, BigDecimal total,
                     OrderStatus status, String failureReason, Instant createdAt) {}
}
