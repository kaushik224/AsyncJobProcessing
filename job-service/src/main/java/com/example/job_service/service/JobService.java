package com.example.job_service.service;

import com.example.job_service.dto.JobRequestDTO;
import com.example.job_service.dto.JobResponseDTO;
import com.example.job_service.enums.JobStatus;

import java.util.List;

public interface JobService {

  JobResponseDTO getJobById(String jobId);

  List<JobResponseDTO> getJobsByStatus(JobStatus status);

  JobResponseDTO createJob(JobRequestDTO request);

  JobResponseDTO retryJob(String jobId);

}
