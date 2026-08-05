package dev.altencir.ecommerce.users;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
class UsersController {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final JwtService jwt;
    UsersController(UserRepository users, PasswordEncoder passwords, JwtService jwt) { this.users = users; this.passwords = passwords; this.jwt = jwt; }

    @PostMapping("/login")
    Map<String, String> login(@Valid @RequestBody LoginRequest request) {
        var user = users.findByEmailIgnoreCase(request.email()).filter(it -> passwords.matches(request.password(), it.passwordHash))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return Map.of("accessToken", jwt.issue(user), "tokenType", "Bearer", "role", user.role);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    Map<String, String> register(@Valid @RequestBody LoginRequest request) {
        if (users.findByEmailIgnoreCase(request.email()).isPresent()) throw new dev.altencir.ecommerce.shared.WebSupport.Conflict("Email already registered");
        var user = users.save(new UserAccount(request.email(), passwords.encode(request.password()), "CUSTOMER"));
        return Map.of("id", user.id.toString(), "email", user.email);
    }

    record LoginRequest(@Email @NotBlank String email, @Size(min = 8) String password) {}
}

@Component
class DevelopmentUsers implements ApplicationRunner {
    private final UserRepository users; private final PasswordEncoder passwords;
    DevelopmentUsers(UserRepository users, PasswordEncoder passwords) { this.users = users; this.passwords = passwords; }
    @Transactional public void run(ApplicationArguments args) {
        seed("admin@test.com", "ADMIN"); seed("customer@test.com", "CUSTOMER"); seed("other@test.com", "CUSTOMER");
    }
    private void seed(String email, String role) {
        if (users.findByEmailIgnoreCase(email).isEmpty()) users.save(new UserAccount(email, passwords.encode("Password123!"), role));
    }
}
