package com.example.job_service.exception;

public class JobNotFoundException extends RuntimeException {

  public JobNotFoundException(String message) {
    super(message);
  }

  public JobNotFoundException(String jobId, Throwable cause) {
    super("Job not found with ID: " + jobId, cause);
  }

}
