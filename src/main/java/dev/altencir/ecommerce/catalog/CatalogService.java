package dev.altencir.ecommerce.catalog;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CatalogService implements CatalogQuery {
    private final ProductRepository products;
    CatalogService(ProductRepository products) { this.products = products; }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "catalog-products", key = "'all'")
    public List<ProductView> list() { return products.findAll().stream().filter(it -> it.active).map(CatalogService::view).toList(); }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "catalog-product", key = "#id")
    public Optional<ProductView> find(UUID id) { return products.findById(id).filter(it -> it.active).map(CatalogService::view); }

    @Transactional
    @CacheEvict(cacheNames = {"catalog-products", "catalog-product"}, allEntries = true)
    public ProductView create(String sku, String name, BigDecimal price) { return view(products.save(new Product(sku, name, price))); }

    private static ProductView view(Product p) { return new ProductView(p.id, p.sku, p.name, p.price, p.active); }
}
