package com.caching.demo.client;

import com.caching.demo.resource.response.User;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "userClient", url = "https://jsonplaceholder.typicode.com")
public interface UserServiceClient {

  @GetMapping(value = "/users")
  List<User> getUsers();
}
