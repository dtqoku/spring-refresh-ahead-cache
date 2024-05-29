package com.caching.demo.resource;

import com.caching.demo.cache.Cached;
import com.caching.demo.client.UserServiceClient;
import com.caching.demo.resource.response.User;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path = "api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserResource {

  private final UserServiceClient userServiceClient;

  @Cached
  @GetMapping
  public List<User> getAllUsers() {
    return userServiceClient.getUsers();
  }

  @Cached
  @GetMapping("/{id}")
  public User getUser(@PathVariable("id") Integer id) {
    return getAllUsers().stream()
        .filter(user -> Objects.equals(user.id(), id))
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }
}
