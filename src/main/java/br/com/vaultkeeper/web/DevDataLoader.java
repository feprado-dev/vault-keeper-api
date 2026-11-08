package br.com.vaultkeeper.web;

import br.com.vaultkeeper.magazine.Condition;
import br.com.vaultkeeper.magazine.Magazine;
import br.com.vaultkeeper.magazine.MagazineRepository;
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
    private final UUID ownerId;

    public DevDataLoader(MagazineRepository repository, @Value("${app.owner-id}") UUID ownerId) {
        this.repository = repository;
        this.ownerId = ownerId;
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
            repository.save(m);
        }
    }
}
