package dev.altencir.ecommerce.payments;

import dev.altencir.ecommerce.shared.WebSupport;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
class PaymentsController {
    private final PaymentRepository payments;
    PaymentsController(PaymentRepository payments) { this.payments = payments; }
    @GetMapping("/{orderId}") PaymentView get(@PathVariable UUID orderId) {
        var payment = payments.findByOrderId(orderId).orElseThrow(() -> new WebSupport.NotFound("Payment not found"));
        return new PaymentView(payment.id, payment.orderId, payment.amount, payment.status, payment.processedAt);
    }
    record PaymentView(UUID id, UUID orderId, BigDecimal amount, String status, Instant processedAt) {}
}
