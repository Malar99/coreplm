package com.coreplm.config;
import jakarta.servlet.http.HttpServletResponse;
import com.coreplm.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
            	    .authenticationEntryPoint((request, response, authException) -> {
            	        response.setContentType("application/json");
            	        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            	        response.getWriter().write("""
            	            {
            	              "status":401,
            	              "error":"Unauthorized",
            	              "message":"Authentication required",
            	              "path":"%s"
            	            }
            	            """.formatted(request.getRequestURI()));
            	    })
            	)
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/v1/health").permitAll()
                    .requestMatchers("/api/v1/auth/login").permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/users").permitAll()
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}