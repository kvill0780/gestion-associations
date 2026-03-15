package bf.kvill.associa.core.config;

import bf.kvill.associa.shared.enums.RoleType;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Templates de rôles pré-configurés
 * Créés automatiquement pour chaque nouvelle association
 */
@Component
@Getter
public class RoleTemplatesConfig {

    public static final List<RoleTemplate> TEMPLATES = List.of(
        new RoleTemplate(
            "Président",
            "president",
            "Accès complet à toutes les fonctionnalités de l'association",
            RoleType.LEADERSHIP,
            true,
            0,
            Map.of("admin_all", true)
        ),

        new RoleTemplate(
            "Trésorier",
            "treasurer",
            "Gestion complète des finances et accès aux documents",
            RoleType.LEADERSHIP,
            true,
            0,
            Map.of(
                "finances_all", true,
                "members.view", true,
                "documents.view", true,
                "documents.upload", true
            )
        ),

        new RoleTemplate(
            "Secrétaire Général",
            "secretary",
            "Gestion administrative, membres et documents",
            RoleType.LEADERSHIP,
            true,
            0,
            Map.of(
                "members_all", true,
                "documents_all", true,
                "events.view", true,
                "events.create", true,
                "announcements.view", true,
                "announcements.create", true
            )
        ),

        new RoleTemplate(
            "Responsable Événements",
            "event_manager",
            "Création et gestion complète des événements",
            RoleType.LEADERSHIP,
            true,
            0,
            Map.of(
                "events_all", true,
                "members.view", true,
                "documents.view", true,
                "gallery.view", true,
                "gallery.upload", true
            )
        ),

        new RoleTemplate(
            "Responsable Communication",
            "communication_manager",
            "Gestion des annonces, messages et galerie",
            RoleType.LEADERSHIP,
            true,
            0,
            Map.of(
                "announcements_all", true,
                "messages.view", true,
                "messages.send", true,
                "gallery_all", true,
                "members.view", true
            )
        ),

        new RoleTemplate(
            "Membre",
            "member",
            "Accès de base pour tous les membres",
            RoleType.MEMBER,
            true,
            0,
            Map.of(
                "members.view", true,
                "events.view", true,
                "documents.view", true,
                "announcements.view", true,
                "messages.view", true,
                "gallery.view", true,
                "votes.view", true,
                "votes.cast", true
            )
        )
    );

    public record RoleTemplate(
        String name,
        String slug,
        String description,
        RoleType type,
        Boolean isTemplate,
        Integer displayOrder,
        Map<String, Boolean> permissions
    ) {}
}
