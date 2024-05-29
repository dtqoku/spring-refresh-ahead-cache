package com.caching.demo.cache;

import com.caching.demo.exception.RefreshAheadCacheException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshAheadService {

  private final ApplicationContext applicationContext;
  private final Cache<String, Object> cacheManager;
  private final CacheKeyGenerator cacheKeyGenerator;
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  @Value("${cache.reload.interval.seconds:3}")
  Integer reloadIntervalSeconds;

  @EventListener(ApplicationReadyEvent.class)
  public void onStartup() {
    scheduler.scheduleAtFixedRate(this::reloadCache, 0, reloadIntervalSeconds, TimeUnit.SECONDS);
  }

  public void reloadCache() {
    List<CacheKey> keys = getKeysToReload();
    keys.forEach(this::reloadCacheForKey);
  }

  private List<CacheKey> getKeysToReload() {
    if (cacheManager.keySet().isEmpty()) {
      return getCachedMethodKeys();
    }
    return cacheManager.keySet().stream().map(CacheKey::deserialize).toList();
  }

  private List<CacheKey> getCachedMethodKeys() {
    return findCachedMethods().stream()
        .map(method -> cacheKeyGenerator.generate(method.getDeclaringClass().getName(), method))
        .toList();
  }

  public void reloadCacheForKey(CacheKey key) {
    try {
      Object[] parameters = key.getParameterClazzNames();
      log.debug(
          "Starting refresh for method '{}' and parameters '{}'",
          key.getMethodName(),
          key.getParameters());

      Object bean = getBeanForKey(key);

      if (bean == null) {
        log.warn("Cache {} could not be resolved!", key);
        return;
      }

      Method method = getMethodForKey(key, bean);
      Object cacheValue = method.invoke(bean, key.getParameters());

      log.info(
          "Finished refresh for method {} parameters '{}' with value '{}'",
          key.getMethodName(),
          parameters,
          cacheValue);
    } catch (Exception ex) {
      throw new RefreshAheadCacheException("Cache refresh error for key " + key, ex);
    }
  }

  private Object getBeanForKey(CacheKey key) throws ClassNotFoundException {
    Object proxyBean = applicationContext.getBean(Class.forName(key.getInstanceName()));
    if (proxyBean instanceof Advised advised) {
      try {
        return advised.getTargetSource().getTarget();
      } catch (Exception ex) {
        log.error("Failed to get target bean from proxy", ex);
        return null;
      }
    }
    return proxyBean;
  }

  private Method getMethodForKey(CacheKey key, Object bean) throws NoSuchMethodException {
    Class<?>[] methodClazzes =
        Arrays.stream(key.getParameterClazzNames())
            .map(this::getClassForName)
            .toArray(Class[]::new);
    return bean.getClass().getMethod(key.getMethodName(), methodClazzes);
  }

  private Class<?> getClassForName(String clazzName) {
    try {
      return Class.forName(clazzName);
    } catch (ClassNotFoundException ex) {
      throw new IllegalArgumentException(
          "Could not find Class '" + clazzName + "' for parameter!", ex);
    }
  }

  public List<Method> findCachedMethods() {
    List<Method> cachedMethods = new ArrayList<>();
    final MethodFilter methodFilter =
        m -> m.isAnnotationPresent(Cached.class) && m.getParameters().length == 0;
    String[] beanNames = applicationContext.getBeanDefinitionNames();

    for (String beanName : beanNames) {
      Object bean = applicationContext.getBean(beanName);
      Class<?> beanClass = bean.getClass();
      ReflectionUtils.doWithMethods(beanClass, cachedMethods::add, methodFilter);
    }
    return cachedMethods;
  }
}
