package dev.altencir.ecommerce.catalog;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface CatalogQuery {
    Optional<ProductView> find(UUID id);
    record ProductView(UUID id, String sku, String name, BigDecimal price, boolean active) implements Serializable {}
}
