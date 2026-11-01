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

    // user_id é NOT NULL (como no banco real): toda revista de teste precisa de dono.
    private static Magazine revista(String titulo) {
        var m = new Magazine(titulo);
        m.setUserId(DONO);
        return m;
    }

    static final java.util.UUID DONO = java.util.UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    org.springframework.jdbc.core.JdbcTemplate jdbc;

    @Test
    void salvaACondicaoComoTextoDoBancoDoSupabase() {
        var m = revista("Com condição");
        m.setCondition(Condition.BOM);
        var salva = repository.saveAndFlush(m);

        var texto = jdbc.queryForObject("select condition from magazines where id = ?", String.class, salva.getId());
        assertThat(texto).isEqualTo("Bom");
        assertThat(repository.findById(salva.getId()).orElseThrow().getCondition()).isEqualTo(Condition.BOM);
    }

    @Test
    void condicaoVaziaContinuaNulaNoBanco() {
        var salva = repository.saveAndFlush(revista("Sem condição"));

        var texto = jdbc.queryForObject("select condition from magazines where id = ?", String.class, salva.getId());
        assertThat(texto).isNull();
        assertThat(repository.findById(salva.getId()).orElseThrow().getCondition()).isNull();
    }

    @Test
    void buscaSoAsRevistasDoDono() {
        var minha = repository.save(revista("Minha"));
        var deOutro = revista("De outro");
        deOutro.setUserId(java.util.UUID.fromString("22222222-2222-2222-2222-222222222222"));
        deOutro = repository.save(deOutro);

        assertThat(repository.findByUserId(DONO)).extracting(Magazine::getTitle).containsExactly("Minha");
        assertThat(repository.findByIdAndUserId(minha.getId(), DONO)).isPresent();
        assertThat(repository.findByIdAndUserId(deOutro.getId(), DONO)).isEmpty();
    }

    @Test
    void salvaERecuperaUmaRevista() {
        var salva = repository.save(revista("Edição 293"));

        assertThat(salva.getId()).isNotNull();
        assertThat(repository.findById(salva.getId())).isPresent();
    }

    @Test
    void listaSoAsPublicasDaMaisNovaParaAMaisAntiga() {
        var antiga = revista("Antiga");
        antiga.setYear(1990);
        antiga.setMonth(5);
        antiga.setPublic(true);

        var nova = revista("Nova");
        nova.setYear(2001);
        nova.setMonth(3);
        nova.setPublic(true);

        var privada = revista("Privada");
        privada.setYear(2010);
        privada.setMonth(1);

        repository.saveAll(java.util.List.of(antiga, nova, privada));

        assertThat(repository.findByIsPublicTrueOrderByYearDescMonthDesc())
                .extracting(Magazine::getTitle)
                .containsExactly("Nova", "Antiga");
    }
}
