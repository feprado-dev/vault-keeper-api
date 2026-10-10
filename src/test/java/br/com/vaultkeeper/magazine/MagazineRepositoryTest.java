package br.com.vaultkeeper.magazine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

// @DataJpaTest sobe só a parte do banco, rápido e isolado.
@DataJpaTest
class MagazineRepositoryTest {

    @Autowired
    MagazineRepository repository;

    @Test
    void salvaERecuperaUmaRevista() {
        var salva = repository.save(new Magazine("Playboy 293"));

        assertThat(salva.getId()).isNotNull();
        assertThat(repository.findById(salva.getId())).isPresent();
    }

    @Test
    void listaSoAsPublicasDaMaisNovaParaAMaisAntiga() {
        var antiga = new Magazine("Antiga");
        antiga.setYear(1990);
        antiga.setMonth(5);
        antiga.setPublic(true);

        var nova = new Magazine("Nova");
        nova.setYear(2001);
        nova.setMonth(3);
        nova.setPublic(true);

        var privada = new Magazine("Privada");
        privada.setYear(2010);
        privada.setMonth(1);

        repository.saveAll(java.util.List.of(antiga, nova, privada));

        assertThat(repository.findByIsPublicTrueOrderByYearDescMonthDesc())
                .extracting(Magazine::getTitle)
                .containsExactly("Nova", "Antiga");
    }
}
