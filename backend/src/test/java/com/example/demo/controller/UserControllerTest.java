package com.example.demo.controller;

import org.springframework.boot.webmvc.test.autoconfigure.MockMvcAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest 
public class UserControllerTest {
  MockMvc mock = new MockMvc();
}
