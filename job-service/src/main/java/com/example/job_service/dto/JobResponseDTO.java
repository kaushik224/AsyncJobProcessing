package com.example.job_service.dto;

import com.example.job_service.enums.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponseDTO {

  private String jobId;
  private String type;
  private String payload;
  private JobStatus status;
  private Integer retryCount;
  private Integer maxRetries;
  private LocalDateTime nextRetryAt;
  private String lastError;
  private LocalDateTime processingStartedAt;
  private String idempotencyKey;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

}
