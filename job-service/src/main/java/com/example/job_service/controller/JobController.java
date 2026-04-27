package com.example.job_service.controller;

import com.example.job_service.dto.JobRequestDTO;
import com.example.job_service.dto.JobResponseDTO;
import com.example.job_service.enums.JobStatus;
import com.example.job_service.service.JobService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/jobs")
public class JobController {

  private final JobService jobService;

  public JobController(JobService jobService) {
    this.jobService = jobService;
  }

  @GetMapping("/{id}")
  public ResponseEntity<JobResponseDTO> getJobById(@PathVariable String id) {
    log.info("Received request to get job with ID: {}", id);
    JobResponseDTO response = jobService.getJobById(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/status")
  public ResponseEntity<List<JobResponseDTO>> getJobsByStatus(
      @RequestParam(required = false) JobStatus status) {
    if (status != null) {
      log.info("Received request to get jobs with status: {}", status);
      List<JobResponseDTO> response = jobService.getJobsByStatus(status);
      return ResponseEntity.ok(response);
    }
    log.info("Received request to get all jobs");
    List<JobResponseDTO> response = jobService.getJobsByStatus(null);
    return ResponseEntity.ok(response);
  }
  
  @PostMapping
  public ResponseEntity<JobResponseDTO> createJob(@Valid @RequestBody JobRequestDTO request) {
    log.info("Received request to create job with type: {}", request.getType());
    JobResponseDTO response = jobService.createJob(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
  
  @PostMapping("/{id}/retry")
  public ResponseEntity<JobResponseDTO> retryJob(@PathVariable String id) {
    log.info("Received request to retry job with ID: {}", id);
    JobResponseDTO response = jobService.retryJob(id);
    return ResponseEntity.ok(response);
  }

}
