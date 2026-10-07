package com.example.demo.controller;

import java.util.List;
import java.util.Optional;

import com.example.demo.dto.*;
import com.example.demo.exception.ApiException;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

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
    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }
    
    @PostMapping("/signup")
    public AuthResponse signUpUser(@RequestBody SignUpRequest request) {
        return userService.signUpUser(request);
    }

    @PostMapping ("/login")
    public AuthResponse loginUser(@RequestBody LoginRequest request) {
        return userService.loginUser(request);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logoutUser(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        userService.logoutUser(authorization.substring(7));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public AccountResponse updateCurrentAccount(
            @AuthenticationPrincipal UserResponse user,
            @RequestBody AccountRequest accountRequest
    ) {
        return userService.updateCurrentAccount(user.id(), accountRequest);
    }

    @GetMapping ("/me")
    @PreAuthorize("isAuthenticated()")
    public Optional<AccountResponse> getCurrentAccount(@AuthenticationPrincipal UserResponse user) {
        return userService.getCurrentAccount(user.id());
    }
}
