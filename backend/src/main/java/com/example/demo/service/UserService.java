package com.example.demo.service;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import java.util.List;
import com.example.demo.dao.UserDao;
import com.example.demo.dto.SignUpRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    public UserResponse signUpUser(SignUpRequest request) {
        
        //hash request.password
        String hashedPassword = hashPassword(request.password());
        if (hashedPassword == null)
            throw new RuntimeException("Unable to hash password");

        //check if user exists 
        if (userDao.getUserByEmail(request.email()) != null)
            throw new RuntimeException("User with email" + request.email() + " already exists!");

        return userDao.signUpUser(request.name(), request.email(), hashedPassword);
    }

    public String hashPassword(String password) {
        // Create an encoder with all the defaults
        Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        String result = encoder.encode(password);
        if (encoder.matches(password, result))        
            return result;
        return null;
    }
}
