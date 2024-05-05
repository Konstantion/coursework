package com.konstantion.security.configuration;

import com.konstantion.security.jwt.JwtAuthorizationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    private static final String[] SECURITY_WHITELIST = {
            "/static/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/web-api/calls-websocket/**"
    };
    private static final String[] AUTHENTICATION_WHITELIST = {
            "/table-api/authentication/**",
            "/web-api/authentication/**"
    };
    private final AuthenticationManager authenticationManager;
    private final JwtAuthorizationFilter authorizationFilter;

    public SecurityConfig(AuthenticationManager authenticationManager, JwtAuthorizationFilter authorizationFilter) {
        this.authenticationManager = authenticationManager;
        this.authorizationFilter = authorizationFilter;
    }

    @Bean
    public SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers(AUTHENTICATION_WHITELIST).permitAll()
                .requestMatchers(SECURITY_WHITELIST).permitAll()
                .requestMatchers("/admin-api/**").hasRole("ADMIN")
                .requestMatchers("/table-api/**").hasRole("TABLE")
                .requestMatchers("/web-api/**").hasAnyRole("GUIDE", "ADMIN")
                .anyRequest().authenticated()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationManager(authenticationManager)
                .addFilterBefore(authorizationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        String hierarchy = "ROLE_ADMIN > ROLE_GUIDE";
        roleHierarchy.setHierarchy(hierarchy);
        return roleHierarchy;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(AUTHENTICATION_WHITELIST);
    }
}
