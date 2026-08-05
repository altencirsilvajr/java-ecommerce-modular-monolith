package dev.altencir.ecommerce.users;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "user_accounts", schema = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
class UserAccount {
    @Id UUID id;
    @Column(nullable = false) String email;
    @Column(nullable = false) String passwordHash;
    @Column(nullable = false) String role;
    protected UserAccount() {}
    UserAccount(String email, String passwordHash, String role) {
        this.id = UUID.randomUUID(); this.email = email.toLowerCase(); this.passwordHash = passwordHash; this.role = role;
    }
}

interface UserRepository extends org.springframework.data.jpa.repository.JpaRepository<UserAccount, UUID> {
    java.util.Optional<UserAccount> findByEmailIgnoreCase(String email);
}
