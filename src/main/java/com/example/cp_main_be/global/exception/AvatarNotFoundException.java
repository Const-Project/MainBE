package com.example.cp_main_be.global.exception;

public class AvatarNotFoundException extends RuntimeException {
  public AvatarNotFoundException(String message) {
    super(message);
  }
}
