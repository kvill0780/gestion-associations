package bf.kvill.associa.security.config;

import bf.kvill.associa.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration Spring Security
 *
 * Responsabilités :
 * - Configurer JWT authentication
 * - Définir les endpoints publics/protégés
 * - Activer @PreAuthorize pour contrôle permissions
 * - Configurer CORS
 * - Désactiver sessions (stateless avec JWT)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Active @PreAuthorize, @Secured, etc.
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthFilter;
        private final UserDetailsService userDetailsService;

        @Value("${app.cors.allowed-origins:http://localhost:5174,http://127.0.0.1:5174}")
        private String allowedOrigins;

        /**
         * Configuration principale de sécurité
         */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // Désactiver CSRF (pas nécessaire avec JWT)
                                .csrf(csrf -> csrf.disable())

                                // Configuration CORS
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // Configuration des autorisations
                                .authorizeHttpRequests(auth -> auth
                                                // Preflight CORS
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                // Endpoints publics (pas d'authentification requise)
                                                .requestMatchers(
                                                                "/api/auth/login",
                                                                "/api/auth/register",
                                                                "/api/auth/refresh",
                                                                "/api/auth/forgot-password",
                                                                "/api/auth/reset-password",
                                                                "/api/public/**",
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui.html",
                                                                "/actuator/health")
                                                .permitAll()

                                                // Tous les autres endpoints nécessitent authentification
                                                .anyRequest().authenticated())

                                // Gestion des sessions : STATELESS (pas de session, tout en JWT)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Provider d'authentification
                                .authenticationProvider(authenticationProvider())

                                // Ajouter le filtre JWT AVANT UsernamePasswordAuthenticationFilter
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        /**
         * Provider d'authentification
         * Utilise UserDetailsService et PasswordEncoder
         */
        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        /**
         * AuthenticationManager pour login
         */
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        /**
         * Encodeur de mots de passe (BCrypt)
         *
         * Utilise BCrypt avec strength 12 (bon compromis sécurité/performance)
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(12);
        }

        /**
         * Configuration CORS (Cross-Origin Resource Sharing)
         *
         * EN PRODUCTION : Restreindre les origines autorisées
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // Origines autorisées (depuis application.properties)
                List<String> allowedOriginsList = Arrays.stream(allowedOrigins.split(","))
                                .map(String::trim)
                                .filter(origin -> !origin.isEmpty())
                                .toList();
                
                // Accept explicit origins and pattern-based entries (e.g. http://localhost:*)
                configuration.setAllowedOriginPatterns(allowedOriginsList);

                // Méthodes HTTP autorisées
                configuration.setAllowedMethods(Arrays.asList(
                                "GET",
                                "POST",
                                "PUT",
                                "DELETE",
                                "OPTIONS",
                                "PATCH"));

                // Headers autorisés
                configuration.setAllowedHeaders(Arrays.asList(
                                "*",
                                "Authorization",
                                "Content-Type",
                                "Accept",
                                "X-Requested-With"));

                // Headers exposés (accessibles côté client)
                configuration.setExposedHeaders(Arrays.asList(
                                "Authorization",
                                "X-Total-Count",
                                "X-Total-Pages"));

                // Bearer tokens are used. Keeping credentials disabled avoids wildcard+credentials conflicts.
                configuration.setAllowCredentials(false);

                // Durée de cache de la requête preflight (OPTIONS)
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}
