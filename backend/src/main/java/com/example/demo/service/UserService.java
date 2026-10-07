package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import com.example.demo.dao.UserDao;
import com.example.demo.dto.AccountResponse;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.SignUpRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.demo.dto.AccountRequest;

@Service
public class UserService {
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public List<UserResponse> getAllUsers() {
        return userDao.getAllUsers();
    }

    @Transactional 
    public Optional<AccountResponse> getCurrentAccount(Long user_id) {
        return userDao.getCurrentAccountByUserId(user_id);
    }

    @Transactional
    public AuthResponse loginUser(LoginRequest request) {
        if (request == null ||
            request.email() == null ||
            request.password() == null ||
            request.email().isBlank() ||
            request.password().isBlank()) {
            throw new ApiException(
                HttpStatus.BAD_REQUEST,
                "Email and password are required."
            );
        }

        String email = request.email().trim().toLowerCase();
        String storedHash = userDao.getPasswordHashByEmail(email);

        if (storedHash == null ||
            !passwordEncoder.matches(request.password(), storedHash)) {
            throw new ApiException(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password."
            );
        }

        UserResponse user = userDao.getUserByEmail(email);
        String apiToken = userDao.createSession(user.id());
        return new AuthResponse(apiToken, user);
    }

    @Transactional
    public AuthResponse signUpUser(SignUpRequest request) {
        if (request == null) {
            throw new ApiException(
                HttpStatus.BAD_REQUEST,
                "Name, email, and password are required."
            );
        }

        if (request.name() == null || request.name().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Name is required.");
        }

        if (request.email() == null ||
            !request.email().matches(
                "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
            )) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email is not valid.");
        }

        if (request.password() == null || request.password().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password is required.");
        }
        String email = request.email().trim().toLowerCase();
        String hashedPassword = hashPassword(request.password());

        if (userDao.getUserByEmail(email) != null) {
            throw new ApiException(
                HttpStatus.CONFLICT,
                "A user with that email already exists."
            );
        }

        UserResponse user = userDao.signUpUser(
            request.name().trim(),
            email,
            hashedPassword
        );

        String apiToken = userDao.createSession(user.id());
        return new AuthResponse(apiToken, user);
    }

    public AccountResponse updateCurrentAccount(Long user_id, AccountRequest accountRequest) {
        String name = accountRequest.name().trim();
        String email = accountRequest.email().trim();
        String phone = accountRequest.phone().trim();
        String Address = accountRequest.address().trim();
        userDao.updateAccount(user_id, name, email, phone, Address);
        return userDao.getCurrentAccountByUserId(user_id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Account not found."));
    }

    public void logoutUser(String apiToken) {
        userDao.deleteSession(apiToken);
    }

    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }


}
