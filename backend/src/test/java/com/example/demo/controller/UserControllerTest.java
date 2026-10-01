package com.example.demo.controller;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.example.demo.config.SecurityConfig;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.SignUpRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.exception.ApiException;
import com.example.demo.service.UserService;
import tools.jackson.databind.ObjectMapper;
import com.example.demo.dao.UserDao;

@WebMvcTest (UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {
  @Autowired 
  MockMvc mockMvc;
  @MockitoBean 
  UserService userService;

  @Test
  public void signUp_withValidAnonymousRequest_returnsUserResponse() throws Exception{
      String name = "test-name";
      String email = "test@gmail.com";
      String password = "TestP@ssw0rd";
      String apiUrl = "/api/users/signup";

      SignUpRequest request = new SignUpRequest(name, email, password);
      UserResponse user = new UserResponse(
          1L,
          name,
          email,
          "Customer"
      );

      AuthResponse response = new AuthResponse(
          "test-token",
          user
      );  
      when(userService.signUpUser(request)).thenReturn(response);

      ObjectMapper om = new ObjectMapper();
      String jsonRequest = om.writeValueAsString(request);
      mockMvc.perform(MockMvcRequestBuilders.post(apiUrl).content(jsonRequest)
          .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.id").value(1L))
        .andExpect(jsonPath("$.user.name").value(name))
        .andExpect(jsonPath("$.user.email").value(email));
  }

  @Test
  public void signUp_withExistingEmail_returnsConflict() throws Exception{
      String name = "test-name";
      String email = "test@gmail.com";
      String password = "TestP@ssw0rd";
      String apiUrl = "/api/users/signup";

      SignUpRequest request = new SignUpRequest(name, email, password);
      when(userService.signUpUser(request)).thenThrow(ApiException.class);

      ObjectMapper om = new ObjectMapper();
      String jsonRequest = om.writeValueAsString(request);
      mockMvc.perform(MockMvcRequestBuilders.post(apiUrl).content(jsonRequest)
          .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isConflict());
  }

  @Test
  public void signUp_withNullName_returnsBadRequest() throws Exception{
      String name = null;
      String email = "test@gmail.com";
      String password = "TestP@ssw0rd";
      String apiUrl = "/api/users/signup";

      SignUpRequest request = new SignUpRequest(name, email, password);
      when(userService.signUpUser(request)).thenThrow(ApiException.class);

      ObjectMapper om = new ObjectMapper();
      String jsonRequest = om.writeValueAsString(request);
      mockMvc.perform(MockMvcRequestBuilders.post(apiUrl).content(jsonRequest)
          .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }
  @Test
  public void signUp_withBadEmail_returnsBadRequest() throws Exception{
      String name = "test-name";
      String email = "test";
      String password = "TestP@ssw0rd";
      String apiUrl = "/api/users/signup";

      SignUpRequest request = new SignUpRequest(name, email, password);
      when(userService.signUpUser(request)).thenThrow(ApiException.class);

      ObjectMapper om = new ObjectMapper();
      String jsonRequest = om.writeValueAsString(request);
      mockMvc.perform(MockMvcRequestBuilders.post(apiUrl).content(jsonRequest)
          .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }
  @Test
  public void signUp_withBadPassword_returnsBadRequest() throws Exception{
      String name = "test-name";
      String email = "test@gmail.com";
      String password = "password";
      String apiUrl = "/api/users/signup";

      SignUpRequest request = new SignUpRequest(name, email, password);
      when(userService.signUpUser(request)).thenThrow(ApiException.class);

      ObjectMapper om = new ObjectMapper();
      String jsonRequest = om.writeValueAsString(request);
      mockMvc.perform(MockMvcRequestBuilders.post(apiUrl).content(jsonRequest)
          .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }
}
