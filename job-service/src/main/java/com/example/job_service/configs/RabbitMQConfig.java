package com.example.job_service.configs;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String JOB_QUEUE = "job.queue";
  public static final String JOB_EXCHANGE = "job.exchange";
  public static final String JOB_ROUTING_KEY = "job.routing";

  @Bean
  public Queue jobQueue() {
    return new Queue(JOB_QUEUE);
  }

  @Bean
  public TopicExchange jobExchange() {
    return new TopicExchange(JOB_EXCHANGE);
  }

  @Bean
  public Binding binding(Queue queue, TopicExchange exchange) {
    return BindingBuilder
        .bind(queue)
        .to(exchange)
        .with(JOB_ROUTING_KEY);
  }

}
