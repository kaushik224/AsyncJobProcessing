package com.example.worker_service.scheduler;

import com.example.worker_service.configs.RabbitMQConfig;
import com.example.worker_service.entity.Job;
import com.example.worker_service.enums.JobStatus;
import com.example.worker_service.repository.JobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class JobRecoveryScheduler {

  private final JobRepository jobRepository;
  private final RabbitTemplate rabbitTemplate;
  
  // Threshold for considering a job as stuck (5 minutes)
  private static final int STUCK_JOB_THRESHOLD_MINUTES = 5;

  public JobRecoveryScheduler(JobRepository jobRepository, RabbitTemplate rabbitTemplate) {
    this.jobRepository = jobRepository;
    this.rabbitTemplate = rabbitTemplate;
  }


  @Scheduled(fixedRate = 60000)
  @Transactional
  public void recoverStuckJobs() {
    log.info("job_recovery_started");

    try {
      LocalDateTime threshold = LocalDateTime.now().minusMinutes(STUCK_JOB_THRESHOLD_MINUTES);
      
      // Find jobs stuck in PROCESSING status
      List<Job> stuckJobs = jobRepository.findStuckJobs(JobStatus.PROCESSING, threshold);
      
      if (stuckJobs.isEmpty()) {
        log.debug("no_stuck_jobs_found");
        return;
      }

      log.info("stuck_jobs_found count={}", stuckJobs.size());

      int recoveredCount = 0;
      for (Job job : stuckJobs) {
        try {
          recoverJob(job);
          recoveredCount++;
        } catch (Exception e) {
          log.error("job_recovery_failed jobId={} error={}", job.getJobId(), e.getMessage(), e);
        }
      }

      log.info("job_recovery_completed recoveredCount={} totalStuck={}", recoveredCount, stuckJobs.size());
      
    } catch (Exception e) {
      log.error("job_recovery_scheduler_error error={}", e.getMessage(), e);
    }
  }

  private void recoverJob(Job job) {
    log.info("job_recovery_started jobId={} processingStartedAt={}", 
        job.getJobId(), job.getProcessingStartedAt());

    // Move job to RETRYING status
    job.setStatus(JobStatus.RETRYING);
    job.setProcessingStartedAt(null);
    
    // Save to database first (DB-first approach)
    Job savedJob = jobRepository.save(job);
    log.info("job_status_updated jobId={} status=RETRYING", savedJob.getJobId());

    // Republish to queue
    try {
      rabbitTemplate.convertAndSend(
          RabbitMQConfig.JOB_EXCHANGE,
          RabbitMQConfig.JOB_ROUTING_KEY,
          savedJob.getJobId()
      );
      log.info("job_republished_after_recovery jobId={}", savedJob.getJobId());
    } catch (Exception e) {
      log.error("recovery_republish_failed jobId={} error={}", savedJob.getJobId(), e.getMessage(), e);
      // Job is already in DB with RETRYING status, can be recovered later
    }

    log.info("job_recovery_completed jobId={}", savedJob.getJobId());
  }

}
