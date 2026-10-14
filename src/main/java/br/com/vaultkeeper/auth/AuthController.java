package br.com.vaultkeeper.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthProperties props;
    private final PasswordEncoder encoder;
    private final JwtEncoder jwtEncoder;

    public AuthController(AuthProperties props, PasswordEncoder encoder, JwtEncoder jwtEncoder) {
        this.props = props;
        this.encoder = encoder;
        this.jwtEncoder = jwtEncoder;
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record TokenResponse(String token, long expiresInSeconds) {
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        // Sempre conferimos a senha, mesmo com usuário errado, para a resposta
        // não revelar qual dos dois estava incorreto.
        boolean senhaOk = encoder.matches(request.password(), props.auth().passwordHash());
        boolean usuarioOk = props.auth().username().equals(request.username());
        if (!senhaOk || !usuarioOk) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("error", "Usuário ou senha inválidos"));
        }

        var agora = Instant.now();
        var duracao = Duration.ofHours(props.jwt().expirationHours());
        var claims = JwtClaimsSet.builder()
                .subject(request.username())
                .issuedAt(agora)
                .expiresAt(agora.plus(duracao))
                .build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        var token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return ResponseEntity.ok(new TokenResponse(token, duracao.toSeconds()));
    }
}
