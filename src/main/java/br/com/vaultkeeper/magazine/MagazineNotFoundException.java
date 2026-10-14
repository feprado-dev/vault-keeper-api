package br.com.vaultkeeper.magazine;

import java.util.UUID;

public class MagazineNotFoundException extends RuntimeException {

    public MagazineNotFoundException(UUID id) {
        super("Revista não encontrada: " + id);
    }
}
