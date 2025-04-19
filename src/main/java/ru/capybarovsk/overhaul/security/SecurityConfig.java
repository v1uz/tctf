package ru.capybarovsk.overhaul.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.capybarovsk.overhaul.dao.UserDao;

import static org.springframework.security.config.http.SessionCreationPolicy.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CookieFactory cookieFactory;
    private final UserDao userDao;

    public SecurityConfig(CookieFactory cookieFactory, UserDao userDao) {
        this.cookieFactory = cookieFactory;
        this.userDao = userDao;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/account/**", "/error", "/assets/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new CookieAuthenticationFilter(cookieFactory, userDao),
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendRedirect("/account/login");
                        }));

        return http.build();
    }
}

