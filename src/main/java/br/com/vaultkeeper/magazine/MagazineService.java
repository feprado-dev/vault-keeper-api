package br.com.vaultkeeper.magazine;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Aqui moram as REGRAS. O controller só traduz HTTP; o repositório só fala
// com o banco. É o mesmo papel do magazineService.ts no React.
@Service
@Transactional
public class MagazineService {

    private final MagazineRepository repository;

    public MagazineService(MagazineRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Magazine> listAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Magazine> listPublic() {
        return repository.findByIsPublicTrueOrderByYearDescMonthDesc();
    }

    public Magazine create(MagazineRequest request) {
        var magazine = new Magazine(request.title());
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
        repository.delete(find(id));
    }

    private Magazine find(UUID id) {
        return repository.findById(id).orElseThrow(() -> new MagazineNotFoundException(id));
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
    }
}
