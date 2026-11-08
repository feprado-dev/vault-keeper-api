package br.com.vaultkeeper.web;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.vaultkeeper.magazine.Magazine;
import java.util.List;
import org.junit.jupiter.api.Test;

class CatalogFilterTest {

    private static Magazine rev(String titulo, Integer ano, String edicao, String capa) {
        var m = new Magazine(titulo);
        m.setYear(ano);
        m.setIssue(edicao);
        m.setCoverModel(capa);
        return m;
    }

    final List<Magazine> todas = List.of(
            rev("Aurora", 1999, "293", "Maria Conceição"),
            rev("Horizonte", 2001, "310", null),
            rev("Lume", null, null, null));

    @Test
    void semFiltroDevolveTudo() {
        assertThat(CatalogFilter.apply(todas, null, null)).hasSize(3);
        assertThat(CatalogFilter.apply(todas, "  ", null)).hasSize(3);
    }

    @Test
    void buscaIgnoraAcentoEMaiuscula() {
        assertThat(CatalogFilter.apply(todas, "CONCEICAO", null)).extracting(Magazine::getTitle).containsExactly("Aurora");
        assertThat(CatalogFilter.apply(todas, "horizonte", null)).extracting(Magazine::getTitle).containsExactly("Horizonte");
    }

    @Test
    void buscaTambemAchaPeloNumeroDaEdicao() {
        assertThat(CatalogFilter.apply(todas, "310", null)).extracting(Magazine::getTitle).containsExactly("Horizonte");
    }

    @Test
    void filtraPorAnoEEliminaQuemNaoTemAno() {
        assertThat(CatalogFilter.apply(todas, null, 1999)).extracting(Magazine::getTitle).containsExactly("Aurora");
    }

    @Test
    void combinaBuscaEAno() {
        assertThat(CatalogFilter.apply(todas, "aurora", 2001)).isEmpty();
    }

    @Test
    void soAceitaImagemHttpOuHttps() {
        assertThat(CatalogFilter.safeImageUrl("https://x.com/a.png")).isEqualTo("https://x.com/a.png");
        assertThat(CatalogFilter.safeImageUrl("  HTTP://x.com/a.png ")).isEqualTo("HTTP://x.com/a.png");
        assertThat(CatalogFilter.safeImageUrl("javascript:alert(1)")).isNull();
        assertThat(CatalogFilter.safeImageUrl("data:text/html;base64,AAAA")).isNull();
        assertThat(CatalogFilter.safeImageUrl(null)).isNull();
        assertThat(CatalogFilter.safeImageUrl("/capas/abc.png")).isEqualTo("/capas/abc.png");
        assertThat(CatalogFilter.safeImageUrl("/capas/../segredo.png")).isNull();
        assertThat(CatalogFilter.safeImageUrl("/capas/sub/abc.png")).isNull();
        assertThat(CatalogFilter.safeImageUrl("/outra/abc.png")).isNull();
    }
}
