package com.emersondev.security;

import com.emersondev.security.filter.JwtAuthenticationFilter;
import com.emersondev.security.jwt.JwtAuthenticationEntryPoint;
import com.emersondev.security.jwt.JwtTokenValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthenticationEntryPoint unauthorizedHandler;
  private final JwtTokenValidator jwtTokenValidator;
  private final UserDetailsService userDetailsService;

  public SecurityConfig(JwtAuthenticationEntryPoint unauthorizedHandler, JwtTokenValidator jwtTokenValidator, UserDetailsService userDetailsService) {
    this.unauthorizedHandler = unauthorizedHandler;
    this.jwtTokenValidator = jwtTokenValidator;
    this.userDetailsService = userDetailsService;
  }

  @Bean
  public JwtAuthenticationFilter authenticationJwtTokenFilter() {
    return new JwtAuthenticationFilter(jwtTokenValidator, userDetailsService);
  }

  @Bean
  public PasswordEncoder getPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth ->
                    auth    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            .requestMatchers("/api/auth/**").permitAll()
//                            .requestMatchers("/api/books/**").permitAll()  // Allow book browsing without authentication
//                            .requestMatchers("/api/categories/**").permitAll()
//                            .requestMatchers("/api/docs/**").permitAll()
//                            .requestMatchers("/swagger-ui/**").permitAll()
                            .anyRequest().authenticated()
            );
    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
