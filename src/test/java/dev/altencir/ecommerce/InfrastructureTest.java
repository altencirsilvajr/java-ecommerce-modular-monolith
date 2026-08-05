package dev.altencir.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "ecommerce.outbox.enabled=false")
@Testcontainers(disabledWithoutDocker = true)
class InfrastructureTest {
    @Container static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
        .withDatabaseName("ecommerce").withUsername("ecommerce").withPassword("ecommerce");
    @Container static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:8-alpine")).withExposedPorts(6379);
    @Container static final RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management-alpine")
        .withUser("ecommerce", "ecommerce").withPermission("/", "ecommerce", ".*", ".*", ".*");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "ecommerce");
        registry.add("spring.rabbitmq.password", () -> "ecommerce");
        registry.add("ecommerce.jwt.secret", () -> "test-only-secret-that-is-at-least-thirty-two-bytes");
    }

    @Autowired JdbcTemplate jdbc;
    @Autowired RedisConnectionFactory redisConnections;
    @Autowired ConnectionFactory rabbitConnections;

    @Test
    void postgresql_migrations_create_all_module_schemas() {
        var schemas = jdbc.queryForList("select schema_name from information_schema.schemata", String.class);
        assertThat(schemas).contains("users", "catalog", "inventory", "orders", "payments", "shared");
    }

    @Test
    void redis_and_rabbitmq_accept_real_connections() {
        try (var connection = redisConnections.getConnection()) {
            assertThat(connection.ping()).isEqualTo("PONG");
        }
        try (var connection = rabbitConnections.createConnection()) {
            assertThat(connection.isOpen()).isTrue();
        }
    }
}
