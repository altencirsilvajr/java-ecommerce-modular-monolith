package dev.altencir.ecommerce.orders;

import dev.altencir.ecommerce.shared.CheckoutEvents;
import dev.altencir.ecommerce.shared.ReliableEvents;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
class OrderWorkflow {
    private final OrderService orders; private final ReliableEvents reliable;
    OrderWorkflow(OrderService orders, ReliableEvents reliable) { this.orders = orders; this.reliable = reliable; }

    @Async @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void reserved(CheckoutEvents.StockReserved event) {
        if (reliable.first(event.eventId(), "orders.stock-reserved")) orders.stockReserved(event.orderId());
    }

    @Async @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void rejected(CheckoutEvents.StockRejected event) {
        if (reliable.first(event.eventId(), "orders.stock-rejected")) orders.cancel(event.orderId(), event.reason());
    }

    @Async @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void approved(CheckoutEvents.PaymentApproved event) {
        if (reliable.first(event.eventId(), "orders.payment-approved")) orders.confirm(event.orderId());
    }

    @Async @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void paymentRejected(CheckoutEvents.PaymentRejected event) {
        if (reliable.first(event.eventId(), "orders.payment-rejected")) orders.cancel(event.orderId(), event.reason());
    }
}
