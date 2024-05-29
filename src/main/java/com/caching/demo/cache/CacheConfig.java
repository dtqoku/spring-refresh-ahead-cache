package com.caching.demo.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CacheConfig {

  @Bean
  public CacheKeyGenerator cacheKeyGenerator() {
    return new CacheKeyGenerator();
  }

  @Bean
  public Cache<String, Object> cacheManager() {
    return new ConcurrentExpiringCache<>();
  }
}
