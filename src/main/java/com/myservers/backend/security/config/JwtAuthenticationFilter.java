package com.myservers.backend.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myservers.backend.exceptions.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try{
        System.out.println("=== DEBUG: JWT Filter ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Request method: " + request.getMethod());

        final String authHeader = request.getHeader("authorization");
        System.out.println("Authorization header: " + authHeader);

        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No valid authorization header found");
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7);
        System.out.println("JWT token extracted");

        userEmail=jwtService.extractUserEmail(jwt);
        System.out.println("User email: " + userEmail);

        if(userEmail!=null && SecurityContextHolder.getContext().getAuthentication()==null)
        {
            System.out.println("Loading user details for: " + userEmail);
            UserDetails userDetails=this.userDetailsService.loadUserByUsername(userEmail);
            System.out.println("User authorities: " + userDetails.getAuthorities());

            if(jwtService.isTokenValid(jwt,userDetails)){
                System.out.println("Token is valid");
                UsernamePasswordAuthenticationToken authToken=new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                System.out.println("Setting authentication with authorities: " + authToken.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("Authentication set successfully");
            } else {
                System.out.println("Token is invalid");
            }
        } else {
            System.out.println("User email is null or authentication already exists");
        }

        System.out.println("=== END DEBUG: JWT Filter ===");
        filterChain.doFilter(request,response);
    } catch (ExpiredJwtException ex) {
            handleException(response, ex);
    } catch (JwtException | IllegalArgumentException e) {
        // Handle other JWT exceptions if needed
    }
}

    private void handleException(HttpServletResponse response, ExpiredJwtException ex) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write(
                new ObjectMapper().writeValueAsString(ErrorResponse.builder()
                        .message("Token expired")
                        .error(ex.getMessage())
                        .build())
        );
    }
}
