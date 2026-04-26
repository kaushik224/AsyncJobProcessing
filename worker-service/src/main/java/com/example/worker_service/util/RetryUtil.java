package com.example.worker_service.util;


public class RetryUtil {


  private static final long MAX_RETRY_DELAY_MS = 300000; // 5 minutes cap

  public static long calculateRetryDelay(int retryCount) {
    long delay = (long) Math.pow(2, retryCount) * 1000;
    return Math.min(delay, MAX_RETRY_DELAY_MS);
  }


  public static long calculateRetryDelaySeconds(int retryCount) {
    return calculateRetryDelay(retryCount) / 1000;
  }


  public static boolean shouldRetry(int retryCount, int maxRetries) {
    return retryCount < maxRetries;
  }

}
