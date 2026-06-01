package jmaster.etm.server.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jmaster.core.controller.AbstractController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
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

import jmaster.system.user.UserRole;

@Configuration
public class SecurityConfig {
    private static final DefaultRedirectStrategy REDIRECT_STRATEGY = new DefaultRedirectStrategy();

    static final String[] PUBLIC_PATHS = {
            "/error",
            "/actuator/health",
            "/actuator/info"
    };
    static final String[] PUBLIC_GET_PATHS = {
            "/",
            "/client",
            "/client/debug",
            "/client/status",
            "/client/signup",
            "/favicon.ico",
            "/.well-known/appspecific/com.chrome.devtools.json",
            "/static/**"
    };
    static final String[] PUBLIC_POST_PATHS = {
            "/client/deviceAuth",
            "/client/signup"
    };

    @Bean
    @Order(0)
    SecurityFilterChain loginRedirectSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/login")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    @Order(1)
    SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/admin/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/admin/login").permitAll()
                        .anyRequest().hasRole(UserRole.admin.name()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .defaultSuccessUrl("/admin", true)
                        .failureHandler((request, response, exception) ->
                                redirectWithFlash(
                                        request,
                                        response,
                                        "/admin/login",
                                        AbstractController.ATTR_ERROR_MESSAGE,
                                        "Invalid username or password."))
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessHandler((request, response, authentication) ->
                                redirectWithFlash(
                                        request,
                                        response,
                                        "/admin/login",
                                        AbstractController.ATTR_INFO_MESSAGE,
                                        "You have signed out."))
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain appSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_PATHS).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_PATHS).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/locations/report").permitAll()
                        .requestMatchers("/client/**").hasRole(UserRole.client.name())
                        .requestMatchers("/api/locations", "/api/locations/**").hasRole(UserRole.client.name())
                        .anyRequest().authenticated())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder,
            @Value("${geolog.security.username:geolog}") String username,
            @Value("${geolog.security.password:geolog}") String password
    ) {
        return new InMemoryUserDetailsManager(
                User.withUsername(username)
                        .password(passwordEncoder.encode(password))
                        .authorities(UserRole.admin)
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
