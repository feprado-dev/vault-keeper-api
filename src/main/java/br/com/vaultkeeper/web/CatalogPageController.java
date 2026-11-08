package br.com.vaultkeeper.web;

import br.com.vaultkeeper.magazine.Magazine;
import br.com.vaultkeeper.magazine.MagazineService;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Entrega o catálogo público como HTML pronto (renderizado no servidor pelo Thymeleaf).
// Vantagens sobre uma página React: o conteúdo já vem no HTML (bom para buscadores e para
// conexões lentas) e funciona até sem JavaScript.
@Controller
public class CatalogPageController {

    static final int PAGE_SIZE = 24;
    static final int MAX_LIMIT = 480;

    private final MagazineService service;

    public CatalogPageController(MagazineService service) {
        this.service = service;
    }

    // O que o template precisa de cada revista (já com o texto montado e a URL conferida).
    public record Card(String title, String meta, String coverUrl, boolean acquired) {
    }

    @GetMapping("/")
    String home() {
        return "redirect:/catalogo";
    }

    // O painel React é uma pasta estática; este atalho abre o index.html dela.
    @GetMapping({"/painel", "/painel/"})
    String painel() {
        return "forward:/painel/index.html";
    }

    @GetMapping("/catalogo")
    String catalogo(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer ano,
            @RequestParam(defaultValue = "" + PAGE_SIZE) int limite,
            Model model) {
        var publicas = service.listPublic();
        var filtradas = CatalogFilter.apply(publicas, q, ano);
        int limit = Math.max(PAGE_SIZE, Math.min(limite, MAX_LIMIT));

        List<Card> cards = filtradas.stream().limit(limit).map(CatalogPageController::toCard).toList();
        List<Integer> anos = publicas.stream()
                .map(Magazine::getYear)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();

        model.addAttribute("cards", cards);
        model.addAttribute("total", publicas.size());
        model.addAttribute("encontradas", filtradas.size());
        model.addAttribute("anos", anos);
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("ano", ano);
        model.addAttribute("proximoLimite", limit + PAGE_SIZE);
        model.addAttribute("restantes", Math.max(0, filtradas.size() - limit));
        return "catalogo";
    }

    static Card toCard(Magazine m) {
        var date = m.getYear() == null ? null
                : (m.getMonth() == null ? "" : String.format("%02d/", m.getMonth())) + m.getYear();
        var meta = Stream.of(m.getIssue() == null || m.getIssue().isBlank() ? null : "Nº " + m.getIssue(), date)
                .filter(Objects::nonNull)
                .reduce((a, b) -> a + " · " + b)
                .orElse("");
        return new Card(m.getTitle(), meta, CatalogFilter.safeImageUrl(m.getCoverImageUrl()), m.isAcquired());
    }
}
