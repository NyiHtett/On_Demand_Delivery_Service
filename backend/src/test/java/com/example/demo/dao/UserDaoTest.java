package com.example.demo.dao;

import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;

@JdbcTest 
public class UserDaoTest {
  UserDao userDao = new UserDao(null);
}
 