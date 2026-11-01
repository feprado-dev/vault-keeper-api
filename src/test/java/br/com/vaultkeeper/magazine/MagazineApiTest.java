package br.com.vaultkeeper.magazine;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

// Teste de ponta a ponta: faz requisições HTTP de verdade (sem rede) à API inteira.
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
class MagazineApiTest {

    @Autowired WebApplicationContext contexto;
    MockMvc mvc;

    // Monta um MockMvc que já "chega logado": todo pedido leva um JWT válido.
    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto)
                .apply(springSecurity())
                .defaultRequest(get("/").with(jwt()))
                .build();
    }
    @Autowired ObjectMapper json;

    private String criar(String corpo) throws Exception {
        var resposta = mvc.perform(post("/api/magazines")
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode no = json.readTree(resposta);
        return no.get("id").asText();
    }

    @Test
    void criaERetornaARevista() throws Exception {
        mvc.perform(post("/api/magazines").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Edição 293\",\"year\":1999,\"month\":8}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Edição 293"))
                .andExpect(jsonPath("$.condition").doesNotExist());
    }

    @Test
    void recusaTituloEmBrancoEMesInvalido() throws Exception {
        mvc.perform(post("/api/magazines").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\" \",\"month\":13}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("O título é obrigatório"))
                .andExpect(jsonPath("$.month").value("O mês deve ser de 1 a 12"));
    }

    @Test
    void editarSemCondicaoMantemNula() throws Exception {
        var id = criar("{\"title\":\"A\"}");
        mvc.perform(put("/api/magazines/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"B\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("B"))
                .andExpect(jsonPath("$.condition").doesNotExist());
    }

    @Test
    void soAparecemNoPublicoAsRevistasMarcadas() throws Exception {
        var id = criar("{\"title\":\"Secreta Zeta\"}");
        mvc.perform(get("/api/public/magazines"))
                .andExpect(jsonPath("$[?(@.title=='Secreta Zeta')]").isEmpty());

        mvc.perform(patch("/api/magazines/" + id + "/public")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"isPublic\":true}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/public/magazines"))
                .andExpect(jsonPath("$[?(@.title=='Secreta Zeta')]").isNotEmpty());
    }

    @Autowired MagazineRepository repositorio;

    @Test
    void naoEnxergaNemMexeEmRevistaDeOutroUsuario() throws Exception {
        var deOutro = new Magazine("Revista alheia");
        deOutro.setUserId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        var id = repositorio.save(deOutro).getId();

        mvc.perform(get("/api/magazines"))
                .andExpect(jsonPath("$[?(@.title=='Revista alheia')]").isEmpty());
        mvc.perform(delete("/api/magazines/" + id)).andExpect(status().isNotFound());
        mvc.perform(put("/api/magazines/" + id)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"invadida\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void apagaEDepoisDa404() throws Exception {
        var id = criar("{\"title\":\"Descartável\"}");
        mvc.perform(delete("/api/magazines/" + id)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/magazines/" + id)).andExpect(status().isNotFound());
        mvc.perform(put("/api/magazines/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"x\"}"))
                .andExpect(status().isNotFound());
    }
}
