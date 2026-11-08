package br.com.vaultkeeper.magazine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CoverUploadTest {

    static final UUID DONO = UUID.fromString("11111111-1111-1111-1111-111111111111");
    static final UUID OUTRO = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired MockMvc mvc;
    @Autowired MagazineRepository repo;
    @Autowired CoverStorage storage;
    @Autowired ObjectMapper json;

    // Cabeçalho mínimo de um PNG: o servidor só olha os primeiros bytes para decidir o tipo.
    static byte[] png(int extra) {
        var b = new byte[16 + extra];
        System.arraycopy(new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A}, 0, b, 0, 8);
        return b;
    }

    static MockMultipartFile arquivo(String nome, String tipo, byte[] bytes) {
        return new MockMultipartFile("file", nome, tipo, bytes);
    }

    @BeforeEach
    void limpar() {
        repo.deleteAll();
    }

    private UUID revista(UUID dono) {
        var m = new Magazine("Com capa");
        m.setUserId(dono);
        return repo.save(m).getId();
    }

    private Path arquivoDe(String url) {
        return storage.directory().resolve(url.substring(CoverStorage.URL_PREFIX.length()));
    }

    @Test
    void enviaCapaGravaNoDiscoEServePublicamente() throws Exception {
        var id = revista(DONO);

        var corpo = mvc.perform(multipart("/api/magazines/{id}/cover", id).file(arquivo("minha.png", "image/png", png(10)))
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coverImageUrl", startsWith("/capas/")))
                .andExpect(jsonPath("$.coverImageUrl", endsWith(".png")))
                .andReturn().getResponse().getContentAsString();
        var url = json.readTree(corpo).get("coverImageUrl").asText();

        assertThat(Files.exists(arquivoDe(url))).isTrue();
        // quem visita o catálogo não tem token: a imagem é pública
        mvc.perform(get(url)).andExpect(status().isOk()).andExpect(content().contentType("image/png"));
    }

    @Test
    void recusaArquivoQueNaoEImagemMesmoDeclarandoImagePng() throws Exception {
        var id = revista(DONO);
        mvc.perform(multipart("/api/magazines/{id}/cover", id)
                        .file(arquivo("falso.png", "image/png", "<html>oi</html>".getBytes()))
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("imagem")));
        assertThat(repo.findById(id).orElseThrow().getCoverImageUrl()).isNull();
    }

    @Test
    void recusaImagemMaiorQue5MB() throws Exception {
        var id = revista(DONO);
        mvc.perform(multipart("/api/magazines/{id}/cover", id)
                        .file(arquivo("grande.png", "image/png", png(5 * 1024 * 1024)))
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("5 MB")));
    }

    @Test
    void naoMexeNaRevistaDeOutroUsuario() throws Exception {
        var id = revista(OUTRO);
        mvc.perform(multipart("/api/magazines/{id}/cover", id).file(arquivo("a.png", "image/png", png(1))).with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void exigeLogin() throws Exception {
        var id = revista(DONO);
        mvc.perform(multipart("/api/magazines/{id}/cover", id).file(arquivo("a.png", "image/png", png(1))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void trocarACapaApagaAAntigaEApagarARevistaApagaANova() throws Exception {
        var id = revista(DONO);
        var primeira = json.readTree(mvc.perform(multipart("/api/magazines/{id}/cover", id)
                        .file(arquivo("1.png", "image/png", png(1))).with(jwt()))
                .andReturn().getResponse().getContentAsString()).get("coverImageUrl").asText();
        var segunda = json.readTree(mvc.perform(multipart("/api/magazines/{id}/cover", id)
                        .file(arquivo("2.png", "image/png", png(2))).with(jwt()))
                .andReturn().getResponse().getContentAsString()).get("coverImageUrl").asText();

        assertThat(Files.exists(arquivoDe(primeira))).isFalse();
        assertThat(Files.exists(arquivoDe(segunda))).isTrue();

        mvc.perform(delete("/api/magazines/{id}", id).with(jwt())).andExpect(status().isNoContent());
        assertThat(Files.exists(arquivoDe(segunda))).isFalse();
    }

    @Test
    void naoApagaArquivoForaDaPastaNemUrlExterna() throws Exception {
        var fora = Files.createTempFile("nao-apagar", ".txt");
        storage.deleteIfLocal("/capas/../" + fora.getFileName());
        storage.deleteIfLocal("https://exemplo.com/capas/" + fora.getFileName());
        assertThat(Files.exists(fora)).isTrue();
        Files.delete(fora);
    }

    @Test
    void reconheceOsTiposPeloConteudo() {
        assertThat(CoverStorage.detectExtension(new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0})).isEqualTo("jpg");
        assertThat(CoverStorage.detectExtension("RIFF....WEBPVP8 ".getBytes())).isEqualTo("webp");
        assertThat(CoverStorage.detectExtension("....ftypavif....".getBytes())).isEqualTo("avif");
        assertThat(CoverStorage.detectExtension("GIF89a......".getBytes())).isNull();
        assertThat(CoverStorage.detectExtension(new byte[0])).isNull();
    }
}
