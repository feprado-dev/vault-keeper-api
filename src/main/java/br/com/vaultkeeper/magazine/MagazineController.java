package br.com.vaultkeeper.magazine;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Área do dono (no futuro protegida por login).
@RestController
@RequestMapping("/api/magazines")
public class MagazineController {

    private final MagazineService service;

    public MagazineController(MagazineService service) {
        this.service = service;
    }

    @GetMapping
    public List<MagazineResponse> list() {
        return service.listAll().stream().map(MagazineResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MagazineResponse create(@Valid @RequestBody MagazineRequest request) {
        return MagazineResponse.from(service.create(request));
    }

    @PutMapping("/{id}")
    public MagazineResponse update(@PathVariable UUID id, @Valid @RequestBody MagazineRequest request) {
        return MagazineResponse.from(service.update(id, request));
    }

    // PATCH = muda só uma parte (aqui, só a visibilidade).
    @PatchMapping("/{id}/public")
    public MagazineResponse setPublic(@PathVariable UUID id, @RequestBody VisibilityRequest request) {
        return MagazineResponse.from(service.setPublic(id, request.isPublic()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    public record VisibilityRequest(boolean isPublic) {
    }
}
