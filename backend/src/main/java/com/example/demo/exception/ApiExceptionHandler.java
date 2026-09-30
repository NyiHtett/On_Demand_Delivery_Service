package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice 
public class ApiExceptionHandler {
  @ExceptionHandler(DuplicateEmailException.class)
  public ResponseEntity<Void> handleDuplicateEmail(DuplicateEmailException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).build();
  }
  @ExceptionHandler(BadSignUpInputException.class)
  public ResponseEntity<Void> handleBadSignUpInput(BadSignUpInputException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
  }
}
