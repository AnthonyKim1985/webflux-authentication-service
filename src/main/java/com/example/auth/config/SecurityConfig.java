package com.example.auth.config;

import com.example.auth.domain.constant.Role;
import com.example.auth.persistence.ReactiveUserRepository;
import com.example.auth.security.AuthReactiveAuthenticationManager;
import com.example.auth.security.AuthSecurityContextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * @author Anthony Jinhyuk Kim
 * @version 1.0.0
 * @since 2018-11-06
 */
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
    private final AuthSecurityContextRepository securityContextRepository;
    private final AuthReactiveAuthenticationManager authenticationManager;

    @Autowired
    public SecurityConfig(AuthSecurityContextRepository securityContextRepository,
                          AuthReactiveAuthenticationManager authenticationManager) {
        this.securityContextRepository = securityContextRepository;
        this.authenticationManager = authenticationManager;
    }

    @Bean
    public SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .authenticationManager(authenticationManager)
                .securityContextRepository(securityContextRepository)
                .authorizeExchange()
                .pathMatchers("/api/v?/admin/**").hasRole(Role.ADMIN.name())
                .pathMatchers("/api/v?/admin/**").authenticated()
                .pathMatchers("/api/v?/manager/**").hasRole(Role.MANAGER.name())
                .pathMatchers("/api/v?/manager/**").authenticated()
                .pathMatchers("/api/v?/staff/**").hasRole(Role.STAFF.name())
                .pathMatchers("/api/v?/staff/**").authenticated()
                .anyExchange().permitAll()
                .and()
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(ReactiveUserRepository repository) {
        return (username) -> repository.findByUsername(username)
                .map(user -> User.withUsername(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.getRoles().stream().map(Enum::name).toArray(String[]::new))
                        .accountExpired(!user.isAccountNonExpired())
                        .credentialsExpired(!user.isCredentialsNonExpired())
                        .disabled(!user.isEnabled())
                        .accountLocked(!user.isAccountNonLocked())
                        .build()
                );
    }
}
