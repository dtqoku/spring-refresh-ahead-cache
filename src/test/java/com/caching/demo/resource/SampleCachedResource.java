package com.caching.demo.resource;

import com.caching.demo.service.SampleCachedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleCachedResource {

  private SampleCachedService sampleCachedService;

  @Autowired
  public SampleCachedResource(SampleCachedService sampleCachedService) {
    this.sampleCachedService = sampleCachedService;
  }

  @GetMapping("/long-run-task/{value}")
  public ResponseEntity<String> longRunTask(@PathVariable String value) throws InterruptedException {
    String result = sampleCachedService.simulateLongRunningTask(value);
    return ResponseEntity.ok(result);
  }
}
