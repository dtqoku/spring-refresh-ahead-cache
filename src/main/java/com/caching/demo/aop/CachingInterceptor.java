package com.caching.demo.aop;

import com.caching.demo.cache.Cache;
import com.caching.demo.cache.CacheKeyGenerator;
import java.lang.reflect.Method;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class CachingInterceptor {

  @Value("${cache.ttl.seconds}")
  Integer cacheTtl;

  private final Cache<String, Object> cacheManager;
  private final CacheKeyGenerator cacheKeyGenerator;

  @Around("@annotation(com.caching.demo.cache.Cached)")
  public Object cacheMethodResult(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
    Logger log = logger(proceedingJoinPoint);

    MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
    Method method = methodSignature.getMethod();
    Object[] args = proceedingJoinPoint.getArgs();
    String instanceName = proceedingJoinPoint.getTarget().getClass().getName();

    final var key = cacheKeyGenerator.generate(instanceName, method, args).toString();
    Object cachedData = cacheManager.get(key);

    if (cachedData != null) {
      log.info("Data retrieved from cache.");
      return cachedData;
    }

    log.info("Calling webservice");
    Object result = proceedingJoinPoint.proceed();

    cacheManager.put(key, result, Duration.ofSeconds(cacheTtl).toMillis());

    return result;
  }

  /**
   * Retrieves the {@link Logger} associated to the given {@link JoinPoint}.
   *
   * @param joinPoint join point we want the logger for.
   * @return {@link Logger} associated to the given {@link JoinPoint}.
   */
  private Logger logger(JoinPoint joinPoint) {
    return LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());
  }
}
