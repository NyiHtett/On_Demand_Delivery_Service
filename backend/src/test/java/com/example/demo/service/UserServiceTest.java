package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.demo.dao.UserDao;
import com.example.demo.dto.SignUpRequest;
import com.example.demo.dto.UserResponse;

public class UserServiceTest {
  UserDao userDao = mock(UserDao.class);
  UserService userService = new UserService(userDao);

  @Test 
  public void signUpStoresValidPasswordHash() {
    String name = "test-name";
    String email = "test@gmail.com";
    String password = "TestP@ssw0rd";

    when(userDao.getUserByEmail(email)).thenReturn(null);
    when(userDao.signUpUser(eq(name), eq(email), anyString()))
        .thenReturn(new UserResponse(1L, name, email));

    SignUpRequest request = new SignUpRequest(
      name, email, password);
    UserResponse response = userService.signUpUser(request);

    ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
    verify(userDao).signUpUser(eq(name), eq(email), hashCaptor.capture());

    assertTrue(
        new BCryptPasswordEncoder()
            .matches(password, hashCaptor.getValue())
    );
    assertTrue(response.id() == 1L);
    assertTrue(response.name().equals(name));
    assertTrue(response.email().equals(email));
  }

  @Test 
  public void signUpRejectsExistingEmail() {
    String name = "test-name";
    String email = "test@gmail.com";
    String password = "TestP@ssw0rd";

    when(userDao.getUserByEmail(email)).thenReturn(new UserResponse(1L, name, email));
    SignUpRequest request = new SignUpRequest(
      name, email, password);
    assertThrows(RuntimeException.class, () -> userService.signUpUser(request));
    //never called insert in DAO, rejected before
    verify(userDao, never()).signUpUser(anyString(), anyString(), anyString());
  }
  @Test 
  public void signUpRejectsNullName() {
    String name = null;
    String email = "test@gmail.com";
    String password = "TestP@ssw0rd";

    when(userDao.getUserByEmail(email)).thenReturn(null);
    SignUpRequest request = new SignUpRequest(
      name, email, password);
    assertThrows(RuntimeException.class, () -> userService.signUpUser(request));
    //never called insert in DAO, rejected before
    verify(userDao, never()).signUpUser(anyString(), anyString(), anyString());
  }

  @Test 
  public void signUpRejectsInvalidEmail() {
    String name = "test-name";
    String email = "testMail";
    String password = "TestP@ssw0rd";

    when(userDao.getUserByEmail(email)).thenReturn(null);
    SignUpRequest request = new SignUpRequest(
      name, email, password);
    assertThrows(RuntimeException.class, () -> userService.signUpUser(request));
    //never called insert in DAO, rejected before
    verify(userDao, never()).signUpUser(anyString(), anyString(), anyString());
  }

  @Test 
  public void signUpRejectsInvalidPassword() {
    String name = "test-name";
    String email = "test@gmail.com";
    String password = "password";

    when(userDao.getUserByEmail(email)).thenReturn(null);
    SignUpRequest request = new SignUpRequest(
      name, email, password);
    assertThrows(RuntimeException.class, () -> userService.signUpUser(request));
    //never called insert in DAO, rejected before
    verify(userDao, never()).signUpUser(anyString(), anyString(), anyString());
  }

}
