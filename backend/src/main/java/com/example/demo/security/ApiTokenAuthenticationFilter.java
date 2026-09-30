package com.example.demo.security;

import com.example.demo.dao.UserDao;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class ApiTokenAuthenticationFilter extends OncePerRequestFilter {
    private final UserDao userDao;

    public ApiTokenAuthenticationFilter(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization != null
            && authorization.startsWith("Bearer ")
            && SecurityContextHolder.getContext().getAuthentication() == null) {
            String apiToken = authorization.substring(7).trim();

            if (!apiToken.isEmpty()) {
                userDao.getUserBySessionId(apiToken).ifPresent(user -> {
                    var authority = new SimpleGrantedAuthority("ROLE_" + user.userType().toUpperCase());
                    var authentication = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(authority)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            }
        }

        filterChain.doFilter(request, response);
    }
}
