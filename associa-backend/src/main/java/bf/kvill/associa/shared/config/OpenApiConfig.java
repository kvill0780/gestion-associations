package bf.kvill.associa.shared.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Swagger / OpenAPI
 *
 * Accès : http://localhost:8080/swagger-ui/index.html
 *
 * Sécurité : JWT Bearer token
 * - Se connecter via POST /api/auth/login
 * - Copier l'accessToken retourné
 * - Cliquer "Authorize" dans Swagger, saisir : Bearer <token>
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "Associa API", version = "1.0.0", description = "Plateforme de gestion des associations étudiantes — API REST", contact = @Contact(name = "Kvill", email = "admin@associa.bf")), servers = {
        @Server(url = "http://localhost:8080", description = "Développement local")
}, security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT", in = SecuritySchemeIn.HEADER, description = "Token JWT — obtenu via POST /api/auth/login")
public class OpenApiConfig {
    // Toute la configuration est portée par les annotations au niveau classe
}
