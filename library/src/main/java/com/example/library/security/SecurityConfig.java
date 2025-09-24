package com.example.library.security;

import com.example.library.User.CustomLogoutHandler;
import com.example.library.User.UserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableMethodSecurity(prePostEnabled = true) //aktiverar preauthorize
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final CustomLogoutHandler customLogoutHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiRateLimitFilter apiRateLimitFilter;

    public SecurityConfig(UserDetailsService userDetailsService,
                          PasswordEncoder passwordEncoder,
                          CustomLogoutHandler customLogoutHandler,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          ApiRateLimitFilter apiRateLimitFilter) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.customLogoutHandler = customLogoutHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.apiRateLimitFilter = apiRateLimitFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                //  STATELESS SESSION  - Ingen server-session lagras
                // Säkerheten bygger helt på JWT-tokens
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Öppna endpoints som alla kan nå
                        .requestMatchers("/api/auth/**", "/api/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/books/**", "/api/authors/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()


                        // Admin-behörigheter
                        .requestMatchers(HttpMethod.POST, "/api/books/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/books/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/authors/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/authors/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/authors/**").hasRole("ADMIN")

                        // inloggade användare
                        .requestMatchers("/api/loans/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/*/loans").hasAnyRole("USER", "ADMIN")

                        //  FALLBACK - Alla andra endpoints kräver autentisering
                        .anyRequest().authenticated()
                )
                //  JWT-filter innan UsernamePasswordAuthenticationFilter - Validerar tokens före standardfilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(apiRateLimitFilter, JwtAuthenticationFilter.class)

                // Logout hantering - Blacklistar token vid utloggning
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .addLogoutHandler(customLogoutHandler)
                        .logoutSuccessHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_NO_CONTENT))
                )


                // Exception hantering
                .exceptionHandling(ex -> ex
                        // Om ingen är inloggad alls → 401 Unauthorized
                        .authenticationEntryPoint((req, res, e) -> { //fångar alla requests där ingen giltig authentication finns
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.getWriter().write("Unauthorized - logga in först");
                        })
                        // Om man är inloggad men saknar behörighet → 403 Forbidden
                        .accessDeniedHandler((req, res, e) -> { //fångar alla fall där man är autentiserad men saknar rättigheter
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.getWriter().write("Forbidden - du saknar behörighet");
                        })
                )

                // Säkerhetsheaders - Skydd mot web-attacker
                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).preload(true).maxAgeInSeconds(31536000)) // HSTS
                        .frameOptions(frame -> frame.deny()) // X-Frame-Options: Clickjacking-skydd
                        .contentSecurityPolicy(csp -> csp.policyDirectives( // CSP: XSS-skydd
                                "default-src 'self'; script-src 'self'; object-src 'none'; style-src 'self' 'unsafe-inline';"
                        ))
                );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}



// ALTERNATIV SESSION-BASERAD KONFIGURATION (EJ ANVÄND)
/*
@Bean
public SecurityFilterChain sessionBasedConfig(HttpSecurity http) throws Exception {
    return http
        // Aktivera sessions istället för stateless
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .maximumSessions(1)
            .maxSessionsPreventsLogin(false))

        // Cookie-baserad autentisering
        .formLogin(form -> form
            .loginProcessingUrl("/api/auth/login")
            .successHandler((req, res, auth) -> {
                HttpSession session = req.getSession(); // Skapa session
                session.setAttribute("user", auth.getPrincipal());
                res.setStatus(200);
            }))

        .logout(logout -> logout
            .logoutUrl("/api/auth/logout")
            .invalidateHttpSession(true) // Förstör session
            .deleteCookies("JSESSIONID"))
        .build();
}
*/