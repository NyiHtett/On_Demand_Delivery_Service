package com.example.demo.service;

import java.util.List;
import com.example.demo.dao.UserDao;
import com.example.demo.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserDao UserDao;

    public UserService(UserDao UserDao) {
        this.UserDao = UserDao;
    }

    public List<User> getAllUsers() {
        return UserDao.getAllUsers();
    }
}
