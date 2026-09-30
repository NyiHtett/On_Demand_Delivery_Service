package com.example.demo.controller;

import java.util.List;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.SignUpRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpSession;


@RestController

// set url path for the controller
@RequestMapping("api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get mapping handles GET requests
     */
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
    @PostMapping("/signup")
    public UserResponse signUpUser(@RequestBody SignUpRequest request, HttpSession session) {
        UserResponse user = userService.signUpUser(request);
        session.setAttribute("email", user.email());
        return user;
    }

    @PostMapping ("/login")
    public UserResponse loginUser(@RequestBody LoginRequest request, HttpSession session) {
        UserResponse user = userService.loginUser(request);
        session.setAttribute("email", user.email());
        return user;
    }

    @GetMapping ("/account")
    public UserResponse accountUser(HttpSession session){
        return userService.getUserByEmail((String) session.getAttribute("email"));
    }


}
