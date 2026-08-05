package dev.altencir.ecommerce.catalog;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog/products")
class CatalogController {
    private final CatalogService catalog;
    CatalogController(CatalogService catalog) { this.catalog = catalog; }

    @GetMapping List<CatalogQuery.ProductView> list() { return catalog.list(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    CatalogQuery.ProductView create(@Valid @RequestBody CreateProduct request) { return catalog.create(request.sku(), request.name(), request.price()); }

    record CreateProduct(@NotBlank String sku, @NotBlank String name, @NotNull @DecimalMin("0.01") BigDecimal price) {}
}
