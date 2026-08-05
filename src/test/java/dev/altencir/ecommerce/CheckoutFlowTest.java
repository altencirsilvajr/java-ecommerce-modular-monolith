package dev.altencir.ecommerce;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CheckoutFlowTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    String adminToken;
    String customerToken;

    @BeforeEach
    void authenticate() throws Exception {
        adminToken = login("admin@test.com", "Password123!");
        customerToken = login("customer@test.com", "Password123!");
    }

    @Test
    void customer_can_observe_checkout_reach_confirmed() throws Exception {
        String productId = createProduct("OK-" + UUID.randomUUID(), "Keyboard", "199.90");
        adjustStock(productId, 3);
        String orderId = checkout(customerToken, "checkout-happy", productId, 1);

        JsonNode order = awaitStatus(customerToken, orderId, "CONFIRMED");

        assertThat(order.path("total").decimalValue()).isEqualByComparingTo("199.90");
    }

    @Test
    void insufficient_stock_cancels_checkout() throws Exception {
        String productId = createProduct("LOW-" + UUID.randomUUID(), "Monitor", "999.00");
        adjustStock(productId, 1);
        String orderId = checkout(customerToken, "checkout-low", productId, 2);

        JsonNode order = awaitStatus(customerToken, orderId, "CANCELLED");

        assertThat(order.path("failureReason").asText()).isEqualTo("INSUFFICIENT_STOCK");
    }

    @Test
    void deterministic_payment_rejection_releases_the_order() throws Exception {
        String productId = createProduct("EXPENSIVE-" + UUID.randomUUID(), "Workstation", "6000.00");
        adjustStock(productId, 1);
        String orderId = checkout(customerToken, "checkout-payment", productId, 1);

        JsonNode order = awaitStatus(customerToken, orderId, "CANCELLED");

        assertThat(order.path("failureReason").asText()).isEqualTo("PAYMENT_REJECTED");
    }

    @Test
    void checkout_idempotency_replays_result_and_rejects_payload_reuse() throws Exception {
        String productId = createProduct("IDEM-" + UUID.randomUUID(), "Mouse", "59.90");
        adjustStock(productId, 4);
        String first = checkout(customerToken, "same-command", productId, 1);
        String replay = checkout(customerToken, "same-command", productId, 1);
        assertThat(replay).isEqualTo(first);

        mvc.perform(post("/api/v1/orders").header("Authorization", bearer(customerToken))
                .header("Idempotency-Key", "same-command").contentType(MediaType.APPLICATION_JSON)
                .content("{\"items\":[{\"productId\":\"%s\",\"quantity\":2}]}".formatted(productId)))
            .andExpect(status().isConflict()).andExpect(content().contentTypeCompatibleWith("application/problem+json"));
    }

    @Test
    void customer_cannot_adjust_stock_or_read_another_customers_order() throws Exception {
        String productId = createProduct("SEC-" + UUID.randomUUID(), "Dock", "399.90");
        mvc.perform(post("/api/v1/inventory/products/{id}/adjust", productId)
                .header("Authorization", bearer(customerToken)).header("Idempotency-Key", "forbidden")
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":1}"))
            .andExpect(status().isForbidden());

        String orderId = checkout(customerToken, "owned-order", productId, 1);
        String other = login("other@test.com", "Password123!");
        mvc.perform(get("/api/v1/orders/{id}", orderId).header("Authorization", bearer(other)))
            .andExpect(status().isForbidden());
    }

    private String login(String email, String password) throws Exception {
        var response = mvc.perform(post("/api/v1/users/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(response).path("accessToken").asText();
    }

    private String createProduct(String sku, String name, String price) throws Exception {
        var response = mvc.perform(post("/api/v1/catalog/products").header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sku\":\"%s\",\"name\":\"%s\",\"price\":%s}".formatted(sku, name, price)))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(response).path("id").asText();
    }

    private void adjustStock(String productId, int quantity) throws Exception {
        mvc.perform(post("/api/v1/inventory/products/{id}/adjust", productId)
                .header("Authorization", bearer(adminToken)).header("Idempotency-Key", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":%d}".formatted(quantity)))
            .andExpect(status().isOk());
    }

    private String checkout(String token, String key, String productId, int quantity) throws Exception {
        var response = mvc.perform(post("/api/v1/orders").header("Authorization", bearer(token))
                .header("Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"items\":[{\"productId\":\"%s\",\"quantity\":%d}]}".formatted(productId, quantity)))
            .andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();
        return json.readTree(response).path("id").asText();
    }

    private JsonNode awaitStatus(String token, String orderId, String expected) throws Exception {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(5));
        JsonNode order = null;
        while (Instant.now().isBefore(deadline)) {
            var response = mvc.perform(get("/api/v1/orders/{id}", orderId).header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            order = json.readTree(response);
            if (expected.equals(order.path("status").asText())) return order;
            Thread.sleep(50);
        }
        throw new AssertionError("Expected " + expected + " but last order was " + order);
    }

    private static String bearer(String token) { return "Bearer " + token; }
}
