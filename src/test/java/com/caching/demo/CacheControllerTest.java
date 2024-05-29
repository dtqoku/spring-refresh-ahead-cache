package com.caching.demo;

import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class CacheControllerTest {

  private static final Logger logger = LoggerFactory.getLogger(CacheControllerTest.class);

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Test cached method and refresh ahead functionality")
  void testRefreshAheadCaches() throws Exception {
    Instant startTestTime = Instant.now();
    int max = 50;
    for (int i = 0; i < max; i++) {
      logger.info("Iteration '{}'", i);
      Instant iterationStart = Instant.now();
      long testTimePassed = Duration.between(startTestTime, iterationStart).getSeconds();

      if (testTimePassed < 8) {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/long-run-task/iteration1"))
            .andExpect(status().isOk())
            .andExpect(content().string("test iteration1"));
      }

      if (testTimePassed < 16) {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/long-run-task/iteration2"))
            .andExpect(status().isOk())
            .andExpect(content().string("test iteration2"));
      }

      if (testTimePassed < 17) {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/long-run-task/iteration3"))
            .andExpect(status().isOk())
            .andExpect(content().string("test iteration3"));
      }

      // first round we expect to call the webservice
      if (i > 0) {
        Instant finish = Instant.now();
        long durationSeconds = Duration.between(iterationStart, finish).getSeconds();
        // test fails in case service took more than 1s to respond
        assertTrue("Passed seconds was: " + durationSeconds, durationSeconds <= 1);
      }
      if (testTimePassed >= 17) {
        break;
      }
      TimeUnit.SECONDS.sleep(1L);
    }
  }
}
