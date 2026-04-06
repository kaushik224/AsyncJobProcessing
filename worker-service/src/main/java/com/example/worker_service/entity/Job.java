package com.example.worker_service.entity;

import com.example.worker_service.enums.JobStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
@Getter
@Setter
public class Job {

  @Id
  @Column(name = "job_id", length = 36)
  private String jobId;

  @Column(name = "type", length = 50, nullable = false)
  private String type;

  @Column(name = "payload", columnDefinition = "JSON")
  private String payload;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 20, nullable = false)
  private JobStatus status;

  @Column(name = "retry_count")
  private Integer retryCount;

  @Column(name = "max_retries")
  private Integer maxRetries;

  @Column(name = "next_retry_at")
  private LocalDateTime nextRetryAt;

  @Column(name = "last_error", columnDefinition = "TEXT")
  private String lastError;

  @Column(name = "processing_started_at")
  private LocalDateTime processingStartedAt;

  @Column(name = "idempotency_key", length = 255, unique = true)
  private String idempotencyKey;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

}
