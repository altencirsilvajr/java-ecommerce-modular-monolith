package dev.altencir.ecommerce.inventory;

import dev.altencir.ecommerce.shared.WebSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Service
class InventoryService {
    private final StockRepository stocks; private final AdjustmentRepository requests;
    InventoryService(StockRepository stocks, AdjustmentRepository requests) { this.stocks = stocks; this.requests = requests; }

    @Transactional
    StockView adjust(UUID productId, int quantity, String key) {
        String hash = sha(productId + ":" + quantity);
        var prior = requests.findById(key);
        if (prior.isPresent()) {
            if (!prior.get().payloadHash.equals(hash)) throw new WebSupport.Conflict("Idempotency key reused with another payload");
            return view(stocks.findById(productId).orElseThrow());
        }
        var stock = stocks.lockById(productId).orElseGet(() -> new StockItem(productId));
        stock.adjust(quantity); stocks.save(stock); requests.save(new AdjustmentRequest(key, hash, productId));
        return view(stock);
    }
    private static String sha(String input) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }
    static StockView view(StockItem s) { return new StockView(s.productId, s.available, s.reserved); }
    record StockView(UUID productId, int available, int reserved) {}
}
