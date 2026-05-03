package com.example.worker_service.service;

import com.example.worker_service.entity.Job;
import com.example.worker_service.enums.JobStatus;
import com.example.worker_service.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobProcessorTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private DistributedLockService distributedLockService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private JobProcessor jobProcessor;

    private Job testJob;

    @BeforeEach
    void setUp() {
        testJob = new Job();
        testJob.setJobId("job-456");
        testJob.setType("IMAGE_RESIZE");
        testJob.setStatus(JobStatus.PENDING);
        testJob.setRetryCount(0);
        testJob.setMaxRetries(3);
    }

    @Test
    @DisplayName("Should skip processing if distributed lock acquisition fails")
    void testProcessJobLockAcquisitionFailed() {
        when(distributedLockService.acquireLock("job-456")).thenReturn(false);

        jobProcessor.processJob("job-456");

        verify(jobRepository, never()).findByJobId(anyString());
    }

    @Test
    @DisplayName("Should process pending job successfully when lock acquired")
    void testProcessJobSuccess() {
        when(distributedLockService.acquireLock("job-456")).thenReturn(true);
        when(jobRepository.findByJobId("job-456")).thenReturn(Optional.of(testJob));

        jobProcessor.processJob("job-456");

        verify(jobRepository, atLeastOnce()).save(any(Job.class));
        verify(distributedLockService, times(1)).releaseLock("job-456");
    }
}
