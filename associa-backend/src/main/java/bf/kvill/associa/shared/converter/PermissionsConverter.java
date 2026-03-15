package bf.kvill.associa.shared.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convertisseur JPA pour Map<String, Boolean> ↔ JSONB
 *
 * Permet de stocker les permissions en JSONB dans PostgreSQL
 * et de les manipuler comme Map en Java
 */
@Converter
public class PermissionsConverter implements AttributeConverter<Map<String, Boolean>, String> {

    private static final Logger log = LoggerFactory.getLogger(PermissionsConverter.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Boolean> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return "{}";
        }

        try {
            return objectMapper.writeValueAsString(permissions);
        } catch (JsonProcessingException e) {
            log.error("Erreur conversion permissions → JSONB: {}", e.getMessage());
            return "{}";
        }
    }

    @Override
    public Map<String, Boolean> convertToEntityAttribute(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Boolean>>() {});
        } catch (JsonProcessingException e) {
            log.error("Erreur conversion JSONB → permissions: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}