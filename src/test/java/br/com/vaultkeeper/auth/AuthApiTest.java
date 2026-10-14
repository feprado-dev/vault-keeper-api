package br.com.vaultkeeper.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

// Aqui NÃO fingimos login: testamos a "portaria" de verdade.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthApiTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private String login(String user, String senha) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + user + "\",\"password\":\"" + senha + "\"}"))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void semTokenAAreaPrivadaDa401() throws Exception {
        mvc.perform(get("/api/magazines")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/magazines").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\"}")).andExpect(status().isUnauthorized());
    }

    @Test
    void catalogoPublicoNaoPrecisaDeLogin() throws Exception {
        mvc.perform(get("/api/public/magazines")).andExpect(status().isOk());
    }

    @Test
    void loginCorretoDaTokenQueAbreAAreaPrivada() throws Exception {
        var token = json.readTree(login("dono", "senha-de-teste-123")).get("token").asText();

        mvc.perform(get("/api/magazines").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void senhaOuUsuarioErradosDao401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"dono\",\"password\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Usuário ou senha inválidos"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"outro\",\"password\":\"senha-de-teste-123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenFalsoOuAdulteradoEhRecusado() throws Exception {
        var token = json.readTree(login("dono", "senha-de-teste-123")).get("token").asText();
        var adulterado = token.substring(0, token.length() - 3) + "abc";

        mvc.perform(get("/api/magazines").header("Authorization", "Bearer " + adulterado))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/magazines").header("Authorization", "Bearer lixo.lixo.lixo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void corsLiberaSoOSiteConfigurado() throws Exception {
        mvc.perform(options("/api/magazines")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));

        mvc.perform(options("/api/magazines")
                        .header("Origin", "http://site-estranho.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}
