package br.com.vaultkeeper.magazine;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

// Guarda as capas enviadas numa pasta do disco (app.storage.dir) e entrega o endereço público /capas/<arquivo>.
// Regras de segurança: o tipo vem do CONTEÚDO do arquivo (os primeiros bytes), nunca do nome nem do
// Content-Type que o cliente declara; o nome gravado é sempre um UUID gerado aqui.
@Component
public class CoverStorage {

    public static final String URL_PREFIX = "/capas/";
    public static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Pattern SAFE_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]*");

    private final Path dir;

    public CoverStorage(@Value("${app.storage.dir:./data/capas}") String dir) {
        this.dir = Path.of(dir).toAbsolutePath().normalize();
    }

    public Path directory() {
        return dir;
    }

    // Descobre a extensão pelos "números mágicos" do início do arquivo. null = não é uma imagem aceita.
    static String detectExtension(byte[] h) {
        if (h.length >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (h.length >= 8 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G') {
            return "png";
        }
        if (h.length >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') {
            return "webp";
        }
        if (h.length >= 12 && h[4] == 'f' && h[5] == 't' && h[6] == 'y' && h[7] == 'p'
                && h[8] == 'a' && h[9] == 'v' && h[10] == 'i' && h[11] == 'f') {
            return "avif";
        }
        return null;
    }

    // Valida, grava e devolve o endereço público (ex.: /capas/3f2a....png).
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidCoverException("Envie um arquivo de imagem.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new InvalidCoverException("A imagem passa de 5 MB.");
        }
        try {
            byte[] bytes = file.getBytes();
            var ext = detectExtension(bytes);
            if (ext == null) {
                throw new InvalidCoverException("O arquivo precisa ser uma imagem JPEG, PNG, WebP ou AVIF.");
            }
            Files.createDirectories(dir);
            var name = UUID.randomUUID() + "." + ext;
            Files.write(dir.resolve(name), bytes);
            return URL_PREFIX + name;
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível gravar a capa", e);
        }
    }

    // Só para os dados de exemplo do perfil dev (capas SVG geradas pelo próprio código).
    public String storeGenerated(String name, byte[] content) {
        try {
            Files.createDirectories(dir);
            Files.write(dir.resolve(name), content);
            return URL_PREFIX + name;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // Apaga a capa antiga ao trocar ou apagar a revista. Ignora endereços de fora (https://...)
    // e qualquer nome que tente sair da pasta.
    public void deleteIfLocal(String url) {
        if (url == null || !url.startsWith(URL_PREFIX)) {
            return;
        }
        var name = url.substring(URL_PREFIX.length());
        if (!SAFE_NAME.matcher(name).matches() || name.contains("..")) {
            return;
        }
        try {
            var target = dir.resolve(name).normalize();
            if (target.startsWith(dir)) {
                Files.deleteIfExists(target);
            }
        } catch (IOException ignored) {
            // arquivo órfão não vale um erro para o usuário
        }
    }
}
