//package com.myservers.backend.security.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.EnableWebMvc;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
////import com.mongodb.client.MongoClient;
////import com.mongodb.client.MongoClients;
//
//
//@Configuration
//@EnableWebMvc
//public class CorsConfig implements WebMvcConfigurer{
//
//
////    @Override
////	public void addCorsMappings(CorsRegistry registry) {
////	registry.addMapping("/**").allowedOrigins("http://localhost:4200").allowedMethods("GET","POST","PUT","DELETE");
////
////	}
////
////	@Bean
////	public WebMvcConfigurer corsConfigurer() {
////		return new WebMvcConfigurer() {
////			@Override
////			public void addCorsMappings(CorsRegistry registry) {
////				registry
////						.addMapping("/api/v1/servers/basic")
////						.allowedOrigins("http://localhost:4200");
////			}
////		};
////	}
//	}