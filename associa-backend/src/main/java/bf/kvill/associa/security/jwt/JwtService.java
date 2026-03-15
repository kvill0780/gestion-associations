package bf.kvill.associa.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service pour la gestion des tokens JWT
 * 
 * Responsabilités :
 * - Génération access token (24h)
 * - Génération refresh token (30 jours)
 * - Validation et parsing tokens
 * - Extraction claims (userId, email, roles, etc.)
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    /**
     * Clé secrète pour signer les tokens
     * 
     * EN PRODUCTION : Utiliser une clé forte (256+ bits)
     * Exemple : openssl rand -base64 64
     */
    @Value("${jwt.secret:MySecretKeyForAssociaApplicationMustBe256BitsMinimumForHS256Algorithm}")
    private String secretKey;

    /**
     * Durée de vie access token (en millisecondes)
     * Par défaut : 24 heures
     */
    @Value("${jwt.access-token.expiration:86400000}")
    private Long accessTokenExpiration;

    /**
     * Durée de vie refresh token (en millisecondes)
     * Par défaut : 30 jours
     */
    @Value("${jwt.refresh-token.expiration:2592000000}")
    private Long refreshTokenExpiration;

    // ==================== GÉNÉRATION TOKENS ====================

    /**
     * Génère un access token pour un utilisateur
     * 
     * Claims inclus :
     * - sub : email (subject)
     * - userId : ID utilisateur
     * - associationId : ID association
     * - roles : Liste des rôles
     * - iat : Date émission
     * - exp : Date expiration
     */
    public String generateAccessToken(UserDetails userDetails, Long userId, Long associationId, String roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("associationId", associationId);
        claims.put("roles", roles);
        claims.put("type", "ACCESS");

        return generateToken(claims, userDetails.getUsername(), accessTokenExpiration);
    }

    /**
     * Génère un refresh token
     */
    public String generateRefreshToken(UserDetails userDetails, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "REFRESH");

        return generateToken(claims, userDetails.getUsername(), refreshTokenExpiration);
    }

    /**
     * Génère un token JWT avec claims personnalisés
     */
    private String generateToken(Map<String, Object> extraClaims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    // ==================== VALIDATION TOKENS ====================

    /**
     * Valide un token JWT
     * 
     * Vérifie :
     * - Signature valide
     * - Non expiré
     * - Username correspond
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si un token est expiré
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // ==================== EXTRACTION CLAIMS ====================

    /**
     * Extrait le username (email) du token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrait l'userId du token
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Extrait l'associationId du token
     */
    public Long extractAssociationId(String token) {
        return extractClaim(token, claims -> claims.get("associationId", Long.class));
    }

    /**
     * Extrait les rôles du token
     */
    public String extractRoles(String token) {
        return extractClaim(token, claims -> claims.get("roles", String.class));
    }

    /**
     * Extrait le type du token (ACCESS ou REFRESH)
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    /**
     * Extrait la date d'expiration
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrait un claim spécifique
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrait tous les claims du token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ==================== HELPERS ====================

    /**
     * Récupère la clé de signature
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Valide la structure d'un token sans vérifier l'expiration
     */
    public boolean isTokenStructureValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT structure: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Récupère le temps restant avant expiration (en secondes)
     */
    public long getExpirationTimeInSeconds(String token) {
        Date expiration = extractExpiration(token);
        Date now = new Date();
        return (expiration.getTime() - now.getTime()) / 1000;
    }
}
