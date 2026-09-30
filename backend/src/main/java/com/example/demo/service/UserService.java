package com.example.demo.service;

import java.util.List;
import com.example.demo.dao.UserDao;
import com.example.demo.dto.SignUpRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.model.User;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    // Service
    public UserResponse loginUser(LoginRequest request) {
        String storedHash = userDao.getPasswordHashByEmail(request.email());

        if (storedHash == null || !passwordEncoder.matches(request.password(), storedHash)) {
            throw new RuntimeException("Invalid email or password");
        }

        return userDao.getUserByEmail(request.email());
    }

    public UserResponse signUpUser(SignUpRequest request) {
        if (request.name() == null)
            throw new RuntimeException("Name cannot be null");
        if (!request.email().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"))
            throw new RuntimeException("Email is not valid");
        if (!request.password().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,24}$"))
            throw new RuntimeException("Password does not match criteria of 1 special character, 1 number, 1 uppercase letter, 1 lowercase letter, between 8 and 24 characters in length");
        //hash request.password
        String hashedPassword = hashPassword(request.password());
        if (hashedPassword == null)
            throw new RuntimeException("Unable to hash password");

        //check if user exists 
        if (userDao.getUserByEmail(request.email()) != null)
            throw new RuntimeException("User with email" + request.email() + " already exists!");

        return userDao.signUpUser(request.name(), request.email(), hashedPassword);
    }

    public UserResponse getUserByEmail(String email) {
        if (email == null) {throw new RuntimeException("Invalid email");}
        return userDao.getUserByEmail(email);
    }

    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }
}
