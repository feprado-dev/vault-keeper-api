package br.com.vaultkeeper.magazine;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

// Aqui moram as REGRAS. O controller só traduz HTTP; o repositório só fala
// com o banco. É o mesmo papel do magazineService.ts no React.
@Service
@Transactional
public class MagazineService {

    private final MagazineRepository repository;

    // A API tem um único dono (o administrador); o id dele vem de APP_OWNER_ID.
    private final UUID ownerId;
    private final CoverStorage covers;

    public MagazineService(MagazineRepository repository, CoverStorage covers,
            @Value("${app.owner-id}") UUID ownerId) {
        this.repository = repository;
        this.covers = covers;
        this.ownerId = ownerId;
    }

    @Transactional(readOnly = true)
    public List<Magazine> listAll() {
        return repository.findByUserId(ownerId);
    }

    @Transactional(readOnly = true)
    public List<Magazine> listPublic() {
        return repository.findByIsPublicTrueOrderByYearDescMonthDesc();
    }

    public Magazine create(MagazineRequest request) {
        var magazine = new Magazine(request.title());
        magazine.setUserId(ownerId);
        apply(magazine, request);
        return repository.save(magazine);
    }

    public Magazine update(UUID id, MagazineRequest request) {
        var magazine = find(id);
        apply(magazine, request);
        return magazine; // dentro da transação, o JPA grava as mudanças sozinho
    }

    public Magazine setPublic(UUID id, boolean value) {
        var magazine = find(id);
        magazine.setPublic(value);
        return magazine;
    }

    public void delete(UUID id) {
        var magazine = find(id);
        repository.delete(magazine);
        covers.deleteIfLocal(magazine.getCoverImageUrl()); // não deixa a imagem órfã no disco
    }

    // Troca a capa: valida e grava a nova primeiro; só então apaga a antiga.
    public Magazine replaceCover(UUID id, MultipartFile file) {
        var magazine = find(id); // 404 se a revista não for do dono
        var old = magazine.getCoverImageUrl();
        magazine.setCoverImageUrl(covers.store(file));
        covers.deleteIfLocal(old);
        return magazine;
    }

    private Magazine find(UUID id) {
        return repository.findByIdAndUserId(id, ownerId).orElseThrow(() -> new MagazineNotFoundException(id));
    }

    private void apply(Magazine m, MagazineRequest r) {
        m.setTitle(r.title());
        m.setIssue(r.issue());
        m.setYear(r.year());
        m.setMonth(r.month());
        m.setCoverModel(r.coverModel());
        m.setCondition(r.condition()); // null continua null (corrige o bug do 'Bom')
        m.setAcquired(r.acquired());
        m.setPublic(r.isPublic());
        m.setCoverImageUrl(r.coverImageUrl());
    }
}
