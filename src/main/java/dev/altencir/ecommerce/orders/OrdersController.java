package dev.altencir.ecommerce.orders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
class OrdersController {
    private final OrderService orders;
    OrdersController(OrderService orders) { this.orders = orders; }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    OrderService.OrderView checkout(Authentication auth, @RequestHeader("Idempotency-Key") String key, @Valid @RequestBody Checkout request) {
        if (request.items().size() != 1) throw new IllegalArgumentException("This laboratory checkout accepts exactly one line");
        var item = request.items().getFirst();
        return orders.checkout(UUID.fromString(auth.getName()), key, item.productId(), item.quantity());
    }

    @GetMapping("/{orderId}")
    OrderService.OrderView get(Authentication auth, @PathVariable UUID orderId) {
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return orders.get(orderId, UUID.fromString(auth.getName()), admin);
    }

    record Checkout(@NotEmpty List<@Valid Item> items) {}
    record Item(@NotNull UUID productId, @Positive int quantity) {}
}
