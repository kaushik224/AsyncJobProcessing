package com.example.worker_service.exception;

public class JobNotFoundException extends RuntimeException {

  public JobNotFoundException(String message) {
    super(message);
  }

}
