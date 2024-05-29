package com.caching.demo.cache;

import java.util.Set;

public interface Cache<K, V> {

  void put(K key, V value, long expirationTimeMillis);

  V get(K key);

  void remove(K key);

  Set<K> keySet();
}
