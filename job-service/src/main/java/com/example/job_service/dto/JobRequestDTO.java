package com.example.job_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRequestDTO {

  @NotBlank(message = "Job type is required")
  private String type;

  @NotBlank(message = "Payload is required")
  private String payload;

  @Size(max = 255, message = "Idempotency key must be at most 255 characters")
  private String idempotencyKey;

}
