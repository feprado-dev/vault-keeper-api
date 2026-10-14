package br.com.vaultkeeper.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Lê o bloco `app:` do application.yml e entrega como objeto tipado.
// A senha NUNCA fica em texto: guardamos só o hash BCrypt.
@ConfigurationProperties(prefix = "app")
public record AuthProperties(Auth auth, Jwt jwt, Cors cors) {

    public record Auth(String username, String passwordHash) {
    }

    public record Jwt(String secret, long expirationHours) {
    }

    public record Cors(java.util.List<String> allowedOrigins) {
    }
}
