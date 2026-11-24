package com.miniquests.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                // Stateless JWT(Bearer) 인증이라 쿠키/세션을 쓰지 않으므로 CSRF 보호 불필요
                .csrf(AbstractHttpConfigurer::disable) // NOSONAR
                // 세션 사용 안함
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 모든 요청: 인증된 사용자만 접근
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().authenticated()
                )
                // Authorization: Bearer <JWT> 사용, 검사
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(Customizer.withDefaults())
                )
                .build();
    }
}
