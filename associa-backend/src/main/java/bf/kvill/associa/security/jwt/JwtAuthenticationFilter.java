// ==================== modules/security/jwt/JwtAuthenticationFilter.java ====================

package bf.kvill.associa.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import bf.kvill.associa.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filtre JWT pour intercepter et valider les requêtes HTTP
 * 
 * Workflow :
 * 1. Extrait le token du header Authorization
 * 2. Valide le token via JwtService
 * 3. Charge l'utilisateur via UserDetailsService
 * 4. Authentifie l'utilisateur dans SecurityContext
 * 
 * Ce filtre s'exécute AVANT tous les contrôleurs
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Récupérer le header Authorization
        final String authHeader = request.getHeader("Authorization");

        // Vérifier format : "Bearer <token>"
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Pas de token → passer au filtre suivant
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extraire le token (enlever "Bearer ")
            final String jwt = authHeader.substring(7);

            // Extraire l'email (username) du token
            final String userEmail = jwtService.extractUsername(jwt);

            // Si email trouvé ET utilisateur pas encore authentifié
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Charger l'utilisateur depuis la base de données
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Valider le token
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // Créer l'objet d'authentification
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    // Ajouter les détails de la requête
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // Authentifier l'utilisateur dans le contexte de sécurité
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("✅ User authenticated: {}", userEmail);
                }
            }

        } catch (io.jsonwebtoken.ExpiredJwtException | io.jsonwebtoken.SignatureException
                | io.jsonwebtoken.MalformedJwtException | io.jsonwebtoken.UnsupportedJwtException e) {
            // Erreurs attendues de validation token (warn, pas error)
            log.warn("JWT validation failed: {}", e.getMessage());
        } catch (Exception e) {
            // Erreur inattendue (error)
            log.error("Unexpected JWT authentication error: {}", e.getMessage(), e);
            // Ne pas bloquer la requête, juste logger l'erreur
            // Spring Security gérera l'accès non autorisé
        }

        // Passer au filtre suivant
        filterChain.doFilter(request, response);
    }

}
