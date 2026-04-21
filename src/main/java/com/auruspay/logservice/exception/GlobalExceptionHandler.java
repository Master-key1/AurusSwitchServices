package com.auruspay.logservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 🔥 Validation Errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("Validation failed: {}", errors);

        return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "type", "VALIDATION_ERROR",
                "errors", errors
        ));
    }

    // 🔥 Zabbix / Network Timeout
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<?> handleTimeout(ResourceAccessException ex) {

        log.error("Zabbix connection timeout: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(Map.of(
                "status", "error",
                "type", "TIMEOUT",
                "message", "Zabbix server not reachable"
        ));
    }

    // 🔥 Runtime Errors
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex) {

        log.error("Runtime error: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "type", "BUSINESS_ERROR",
                "message", ex.getMessage()
        ));
    }

    // 🔥 Generic Exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex) {

        log.error("Unhandled exception: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "type", "SYSTEM_ERROR",
                "message", "Something went wrong"
        ));
    }
}