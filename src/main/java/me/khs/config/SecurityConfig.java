//package me.khs.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.web.server.SecurityWebFilterChain;
//
//@Configuration
//@EnableWebFluxSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
//        return http
//                .csrf(csrf -> csrf.disable()) // CSRF 끄기
//                .authorizeExchange(exchanges -> exchanges
//                        .pathMatchers("/auth/**").permitAll() // 로그인 경로 허용
//                        .anyExchange().authenticated() // 나머지는 인증 필요
//                )
//                .build();
//    }
//}
//
