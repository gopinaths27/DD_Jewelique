package com.ddjewelique.backend.exception;

import com.ddjewelique.backend.dto.ResponseWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handle all RuntimeExceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleRuntimeException(RuntimeException ex) {
        ResponseWrapper<Void> response =
                new ResponseWrapper<>("M400", "Technical Error / " + ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Handle all other Exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<Void>> handleException(Exception ex) {
        ResponseWrapper<Void> response =
                new ResponseWrapper<>("M500", "Unexpected Error / " + ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // Handle ProductNotFoundException specifically
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleProductNotFound(ProductNotFoundException ex) {
        ResponseWrapper<Void> response =
                new ResponseWrapper<>("M404", "Product Not Found / " + ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
