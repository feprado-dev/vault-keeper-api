package br.com.vaultkeeper.magazine;

// Capa recusada (tipo, tamanho ou conteúdo inválido). Vira HTTP 400 no ApiExceptionHandler.
public class InvalidCoverException extends RuntimeException {
    public InvalidCoverException(String message) {
        super(message);
    }
}
