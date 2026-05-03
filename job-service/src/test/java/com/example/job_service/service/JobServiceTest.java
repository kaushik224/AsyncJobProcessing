package com.example.job_service.service;

import com.example.job_service.dto.JobRequestDTO;
import com.example.job_service.dto.JobResponseDTO;
import com.example.job_service.entities.Job;
import com.example.job_service.enums.JobStatus;
import com.example.job_service.exception.JobNotFoundException;
import com.example.job_service.messaging.JobPublisher;
import com.example.job_service.repository.JobRepository;
import com.example.job_service.service.impl.JobServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobPublisher jobPublisher;

    @Mock
    private DistributedLockService distributedLockService;

    @InjectMocks
    private JobServiceImpl jobService;

    private Job testJob;

    @BeforeEach
    void setUp() {
        testJob = new Job();
        testJob.setJobId("job-123");
        testJob.setType("EMAIL_SEND");
        testJob.setPayload("{\"to\":\"user@example.com\"}");
        testJob.setStatus(JobStatus.PENDING);
    }

    @Test
    @DisplayName("Should successfully submit job and publish event")
    void testSubmitJobSuccess() {
        JobRequestDTO requestDTO = new JobRequestDTO();
        requestDTO.setType("EMAIL_SEND");
        requestDTO.setPayload("{\"to\":\"user@example.com\"}");

        when(jobRepository.save(any(Job.class))).thenReturn(testJob);

        JobResponseDTO response = jobService.submitJob(requestDTO);

        assertNotNull(response);
        assertEquals("job-123", response.getJobId());
        verify(jobPublisher, times(1)).publishJob(anyString());
    }

    @Test
    @DisplayName("Should return job by ID if found")
    void testGetJobByIdFound() {
        when(jobRepository.findByJobId("job-123")).thenReturn(Optional.of(testJob));

        JobResponseDTO response = jobService.getJobById("job-123");

        assertNotNull(response);
        assertEquals("job-123", response.getJobId());
        assertEquals(JobStatus.PENDING, response.getStatus());
    }

    @Test
    @DisplayName("Should throw JobNotFoundException when job ID does not exist")
    void testGetJobByIdNotFound() {
        when(jobRepository.findByJobId("job-999")).thenReturn(Optional.empty());

        assertThrows(JobNotFoundException.class, () -> jobService.getJobById("job-999"));
    }
}
