package com.example.job_service.messaging;

import com.example.job_service.configs.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobPublisher {

  private final RabbitTemplate rabbitTemplate;

  public JobPublisher(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void publishJobId(String jobId) {
    log.info("Publishing job ID to RabbitMQ: {}", jobId);
    rabbitTemplate.convertAndSend(
        RabbitMQConfig.JOB_EXCHANGE,
        RabbitMQConfig.JOB_ROUTING_KEY,
        jobId
    );
    log.info("Job ID published successfully: {}", jobId);
  }

}
