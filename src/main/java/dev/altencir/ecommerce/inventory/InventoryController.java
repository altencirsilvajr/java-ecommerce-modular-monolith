package dev.altencir.ecommerce.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory/products")
class InventoryController {
    private final InventoryService inventory;
    InventoryController(InventoryService inventory) { this.inventory = inventory; }
    @PostMapping("/{productId}/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    InventoryService.StockView adjust(@PathVariable UUID productId, @RequestHeader("Idempotency-Key") String key,
                                      @Valid @RequestBody Adjustment request) {
        return inventory.adjust(productId, request.quantity(), key);
    }
    record Adjustment(@NotNull Integer quantity) {}
}
