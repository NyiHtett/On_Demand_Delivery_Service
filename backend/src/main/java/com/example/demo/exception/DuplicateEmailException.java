package com.example.demo.exception;

public class DuplicateEmailException extends RuntimeException {
  public DuplicateEmailException (String msg) {
    super(msg);
  }
}
