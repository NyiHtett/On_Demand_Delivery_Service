package com.example.demo.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.springframework.dao.DuplicateKeyException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.demo.dto.UserResponse;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserDao.class)
class UserDaoTest {
    @Autowired
    UserDao userDao;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    public void signUpAddsNewUser() {
      String name = "test-user";
      String email = "test@gmail.com";
      String password = "TestP@ssw0rd";

      UserResponse response = userDao.signUpUser(name, email, password);
      assertNotNull(response);
      assertEquals(name, response.name());
      assertEquals(email, response.email());
      assertEquals("Customer", response.userType());

      Integer count = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM users WHERE email = ?",
        Integer.class,
        "test@gmail.com"
      );

      assertEquals(1, count);
    }

    @Test
    public void signUpRejectsExistingEmail() {
      String name = "test-user";
      String email = "test@gmail.com";
      String password = "TestP@ssw0rd";

      jdbcTemplate.update(
        "INSERT INTO users (name, email, password_hash, user_type) VALUES (?, ?, ?, ?)",
        name,
        email,
        password,
        "Customer"
      );

      Integer count = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM users WHERE email = ?",
        Integer.class,
        email
      );
      assertEquals(1, count);

      assertThrows(
          DuplicateKeyException.class,
          () -> userDao.signUpUser(name, email, password)
      );
    }

}
