package com.example.worker_service.service;

import com.example.worker_service.configs.RabbitMQConfig;
import com.example.worker_service.entity.Job;
import com.example.worker_service.enums.JobStatus;
import com.example.worker_service.exception.InvalidJobStatusTransitionException;
import com.example.worker_service.exception.JobNotFoundException;
import com.example.worker_service.repository.JobRepository;
import com.example.worker_service.util.RetryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class JobProcessor {

  private final JobRepository jobRepository;
  private final DistributedLockService distributedLockService;
  private final RabbitTemplate rabbitTemplate;

  public JobProcessor(JobRepository jobRepository, 
                      DistributedLockService distributedLockService,
                      RabbitTemplate rabbitTemplate) {
    this.jobRepository = jobRepository;
    this.distributedLockService = distributedLockService;
    this.rabbitTemplate = rabbitTemplate;
  }

  @Transactional
  public void processJob(String jobId) {
    long startTime = System.currentTimeMillis();
    log.info("job_processing_started jobId={}", jobId);

    if (!distributedLockService.acquireLock(jobId)) {
      log.warn("job_lock_failed jobId={} reason=already_processing", jobId);
      return;
    }

    try {
      Job job = jobRepository.findByJobId(jobId)
          .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + jobId));

      if (job.getStatus() == JobStatus.SUCCESS) {
        log.info("job_already_succeeded jobId={} skipping=true", jobId);
        return;
      }

      validateAndSetStatus(job, JobStatus.PROCESSING);
      job.setProcessingStartedAt(LocalDateTime.now());
      jobRepository.save(job);
      log.info("job_status_updated jobId={} status=PROCESSING", jobId);

      long processingTime = 0;
      try {
        processingTime = simulateWork(job);
        validateAndSetStatus(job, JobStatus.SUCCESS);
        jobRepository.save(job);
        log.info("job_processed_successfully jobId={} status=SUCCESS processingTime={}ms", jobId, processingTime);
      } catch (Exception e) {
        log.error("job_processing_failed jobId={} error={}", jobId, e.getMessage(), e);
        handleFailure(job, e);
      }

    } catch (Exception e) {
      log.error("job_processing_error jobId={} error={}", jobId, e.getMessage(), e);
    } finally {
      // Release lock
      distributedLockService.releaseLock(jobId);
      long totalTime = System.currentTimeMillis() - startTime;
      log.info("job_processing_completed jobId={} totalTime={}ms", jobId, totalTime);
    }
  }


  private void handleFailure(Job job, Exception error) {
    int retryCount = job.getRetryCount() + 1;
    job.setRetryCount(retryCount);
    job.setLastError(error.getMessage());

    if (RetryUtil.shouldRetry(retryCount, job.getMaxRetries())) {
      // Set status to RETRYING and calculate next retry time
      job.setStatus(JobStatus.RETRYING);
      long delayMs = RetryUtil.calculateRetryDelay(retryCount);
      job.setNextRetryAt(LocalDateTime.now().plusSeconds(delayMs / 1000));
      
      jobRepository.save(job);
      log.info("job_scheduled_for_retry jobId={} retryCount={} nextRetryAt={} delay={}s", 
          job.getJobId(), retryCount, job.getNextRetryAt(), delayMs / 1000);

      try {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.JOB_EXCHANGE,
            RabbitMQConfig.JOB_ROUTING_KEY,
            job.getJobId()
        );
        log.info("job_republished_for_retry jobId={}", job.getJobId());
      } catch (Exception e) {
        log.error("retry_republish_failed jobId={} error={}", job.getJobId(), e.getMessage(), e);
      }
    } else {
      // After Max retries , mark as FAILED
      job.setStatus(JobStatus.FAILED);
      jobRepository.save(job);
      log.info("job_failed_max_retries jobId={} retryCount={} maxRetries={}", 
          job.getJobId(), retryCount, job.getMaxRetries());
    }
  }

  private void validateAndSetStatus(Job job, JobStatus newStatus) {
    JobStatus currentStatus = job.getStatus();
    
    if (!isValidTransition(currentStatus, newStatus)) {
      throw new InvalidJobStatusTransitionException(
          String.format("Invalid status transition from %s to %s for job ID: %s",
              currentStatus, newStatus, job.getJobId())
      );
    }
    
    job.setStatus(newStatus);
  }


  private boolean isValidTransition(JobStatus current, JobStatus next) {
    return (current == JobStatus.PENDING && next == JobStatus.PROCESSING) ||
           (current == JobStatus.PROCESSING && (next == JobStatus.SUCCESS || next == JobStatus.FAILED || next == JobStatus.RETRYING)) ||
           (current == JobStatus.RETRYING && next == JobStatus.PROCESSING);
  }


  private long simulateWork(Job job) {
    log.info("job_work_started jobId={} type={}", job.getJobId(), job.getType());
    long startTime = System.currentTimeMillis();
    
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Work simulation interrupted", e);
    }
    
    long processingTime = System.currentTimeMillis() - startTime;
    log.info("job_work_completed jobId={} type={} processingTime={}ms", 
        job.getJobId(), job.getType(), processingTime);
    
    return processingTime;
  }

}
