package com.caching.demo.cache;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;

public class ConcurrentExpiringCache<K, V> implements Cache<K, V> {

  private final Map<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();
  private final ScheduledExecutorService expirationScheduler =
      Executors.newSingleThreadScheduledExecutor();

  public ConcurrentExpiringCache() {
    expirationScheduler.scheduleAtFixedRate(this::expireEntries, 0, 1, TimeUnit.SECONDS);
  }

  @Override
  public void put(K key, V value, long expirationTimeMillis) {
    cache.put(key, new CacheEntry<>(value, System.currentTimeMillis() + expirationTimeMillis));
  }

  @Override
  public V get(K key) {
    CacheEntry<V> entry = cache.get(key);
    if (entry != null && !entry.isExpired()) {
      return entry.getValue();
    }
    return null;
  }

  @Override
  public void remove(K key) {
    cache.remove(key);
  }

  private void expireEntries() {
    cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
  }

  public void shutdown() {
    expirationScheduler.shutdown();
  }

  public Set<K> keySet() {
    return this.cache.keySet();
  }

  public static class CacheEntry<V> {

    @Getter private final V value;
    private final long expirationTime;

    public CacheEntry(V value, long expirationTime) {
      this.value = value;
      this.expirationTime = expirationTime;
    }

    public boolean isExpired() {
      return System.currentTimeMillis() > expirationTime;
    }
  }
}
