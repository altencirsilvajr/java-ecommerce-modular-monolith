package dev.altencir.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import static org.assertj.core.api.Assertions.assertThat;

class ArchitectureTest {
    private final ApplicationModules modules = ApplicationModules.of(EcommerceApplication.class);

    @Test
    void modules_have_no_cycles_or_illegal_dependencies() {
        modules.verify();
    }

    @Test
    void five_business_modules_are_explicit() {
        assertThat(modules.getModuleByName("users")).isPresent();
        assertThat(modules.getModuleByName("catalog")).isPresent();
        assertThat(modules.getModuleByName("inventory")).isPresent();
        assertThat(modules.getModuleByName("orders")).isPresent();
        assertThat(modules.getModuleByName("payments")).isPresent();
    }
}
