package com.example.demo.service;

import com.example.demo.controller.UserController.CreateUserRequest;
import com.example.demo.controller.UserController.UserResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    private final Map<Long, UserResponse> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public UserService() {
        users.put(1L, new UserResponse(1L, "Alice", "alice@example.com"));
        users.put(2L, new UserResponse(2L, "Bob", "bob@example.com"));
        idGenerator.set(3);
    }

    public List<UserResponse> findAll() {
        return new ArrayList<>(users.values());
    }

    public Optional<UserResponse> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public UserResponse create(CreateUserRequest request) {
        Long id = idGenerator.getAndIncrement();
        UserResponse user = new UserResponse(id, request.name(), request.email());
        users.put(id, user);
        return user;
    }

    public Optional<UserResponse> update(Long id, CreateUserRequest request) {
        if (!users.containsKey(id)) {
            return Optional.empty();
        }
        UserResponse updated = new UserResponse(id, request.name(), request.email());
        users.put(id, updated);
        return Optional.of(updated);
    }

    public boolean delete(Long id) {
        return users.remove(id) != null;
    }
}
