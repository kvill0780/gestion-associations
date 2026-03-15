package bf.kvill.associa.core.config;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Configuration centrale des permissions système
 * Équivalent de config/permissions.php
 */
@Component
@Getter
public class PermissionsConfig {

    // ========== LISTE COMPLÈTE DES PERMISSIONS ==========
    
    public static final Set<String> ALL_PERMISSIONS = Set.of(
        // MEMBRES (5)
        "members.view",
        "members.create",
        "members.update",
        "members.delete",
        "members.approve",

        // FINANCES (6)
        "finances.view",
        "finances.create",
        "finances.update",
        "finances.delete",
        "finances.approve",
        "finances.export",

        // ÉVÉNEMENTS (5)
        "events.view",
        "events.create",
        "events.update",
        "events.delete",
        "events.manage",

        // DOCUMENTS (4)
        "documents.view",
        "documents.upload",
        "documents.delete",
        "documents.share",

        // MESSAGES (3)
        "messages.view",
        "messages.send",
        "messages.delete",

        // ANNONCES (4)
        "announcements.view",
        "announcements.create",
        "announcements.update",
        "announcements.delete",

        // VOTES (4)
        "votes.view",
        "votes.create",
        "votes.manage",
        "votes.cast",

        // GALERIE (3)
        "gallery.view",
        "gallery.upload",
        "gallery.delete",

        // ADMINISTRATION (5)
        "roles.manage",
        "posts.manage",
        "settings.view",
        "settings.update",
        "super_admin"
    );

    // ========== MACROS DE PERMISSIONS ==========
    
    public static final Map<String, Set<String>> MACROS = Map.ofEntries(
        Map.entry("members_all", Set.of(
            "members.view",
            "members.create",
            "members.update",
            "members.delete",
            "members.approve"
        )),

        Map.entry("finances_all", Set.of(
            "finances.view",
            "finances.create",
            "finances.update",
            "finances.delete",
            "finances.approve",
            "finances.export"
        )),

        Map.entry("events_all", Set.of(
            "events.view",
            "events.create",
            "events.update",
            "events.delete",
            "events.manage"
        )),

        Map.entry("documents_all", Set.of(
            "documents.view",
            "documents.upload",
            "documents.delete",
            "documents.share"
        )),

        Map.entry("messages_all", Set.of(
            "messages.view",
            "messages.send",
            "messages.delete"
        )),

        Map.entry("announcements_all", Set.of(
            "announcements.view",
            "announcements.create",
            "announcements.update",
            "announcements.delete"
        )),

        Map.entry("votes_all", Set.of(
            "votes.view",
            "votes.create",
            "votes.manage",
            "votes.cast"
        )),

        Map.entry("gallery_all", Set.of(
            "gallery.view",
            "gallery.upload",
            "gallery.delete"
        ))
    );

    // ========== CATÉGORIES POUR L'UI ==========
    
    public static final List<PermissionCategory> CATEGORIES = List.of(
        new PermissionCategory("members", "Membres", "users", List.of(
            "members.view", "members.create", "members.update", 
            "members.delete", "members.approve"
        )),

        new PermissionCategory("finances", "Finances", "dollar-sign", List.of(
            "finances.view", "finances.create", "finances.update", 
            "finances.delete", "finances.approve", "finances.export"
        )),

        new PermissionCategory("events", "Événements", "calendar", List.of(
            "events.view", "events.create", "events.update", 
            "events.delete", "events.manage"
        )),

        new PermissionCategory("documents", "Documents", "file", List.of(
            "documents.view", "documents.upload", "documents.delete", 
            "documents.share"
        )),

        new PermissionCategory("communication", "Communication", "message-circle", List.of(
            "messages.view", "messages.send", "messages.delete",
            "announcements.view", "announcements.create", 
            "announcements.update", "announcements.delete"
        )),

        new PermissionCategory("votes", "Votes", "check-square", List.of(
            "votes.view", "votes.create", "votes.manage", "votes.cast"
        )),

        new PermissionCategory("gallery", "Galerie", "image", List.of(
            "gallery.view", "gallery.upload", "gallery.delete"
        )),

        new PermissionCategory("administration", "Administration", "settings", List.of(
            "roles.manage", "posts.manage", "settings.view", "settings.update"
        ))
    );

    // ========== HELPER CLASS ==========
    
    public record PermissionCategory(
        String key,
        String label,
        String icon,
        List<String> permissions
    ) {}

    // ========== MÉTHODES UTILITAIRES ==========
    
    /**
     * Vérifie si une permission est valide
     */
    public boolean isValidPermission(String permission) {
        return ALL_PERMISSIONS.contains(permission) || MACROS.containsKey(permission) || "admin_all".equals(permission);
    }

    /**
     * Résout une macro en liste de permissions
     */
    public Set<String> resolveMacro(String macro) {
        if ("admin_all".equals(macro)) {
            // admin_all = toutes les permissions sauf super_admin
            return ALL_PERMISSIONS.stream()
                .filter(p -> !p.equals("super_admin"))
                .collect(java.util.stream.Collectors.toSet());
        }
        return MACROS.getOrDefault(macro, Set.of(macro));
    }

    /**
     * Explose un ensemble de permissions (résout les macros)
     */
    public Set<String> expandPermissions(Set<String> permissions) {
        Set<String> expanded = new HashSet<>();
        
        for (String permission : permissions) {
            if (MACROS.containsKey(permission) || "admin_all".equals(permission)) {
                expanded.addAll(resolveMacro(permission));
            } else {
                expanded.add(permission);
            }
        }
        
        return expanded;
    }
}
