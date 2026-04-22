package com.example.worker_service.consumer;

import com.example.worker_service.configs.RabbitMQConfig;
import com.example.worker_service.service.JobProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RabbitMQConsumer {

  private final JobProcessor jobProcessor;

  public RabbitMQConsumer(JobProcessor jobProcessor) {
    this.jobProcessor = jobProcessor;
  }

  @RabbitListener(queues = RabbitMQConfig.JOB_QUEUE)
  public void consumeJobId(String jobId) {
    log.info("Received job ID from RabbitMQ: {}", jobId);
    try {
      jobProcessor.processJob(jobId);
    } catch (Exception e) {
      log.error("Error processing job with ID: {}", jobId, e);
    }
  }

}
