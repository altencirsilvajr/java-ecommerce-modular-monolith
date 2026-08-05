package dev.altencir.ecommerce.catalog;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products", schema = "catalog", uniqueConstraints = @UniqueConstraint(columnNames = "sku"))
class Product {
    @Id UUID id;
    @Column(nullable = false) String sku;
    @Column(nullable = false) String name;
    @Column(nullable = false, precision = 19, scale = 2) BigDecimal price;
    @Column(nullable = false) boolean active;
    protected Product() {}
    Product(String sku, String name, BigDecimal price) {
        if (price.signum() <= 0) throw new IllegalArgumentException("Price must be positive");
        this.id = UUID.randomUUID(); this.sku = sku; this.name = name; this.price = price; this.active = true;
    }
}

interface ProductRepository extends org.springframework.data.jpa.repository.JpaRepository<Product, UUID> {}
