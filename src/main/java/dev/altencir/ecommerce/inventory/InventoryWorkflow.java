package dev.altencir.ecommerce.inventory;

import dev.altencir.ecommerce.shared.CheckoutEvents;
import dev.altencir.ecommerce.shared.ReliableEvents;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import java.time.Instant;
import java.util.UUID;

@Component
class InventoryWorkflow {
    private final StockRepository stocks; private final ReliableEvents events;
    InventoryWorkflow(StockRepository stocks, ReliableEvents events) { this.stocks = stocks; this.events = events; }

    @Async @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void reserve(CheckoutEvents.OrderPlaced event) {
        if (!events.first(event.eventId(), "inventory.order-placed")) return;
        var stock = stocks.lockById(event.productId()).orElseGet(() -> new StockItem(event.productId()));
        UUID nextId = UUID.randomUUID();
        if (stock.reserve(event.quantity())) {
            stocks.save(stock);
            events.appendAndPublish(nextId, new CheckoutEvents.StockReserved(nextId, event.orderId(), event.productId(), event.quantity(), event.total(), Instant.now()));
        } else {
            events.appendAndPublish(nextId, new CheckoutEvents.StockRejected(nextId, event.orderId(), "INSUFFICIENT_STOCK", Instant.now()));
        }
    }

    @Async @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void release(CheckoutEvents.PaymentRejected event) {
        if (!events.first(event.eventId(), "inventory.payment-rejected")) return;
        stocks.lockById(event.productId()).ifPresent(stock -> stock.release(event.quantity()));
    }

    @Async @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void commit(CheckoutEvents.PaymentApproved event) {
        if (!events.first(event.eventId(), "inventory.payment-approved")) return;
        stocks.lockById(event.productId()).ifPresent(stock -> stock.commit(event.quantity()));
    }
}
