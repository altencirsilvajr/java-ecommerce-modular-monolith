package dev.altencir.ecommerce.users;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
class JwtService {
    private final byte[] secret;
    private final ObjectMapper json;
    JwtService(@Value("${ecommerce.jwt.secret}") String secret, ObjectMapper json) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8); this.json = json;
    }
    String issue(UserAccount user) {
        try {
            String header = encode(json.writeValueAsBytes(Map.of("alg", "HS256", "typ", "JWT")));
            String payload = encode(json.writeValueAsBytes(Map.of("sub", user.id.toString(), "email", user.email,
                "role", user.role, "exp", Instant.now().plusSeconds(3600).getEpochSecond())));
            String body = header + "." + payload;
            return body + "." + encode(sign(body));
        } catch (Exception e) { throw new IllegalStateException("Could not issue token", e); }
    }
    Claims verify(String token) {
        try {
            var parts = token.split("\\.");
            if (parts.length != 3) throw new IllegalArgumentException("Malformed token");
            String body = parts[0] + "." + parts[1];
            if (!java.security.MessageDigest.isEqual(sign(body), Base64.getUrlDecoder().decode(parts[2]))) throw new IllegalArgumentException("Invalid signature");
            var node = json.readTree(Base64.getUrlDecoder().decode(parts[1]));
            if (node.path("exp").asLong() < Instant.now().getEpochSecond()) throw new IllegalArgumentException("Expired token");
            return new Claims(UUID.fromString(node.path("sub").asText()), node.path("email").asText(), node.path("role").asText());
        } catch (Exception e) { throw new IllegalArgumentException("Invalid token", e); }
    }
    private byte[] sign(String value) throws Exception {
        var mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
    }
    private static String encode(byte[] value) { return Base64.getUrlEncoder().withoutPadding().encodeToString(value); }
    record Claims(UUID userId, String email, String role) {}
}
