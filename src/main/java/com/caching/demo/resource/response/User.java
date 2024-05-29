package com.caching.demo.resource.response;

public record User(
    int id, String name, String username, String email, String phone, String website) {}
