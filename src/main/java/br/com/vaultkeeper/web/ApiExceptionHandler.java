package br.com.vaultkeeper.web;

import br.com.vaultkeeper.magazine.InvalidCoverException;
import br.com.vaultkeeper.magazine.MagazineNotFoundException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Um único lugar que transforma exceções em respostas HTTP claras.
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MagazineNotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(MagazineNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(InvalidCoverException.class)
    public ResponseEntity<Map<String, String>> invalidCover(InvalidCoverException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> tooBig(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of("error", "A imagem passa de 5 MB."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> invalid(MethodArgumentNotValidException e) {
        var fields = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(f -> f.getField(), f -> f.getDefaultMessage(), (a, b) -> a));
        return ResponseEntity.badRequest().body(fields);
    }
}
