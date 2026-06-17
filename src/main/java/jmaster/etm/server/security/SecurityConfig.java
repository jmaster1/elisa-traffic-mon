package jmaster.etm.server.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jmaster.core.controller.AbstractController;
import jmaster.core.security.LoginRedirectEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.DefaultRedirectStrategy;

@Configuration
public class SecurityConfig {
    private static final DefaultRedirectStrategy REDIRECT_STRATEGY = new DefaultRedirectStrategy();

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/login",
                                "/favicon.ico",
                                "/static/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/.well-known/appspecific/com.chrome.devtools.json"
                        ).permitAll()
                        .anyRequest().hasRole(EtmUserRole.admin.name()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new LoginRedirectEntryPoint("/login", "/admin", "/consumption")))
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) ->
                                REDIRECT_STRATEGY.sendRedirect(
                                        request,
                                        response,
                                        LoginRedirectEntryPoint.consumeLoginRedirectUrl(
                                                request.getSession(false),
                                                "/consumption/report")))
                        .failureHandler((request, response, exception) ->
                                redirectWithFlash(
                                        request,
                                        response,
                                        "/login",
                                        AbstractController.ATTR_ERROR_MESSAGE,
                                        "Invalid username or password."))
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) ->
                                redirectWithFlash(
                                        request,
                                        response,
                        "/login",
                        AbstractController.ATTR_INFO_MESSAGE,
                        "You have signed out."))
                        .invalidateHttpSession(true)
                        .deleteCookies("SESSION", "JSESSIONID"))
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder,
            @Value("${etm.security.username:etm}") String username,
            @Value("${etm.security.password:etm}") String password
    ) {
        return new InMemoryUserDetailsManager(
                User.withUsername(username)
                        .password(passwordEncoder.encode(password))
                        .authorities(EtmUserRole.admin)
                        .build()
        );
    }

    private static void redirectWithFlash(
            HttpServletRequest request,
            HttpServletResponse response,
            String url,
            String attribute,
            String message
    ) throws java.io.IOException {
        request.getSession(true).setAttribute(attribute, message);
        REDIRECT_STRATEGY.sendRedirect(request, response, url);
    }
}
