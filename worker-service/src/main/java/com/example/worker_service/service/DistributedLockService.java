package com.example.worker_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class DistributedLockService {

  private final RedisTemplate<String, Object> redisTemplate;
  private static final Duration LOCK_TTL = Duration.ofMinutes(5);
  private static final String LOCK_KEY_PREFIX = "processing:";

  public DistributedLockService(RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }


  public boolean acquireLock(String jobId) {
    String lockKey = LOCK_KEY_PREFIX + jobId;
    Boolean acquired = redisTemplate.opsForValue()
        .setIfAbsent(lockKey, "1", LOCK_TTL);
    
    if (Boolean.TRUE.equals(acquired)) {
      log.debug("Lock acquired for job: {}", jobId);
    } else {
      log.debug("Lock already held for job: {}", jobId);
    }
    
    return Boolean.TRUE.equals(acquired);
  }

  public void releaseLock(String jobId) {
    String lockKey = LOCK_KEY_PREFIX + jobId;
    redisTemplate.delete(lockKey);
    log.debug("Lock released for job: {}", jobId);
  }


  public boolean isLocked(String jobId) {
    String lockKey = LOCK_KEY_PREFIX + jobId;
    Boolean exists = redisTemplate.hasKey(lockKey);
    return Boolean.TRUE.equals(exists);
  }

}
