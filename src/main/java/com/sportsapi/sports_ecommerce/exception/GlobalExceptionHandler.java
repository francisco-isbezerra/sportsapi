package com.sportsapi.sports_ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe global para capturar e tratar exceções em toda a API.
 * Isso evita que o cliente receba erros genéricos e feios do Java.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * TRATAMENTO 400 - ERRO DE VALIDAÇÃO (@Valid)
     * Ocorre quando um campo falha em regras como @NotBlank, @Positive ou @NotNull.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Erro de Validação");

        // Captura todos os erros de campos específicos
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        body.put("details", fieldErrors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * TRATAMENTO 400 - ERRO DE LEITURA (JSON Mal formatado)
     * Ocorre quando o usuário envia um texto onde deveria ser número,
     * esquece uma vírgula ou envia um valor inválido para um Enum.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Erro na leitura do JSON");
        body.put("message", "O corpo da requisição está mal formatado ou contém tipos de dados inválidos.");

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * TRATAMENTO 500 - ERRO INTERNO GENÉRICO
     * Captura qualquer outro erro inesperado no servidor para não quebrar a API.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Erro Interno no Servidor");
        body.put("message", "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.");

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}