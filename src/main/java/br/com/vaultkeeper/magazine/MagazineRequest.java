package br.com.vaultkeeper.magazine;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

// DTO de ENTRADA: o que o cliente pode mandar. Nunca expomos a entidade
// direto, assim o cliente não consegue, por exemplo, escolher o próprio id.
// `record` = classe imutável com construtor e getters prontos (Java 16+).
public record MagazineRequest(
        @NotBlank(message = "O título é obrigatório") String title,
        String issue,
        Integer year,
        @Min(value = 1, message = "O mês deve ser de 1 a 12")
        @Max(value = 12, message = "O mês deve ser de 1 a 12") Integer month,
        String coverModel,
        Condition condition,
        boolean acquired,
        boolean isPublic,
        String coverImageUrl) {
}
