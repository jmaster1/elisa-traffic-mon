package jmaster.etm.server.security;

import jmaster.core.security.LoginRedirectEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.CrossOriginOpenerPolicyHeaderWriter;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/login",
                                "/logout",
                                "/firebase-login",
                                "/favicon.ico",
                                "/static/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/.well-known/appspecific/com.chrome.devtools.json"
                        ).permitAll()
                        .anyRequest().hasRole(EtmUserRole.admin.name()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.crossOriginOpenerPolicy(coop -> coop.policy(
                        CrossOriginOpenerPolicyHeaderWriter.CrossOriginOpenerPolicy
                                .SAME_ORIGIN_ALLOW_POPUPS)))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new LoginRedirectEntryPoint("/login", "/admin", "/consumption")))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("SESSION", "JSESSIONID"))
                .build();
    }
}
