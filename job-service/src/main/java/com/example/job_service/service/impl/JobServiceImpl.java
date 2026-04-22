package com.example.job_service.service.impl;

import com.example.job_service.dto.JobRequestDTO;
import com.example.job_service.dto.JobResponseDTO;
import com.example.job_service.entities.Job;
import com.example.job_service.enums.JobStatus;
import com.example.job_service.exception.JobNotFoundException;
import com.example.job_service.messaging.JobPublisher;
import com.example.job_service.repository.JobRepository;
import com.example.job_service.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Service
public class JobServiceImpl implements JobService {

  private final JobRepository jobRepository;
  private final JobPublisher jobPublisher;

  @Autowired
  public JobServiceImpl(JobRepository jobRepository, JobPublisher jobPublisher) {
    this.jobRepository = jobRepository;
    this.jobPublisher = jobPublisher;
  }

   @Override
  public JobResponseDTO getJobById(String jobId) {
    log.info("job_fetch_request jobId={}", jobId);

    Job job = jobRepository.findByJobId(jobId)
        .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + jobId));

    log.info("job_fetched_successfully jobId={} status={}", jobId, job.getStatus());
    return mapToResponseDTO(job);
  }

  @Override
  public List<JobResponseDTO> getJobsByStatus(JobStatus status) {
    log.info("jobs_fetch_by_status_request status={}", status);

    List<Job> jobs = jobRepository.findByStatus(status);
    log.info("jobs_fetched_by_status_successfully count={} status={}", jobs.size(), status);

    return jobs.stream()
        .map(this::mapToResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public JobResponseDTO createJob(JobRequestDTO request) {
    log.info("job_create_request type={} idempotencyKey={}", request.getType(), request.getIdempotencyKey());

    if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isEmpty()) {
      Job existingJob = jobRepository.findByIdempotencyKey(request.getIdempotencyKey()).orElse(null);
      if (existingJob != null) {
        log.info("job_already_exists jobId={} idempotencyKey={}", existingJob.getJobId(), request.getIdempotencyKey());
        return mapToResponseDTO(existingJob);
      }
    }

    // Create new job
    Job job = new Job();
    job.setJobId(UUID.randomUUID().toString());
    job.setType(request.getType());
    job.setPayload(request.getPayload());
    job.setIdempotencyKey(request.getIdempotencyKey());

    Job savedJob = jobRepository.save(job);
    log.info("job_saved_to_db jobId={} status={}", savedJob.getJobId(), savedJob.getStatus());

    try {
      jobPublisher.publishJobId(savedJob.getJobId());
      log.info("job_published_to_queue jobId={}", savedJob.getJobId());
    } catch (Exception e) {
      log.error("queue_publish_failed jobId={} error={}", savedJob.getJobId(), e.getMessage(), e);
    }

    log.info("job_created_successfully jobId={} type={}", savedJob.getJobId(), savedJob.getType());
    return mapToResponseDTO(savedJob);
  }


  @Override
  @Transactional
  public JobResponseDTO retryJob(String jobId) {
    log.info("job_retry_request jobId={}", jobId);

    Job job = jobRepository.findByJobId(jobId)
        .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + jobId));

    // Validate job status - only FAILED jobs can be retried
    if (job.getStatus() != JobStatus.FAILED) {
      throw new IllegalArgumentException("Only FAILED jobs can be retried. Current status: " + job.getStatus());
    }

    // Reset retry metadata
    job.setRetryCount(0);
    job.setLastError(null);
    job.setNextRetryAt(null);
    job.setStatus(JobStatus.PENDING);

    Job savedJob = jobRepository.save(job);
    log.info("job_reset_for_retry jobId={} status={}", savedJob.getJobId(), savedJob.getStatus());

    // Publish jobId to queue
    try {
      jobPublisher.publishJobId(savedJob.getJobId());
      log.info("job_republished_for_retry jobId={}", savedJob.getJobId());
    } catch (Exception e) {
      log.error("retry_queue_publish_failed jobId={} error={}", savedJob.getJobId(), e.getMessage(), e);
    }

    log.info("job_retried_successfully jobId={}", savedJob.getJobId());
    return mapToResponseDTO(savedJob);
  }

  private JobResponseDTO mapToResponseDTO(Job job) {
    return JobResponseDTO.builder()
        .jobId(job.getJobId())
        .type(job.getType())
        .payload(job.getPayload())
        .status(job.getStatus())
        .retryCount(job.getRetryCount())
        .maxRetries(job.getMaxRetries())
        .nextRetryAt(job.getNextRetryAt())
        .lastError(job.getLastError())
        .processingStartedAt(job.getProcessingStartedAt())
        .idempotencyKey(job.getIdempotencyKey())
        .createdAt(job.getCreatedAt())
        .updatedAt(job.getUpdatedAt())
        .build();
  }

}
