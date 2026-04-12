package com.example.worker_service.exception;

public class InvalidJobStatusTransitionException extends RuntimeException {

  public InvalidJobStatusTransitionException(String message) {
    super(message);
  }

}
