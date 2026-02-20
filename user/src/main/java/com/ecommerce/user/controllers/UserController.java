package com.ecommerce.user.controllers;

import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.fetchAllUsers());
    }

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody UserRequest userRequest) {

        userService.addUser(userRequest);

        return ResponseEntity.ok("User added successfully");
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable(value = "id") Long id) {

        ResponseEntity<UserResponse> userResponseEntity = userService.fetchUser(id)
                .map((UserResponse userResponse) -> {
                    return ResponseEntity.ok(userResponse);
                }).orElseGet(() -> {
                    return ResponseEntity.notFound().build();
                });

        return userResponseEntity;
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable(value = "id") Long id,
                                             @RequestBody UserRequest userRequest) {

        boolean updatedUser = userService.updateUser(id, userRequest);
        if (updatedUser) {
            return ResponseEntity.ok("User updated successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
