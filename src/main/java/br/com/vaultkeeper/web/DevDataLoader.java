package br.com.vaultkeeper.web;

import br.com.vaultkeeper.magazine.Condition;
import br.com.vaultkeeper.magazine.CoverStorage;
import br.com.vaultkeeper.magazine.Magazine;
import br.com.vaultkeeper.magazine.MagazineRepository;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// Só no perfil "dev" (H2 em memória): cria revistas FICTÍCIAS para a tela não nascer vazia.
// Nunca roda com o perfil "supabase", então não toca nos dados reais.
@Component
@Profile("dev")
public class DevDataLoader implements CommandLineRunner {

    private final MagazineRepository repository;
    private final CoverStorage covers;
    private final UUID ownerId;

    public DevDataLoader(MagazineRepository repository, CoverStorage covers, @Value("${app.owner-id}") UUID ownerId) {
        this.repository = repository;
        this.covers = covers;
        this.ownerId = ownerId;
    }

    // Capa fictícia 3:4 em SVG (só nos dados de exemplo; os envios reais aceitam JPEG, PNG, WebP e AVIF).
    private static byte[] coverSvg(String titulo, int numero, int matiz) {
        var svg = """
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 300 400" role="img">
                  <defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
                    <stop offset="0" stop-color="hsl(%d,70%%,38%%)"/><stop offset="1" stop-color="hsl(%d,75%%,16%%)"/>
                  </linearGradient></defs>
                  <rect width="300" height="400" fill="url(#g)"/>
                  <circle cx="220" cy="120" r="70" fill="hsl(%d,80%%,62%%)" opacity=".35"/>
                  <text x="24" y="330" fill="#fff" font-family="Helvetica,Arial,sans-serif" font-size="34" font-weight="700">%s</text>
                  <text x="24" y="368" fill="#fff" opacity=".8" font-family="Helvetica,Arial,sans-serif" font-size="20">Nº %d</text>
                </svg>
                """.formatted(matiz, (matiz + 40) % 360, (matiz + 20) % 360, titulo, numero);
        return svg.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void run(String... args) {
        String[] titulos = {"Aurora", "Horizonte", "Lume", "Maré", "Vértice", "Cais", "Pétala", "Brasa", "Névoa", "Faísca"};
        for (int i = 0; i < 30; i++) {
            var m = new Magazine(titulos[i % titulos.length] + " Nº " + (100 + i));
            m.setUserId(ownerId);
            m.setIssue(String.valueOf(100 + i));
            m.setYear(1995 + (i % 12));
            m.setMonth(1 + (i % 12));
            m.setCondition(i % 5 == 0 ? null : Condition.values()[i % 4]);
            m.setAcquired(i % 7 != 0);
            m.setPublic(i % 4 != 3); // algumas ficam privadas, só aparecem no painel
            if (i % 6 != 5) { // algumas ficam sem capa de propósito
                m.setCoverImageUrl(covers.storeGenerated("seed-" + i + ".svg",
                        coverSvg(titulos[i % titulos.length], 100 + i, (i * 37) % 360)));
            }
            repository.save(m);
        }
    }
}
