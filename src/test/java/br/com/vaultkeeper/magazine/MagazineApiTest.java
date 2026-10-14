package br.com.vaultkeeper.magazine;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

// Teste de ponta a ponta: faz requisições HTTP de verdade (sem rede) à API inteira.
@SpringBootTest
@AutoConfigureMockMvc
class MagazineApiTest {

    @Autowired MockMvc mvc;
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
