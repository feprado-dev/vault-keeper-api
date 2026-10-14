package br.com.vaultkeeper.magazine;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Área de qualquer visitante: só lê, só o que é público.
@RestController
@RequestMapping("/api/public/magazines")
public class PublicMagazineController {

    private final MagazineService service;

    public PublicMagazineController(MagazineService service) {
        this.service = service;
    }

    @GetMapping
    public List<MagazineResponse> list() {
        return service.listPublic().stream().map(MagazineResponse::from).toList();
    }
}
