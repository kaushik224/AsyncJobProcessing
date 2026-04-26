package com.example.job_service.util;


public class RetryUtil {


  public static long calculateRetryDelay(int retryCount) {
    return (long) Math.pow(2, retryCount) * 1000;
  }

  public static long calculateRetryDelaySeconds(int retryCount) {
    return calculateRetryDelay(retryCount) / 1000;
  }

  public static boolean shouldRetry(int retryCount, int maxRetries) {
    return retryCount < maxRetries;
  }

}
