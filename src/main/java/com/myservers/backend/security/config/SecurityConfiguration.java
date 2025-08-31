package com.myservers.backend.security.config;


import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.OAuth2ClientDsl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {


    private final JwtAuthenticationFilter jwtAuthFilter;
private final AuthenticationProvider authenticationProvider;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("api/v1/auth/**","api/v1/files/**" ).permitAll()
                        .requestMatchers("api/v1/applications/icons/**").permitAll()
                        .requestMatchers("api/v1/applications/active").hasAnyAuthority("ADMIN","USER")
                        .requestMatchers("api/v1/applications/upload-icon").hasAuthority("ADMIN")
                        .requestMatchers("api/v1/applications/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("api/v1/applications/**").hasAuthority("ADMIN")
                        .requestMatchers("api/v1/applications/icons/**").permitAll()
                        .requestMatchers("api/v1/demo/**").hasAuthority("ADMIN")
                        .requestMatchers("api/v1/servers/basic/**").hasAnyAuthority("ADMIN","USER")
                        .requestMatchers("api/v1/servers/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("api/v1/statistics/admin/**").hasAnyAuthority("ADMIN","USER").anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
//                .httpBasic(Customizer.withDefaults())


        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token", "x-access-token", "cache-control", "pragma", "expires"));
        configuration.setExposedHeaders(Arrays.asList("x-auth-token","authorization", "x-access-token"));
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
