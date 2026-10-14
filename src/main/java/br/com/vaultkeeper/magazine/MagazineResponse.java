package br.com.vaultkeeper.magazine;

import java.util.UUID;

// DTO de SAÍDA: o formato que o React recebe.
public record MagazineResponse(
        UUID id,
        String title,
        String issue,
        Integer year,
        Integer month,
        String coverModel,
        Condition condition,
        boolean acquired,
        boolean isPublic,
        String coverImageUrl) {

    public static MagazineResponse from(Magazine m) {
        return new MagazineResponse(m.getId(), m.getTitle(), m.getIssue(), m.getYear(),
                m.getMonth(), m.getCoverModel(), m.getCondition(), m.isAcquired(),
                m.isPublic(), m.getCoverImageUrl());
    }
}
