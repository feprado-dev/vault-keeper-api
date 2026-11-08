package br.com.vaultkeeper.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.vaultkeeper.magazine.Magazine;
import br.com.vaultkeeper.magazine.MagazineRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

// A página HTML é pública: estes testes NÃO enviam token.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogPageTest {

    static final UUID DONO = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired MockMvc mvc;
    @Autowired MagazineRepository repo;

    private void salvar(String titulo, boolean publica, Integer ano, String capa) {
        var m = new Magazine(titulo);
        m.setUserId(DONO);
        m.setPublic(publica);
        m.setYear(ano);
        m.setCoverImageUrl(capa);
        repo.save(m);
    }

    @BeforeEach
    void limpar() {
        repo.deleteAll();
    }

    @Test
    void raizRedirecionaParaOCatalogo() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/catalogo"));
    }

    @Test
    void painelAbreComOuSemBarraFinalSemToken() throws Exception {
        mvc.perform(get("/painel")).andExpect(status().isOk()).andExpect(forwardedUrl("/painel/index.html"));
        mvc.perform(get("/painel/")).andExpect(status().isOk()).andExpect(forwardedUrl("/painel/index.html"));
    }

    @Test
    void mostraSoRevistasPublicasEJaVemNoHtml() throws Exception {
        salvar("Pública Alfa", true, 1999, "https://exemplo.com/a.png");
        salvar("Privada Beta", false, 2000, null);

        mvc.perform(get("/catalogo"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Pública Alfa")))
                .andExpect(content().string(containsString("https://exemplo.com/a.png")))
                .andExpect(content().string(containsString("1 de 1 revistas públicas")))
                .andExpect(content().string(not(containsString("Privada Beta"))));
    }

    @Test
    void escapaHtmlDoTituloEDescartaUrlPerigosa() throws Exception {
        salvar("<script>alert(1)</script>", true, 1999, "javascript:alert(1)");

        mvc.perform(get("/catalogo"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;alert(1)&lt;/script&gt;")))
                .andExpect(content().string(not(containsString("javascript:alert"))))
                .andExpect(content().string(containsString("Sem capa")));
    }

    @Test
    void filtraPorBuscaEAno() throws Exception {
        salvar("Aurora", true, 1999, null);
        salvar("Horizonte", true, 2001, null);

        mvc.perform(get("/catalogo").param("q", "auro"))
                .andExpect(content().string(containsString("Aurora")))
                .andExpect(content().string(not(containsString("Horizonte"))));
        mvc.perform(get("/catalogo").param("ano", "2001"))
                .andExpect(content().string(containsString("Horizonte")))
                .andExpect(content().string(not(containsString("Aurora"))))
                .andExpect(content().string(containsString("1 de 2 revistas públicas")));
    }

    @Test
    void mostraMaisDe24EmLotes() throws Exception {
        for (int i = 0; i < 30; i++) {
            salvar("Edição " + i, true, 1990 + (i % 5), null);
        }
        mvc.perform(get("/catalogo"))
                .andExpect(content().string(containsString("Mostrar mais (6 restantes)")));
        mvc.perform(get("/catalogo").param("limite", "48"))
                .andExpect(content().string(not(containsString("Mostrar mais"))));
    }

    @Test
    void apiPrivadaContinuaExigindoToken() throws Exception {
        mvc.perform(get("/api/magazines")).andExpect(status().isUnauthorized());
    }
}
