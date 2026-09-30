package com.example.demo.exception;

public class BadSignUpInputException extends RuntimeException {
  public enum BadSignUpInputType {NAME, EMAIL, PASSWORD};
  public BadSignUpInputException (String msg, BadSignUpInputType type) {
    super(msg);
  }
}
