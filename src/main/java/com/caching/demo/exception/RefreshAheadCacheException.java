package com.caching.demo.exception;

public class RefreshAheadCacheException extends RuntimeException {

  public RefreshAheadCacheException(String message) {
    super(message);
  }

  public RefreshAheadCacheException(String message, Throwable cause) {
    super(message, cause);
  }
}
