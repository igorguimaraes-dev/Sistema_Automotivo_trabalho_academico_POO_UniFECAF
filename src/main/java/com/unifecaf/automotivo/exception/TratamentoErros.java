package com.unifecaf.automotivo.exception;

import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class TratamentoErros {
    public record Erro(int status, String mensagem, Map<String, String> campos) { }

    @ExceptionHandler(RegraException.class)
    ResponseEntity<Erro> regra(RegraException e) {
        return ResponseEntity.status(e.getStatus()).body(new Erro(e.getStatus().value(), e.getMessage(), Map.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Erro> validacao(MethodArgumentNotValidException e) {
        Map<String, String> campos = new TreeMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> campos.put(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new Erro(400, "Confira os dados informados.", campos));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<Erro> formato(Exception e) {
        return ResponseEntity.badRequest().body(new Erro(400,
                "Formato inválido. Confira os tipos dos campos e os valores de status e tipo.", Map.of()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Erro> argumento(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new Erro(400, e.getMessage(), Map.of()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Erro> integridade(DataIntegrityViolationException e) {
        return ResponseEntity.status(409).body(new Erro(409,
                "Registro duplicado ou relacionamento inválido. Confira marca e modelo.", Map.of()));
    }
}
