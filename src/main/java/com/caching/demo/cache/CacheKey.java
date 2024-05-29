package com.caching.demo.cache;

import com.caching.demo.exception.RefreshAheadCacheException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CacheKey implements Serializable {

  private String instanceName;
  private String methodName;
  private Object[] parameters;
  private String[] parameterClazzNames;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CacheKey that)) {
      return false;
    }
    return Objects.equals(instanceName, that.instanceName)
        && Objects.equals(methodName, that.methodName)
        && Arrays.equals(parameters, that.parameters)
        && Arrays.equals(parameterClazzNames, that.parameterClazzNames);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(instanceName, methodName);
    result = 31 * result + Arrays.hashCode(parameterClazzNames);
    return result;
  }

  @Override
  public String toString() {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos); ) {
      oos.writeObject(this);
      oos.flush();
      byte[] objectAsBytes = baos.toByteArray();
      return Base64.getEncoder().encodeToString(objectAsBytes);
    } catch (IOException ex) {
      throw new RefreshAheadCacheException("Could not convert key to string!", ex);
    }
  }

  public static CacheKey deserialize(String key) {
    byte[] decodedKey = Base64.getDecoder().decode(key.getBytes());
    try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(decodedKey))) {
      return (CacheKey) ois.readObject();
    } catch (IOException | ClassNotFoundException ex) {
      throw new RefreshAheadCacheException("Could not transform key ", ex);
    }
  }
}
