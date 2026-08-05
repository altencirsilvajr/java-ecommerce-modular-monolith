package dev.altencir.ecommerce.payments;

import dev.altencir.ecommerce.shared.CheckoutEvents;
import dev.altencir.ecommerce.shared.ReliableEvents;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
class PaymentWorkflow {
    private static final BigDecimal REJECTION_LIMIT = new BigDecimal("5000.00");
    private final PaymentRepository payments; private final ReliableEvents events;
    PaymentWorkflow(PaymentRepository payments, ReliableEvents events) { this.payments = payments; this.events = events; }

    @Async @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void process(CheckoutEvents.StockReserved event) {
        if (!events.first(event.eventId(), "payments.stock-reserved")) return;
        boolean approved = event.total().compareTo(REJECTION_LIMIT) < 0;
        payments.save(new Payment(event.orderId(), event.total(), approved ? "APPROVED" : "REJECTED"));
        UUID nextId = UUID.randomUUID();
        if (approved) events.appendAndPublish(nextId, new CheckoutEvents.PaymentApproved(nextId, event.orderId(), event.productId(), event.quantity(), Instant.now()));
        else events.appendAndPublish(nextId, new CheckoutEvents.PaymentRejected(nextId, event.orderId(), event.productId(), event.quantity(), "PAYMENT_REJECTED", Instant.now()));
    }
}
