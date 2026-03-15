package bf.kvill.associa.core.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration du cache (Simple pour MVP, Redis pour production)
 */
@Configuration
@EnableCaching
public class CacheConfig {
    // Cache simple activé par application.properties: spring.cache.type=simple
}
