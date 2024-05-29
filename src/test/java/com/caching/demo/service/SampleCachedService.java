package com.caching.demo.service;

import com.caching.demo.cache.Cached;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class SampleCachedService {

  @Cached
  public String simulateLongRunningTask(String value) throws InterruptedException {
    TimeUnit.SECONDS.sleep(2L);
    return "test " + value;
  }
}
