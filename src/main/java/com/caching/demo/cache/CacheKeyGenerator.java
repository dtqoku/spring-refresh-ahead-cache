package com.caching.demo.cache;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import lombok.NoArgsConstructor;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.lang.NonNull;

@NoArgsConstructor
public class CacheKeyGenerator implements KeyGenerator {

  @NonNull
  @Override
  public CacheKey generate(Object instance, Method method, Object... params) {
    String instanceName = instance.toString();
    String methodName = method.getName();
    Object[] parameters = method.getParameters();
    String[] parameterClazzNames = new String[parameters.length];

    for (int i = 0; i < parameters.length; i++) {
      parameterClazzNames[i] = ((Parameter) parameters[i]).getParameterizedType().getTypeName();
    }

    return new CacheKey(instanceName, methodName, params, parameterClazzNames);
  }
}
