package com.example.demo.controller;

import java.util.List;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController

// set url path for the controller
@RequestMapping("api/Users")
public class UserController {
    private final UserService UserService;

    public UserController(UserService UserService) {
        this.UserService = UserService;
    }

    /**
     * Get mapping handles GET requests
     */
    @GetMapping
    public List<User> getAllUsers() {
        return UserService.getAllUsers();
    }
}
