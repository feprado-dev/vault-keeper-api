package br.com.vaultkeeper.magazine;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

// Basta declarar a interface: o Spring escreve save, findAll, deleteById etc.
// Até métodos como o de baixo são gerados só pelo NOME.
public interface MagazineRepository extends JpaRepository<Magazine, UUID> {

    List<Magazine> findByIsPublicTrueOrderByYearDescMonthDesc();

    // As consultas do dono sempre filtram por user_id: a API conecta como dono do banco,
    // então a RLS do Supabase NÃO protege aqui. Esta é a proteção.
    List<Magazine> findByUserId(UUID userId);

    java.util.Optional<Magazine> findByIdAndUserId(UUID id, UUID userId);
}
