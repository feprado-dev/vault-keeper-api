package br.com.vaultkeeper.web;

import br.com.vaultkeeper.magazine.Magazine;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

// Filtro do catálogo público, feito no servidor. É o equivalente Java do applyFilters.ts do React.
// Funções puras (sem banco, sem HTTP): fáceis de testar.
public final class CatalogFilter {

    private CatalogFilter() {
    }

    // "Edição" e "edicao" devem achar a mesma coisa: tira acentos e ignora maiúsculas.
    static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    public static List<Magazine> apply(List<Magazine> all, String query, Integer year) {
        var q = normalize(query);
        return all.stream()
                .filter(m -> year == null || year.equals(m.getYear()))
                .filter(m -> q.isEmpty()
                        || normalize(m.getTitle()).contains(q)
                        || normalize(m.getCoverModel()).contains(q)
                        || normalize(m.getIssue()).contains(q))
                .toList();
    }

    // Só aceita http(s) e as capas locais (/capas/...) como imagem: nada de "javascript:" ou "data:" vindos do banco.
    public static String safeImageUrl(String url) {
        if (url == null) {
            return null;
        }
        var u = url.trim().toLowerCase(Locale.ROOT);
        boolean externa = u.startsWith("https://") || u.startsWith("http://");
        // Capa enviada pelo painel: /capas/<arquivo>, sem subpastas e sem "..".
        boolean local = u.startsWith("/capas/") && u.indexOf('/', "/capas/".length()) == -1 && !u.contains("..");
        return (externa || local) ? url.trim() : null;
    }
}
