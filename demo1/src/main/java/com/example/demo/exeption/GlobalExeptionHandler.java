package com.example.demo.exeption;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExeptionHandler {

    @ExceptionHandler(value = Exception.class)
        ResponseEntity<String> HandlingRuntimeException(RuntimeException exception){
            return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
