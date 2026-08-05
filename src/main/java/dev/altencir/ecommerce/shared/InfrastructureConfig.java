package dev.altencir.ecommerce.shared;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
class InfrastructureConfig {
    @Bean DirectExchange ecommerceExchange() { return new DirectExchange("ecommerce.events", true, false); }
}
