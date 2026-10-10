package br.com.vaultkeeper.magazine;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

// Basta declarar a interface: o Spring escreve save, findAll, deleteById etc.
// Até métodos como o de baixo são gerados só pelo NOME.
public interface MagazineRepository extends JpaRepository<Magazine, UUID> {

    List<Magazine> findByIsPublicTrueOrderByYearDescMonthDesc();
}
