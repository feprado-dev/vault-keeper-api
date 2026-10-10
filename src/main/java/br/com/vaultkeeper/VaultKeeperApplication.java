package br.com.vaultkeeper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Ponto de partida: o Spring procura classes anotadas a partir deste pacote.
@SpringBootApplication
public class VaultKeeperApplication {

    public static void main(String[] args) {
        SpringApplication.run(VaultKeeperApplication.class, args);
    }
}
