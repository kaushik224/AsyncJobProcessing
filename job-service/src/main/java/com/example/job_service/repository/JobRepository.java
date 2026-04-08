package com.example.job_service.repository;

import com.example.job_service.entities.Job;
import com.example.job_service.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, String> {

  Optional<Job> findByJobId(String jobId);

  List<Job> findByStatus(JobStatus status);

  Optional<Job> findByIdempotencyKey(String idempotencyKey);


  @Query("SELECT j FROM Job j WHERE j.status = :status AND j.processingStartedAt < :threshold")
  List<Job> findStuckJobs(@Param("status") JobStatus status, @Param("threshold") LocalDateTime threshold);


  @Query("SELECT j FROM Job j WHERE j.status = :status AND j.nextRetryAt < :now")
  List<Job> findJobsReadyForRetry(@Param("status") JobStatus status, @Param("now") LocalDateTime now);

}
