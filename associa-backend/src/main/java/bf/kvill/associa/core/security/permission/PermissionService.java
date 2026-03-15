package bf.kvill.associa.core.security.permission;

import bf.kvill.associa.core.config.PermissionsConfig;
import bf.kvill.associa.members.role.Role;
import bf.kvill.associa.members.role.RoleRepository;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.members.role.UserRole;
import bf.kvill.associa.members.role.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service de gestion des permissions
 * 
 * Features :
 * - Vérification permissions avec cache Redis
 * - Résolution des macros (admin_all, finances_all, etc.)
 * - Validation et optimisation
 * - Support Super Admin
 */
@Service
@RequiredArgsConstructor
public class PermissionService {

    private static final Logger log = LoggerFactory.getLogger(PermissionService.class);

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionsConfig permissionsConfig;
    private final ObjectProvider<PermissionService> selfProvider;

    // ==================== VÉRIFICATION PERMISSIONS ====================

    /**
     * Vérifie si un utilisateur a une permission spécifique
     */
    public boolean hasPermission(User user, String permission) {
        if (user.isSuperAdmin()) {
            return true;
        }

        Set<String> userPermissions = proxiedSelf().getUserPermissions(user.getId());
        return userPermissions.contains(permission);
    }

    /**
     * Vérifie si un utilisateur a AU MOINS UNE des permissions
     */
    public boolean hasAnyPermission(User user, String... permissions) {
        if (user.isSuperAdmin())
            return true;

        Set<String> userPermissions = proxiedSelf().getUserPermissions(user.getId());
        return Arrays.stream(permissions)
                .anyMatch(userPermissions::contains);
    }

    /**
     * Vérifie si un utilisateur a TOUTES les permissions
     */
    public boolean hasAllPermissions(User user, String... permissions) {
        if (user.isSuperAdmin())
            return true;

        Set<String> userPermissions = proxiedSelf().getUserPermissions(user.getId());
        return Arrays.stream(permissions)
                .allMatch(userPermissions::contains);
    }

    // ==================== RÉCUPÉRATION PERMISSIONS ====================

    /**
     * Récupère toutes les permissions d'un utilisateur
     * Mise en cache Redis
     */
    @Cacheable(value = "user-permissions", key = "#userId")
    @Transactional(readOnly = true)
    public Set<String> getUserPermissions(Long userId) {
        User user = userRepository.findByIdWithUserRoles(userId).orElse(null);
        Long userAssociationId = user != null && user.getAssociation() != null
                ? user.getAssociation().getId()
                : null;
        List<UserRole> userRoles = userRoleRepository
                .findCurrentByUserIdWithRole(userId, LocalDate.now());

        log.debug("Loaded {} active roles for user {}", userRoles.size(), userId);

        if (userRoles.isEmpty()) {
            return Set.of();
        }

        Set<String> allPermissions = new HashSet<>();

        for (UserRole userRole : userRoles) {
            if (!userRole.isCurrentlyValid()) {
                continue;
            }
            Role role = userRole.getRole();
            if (role == null && userRole.getRoleId() != null) {
                role = roleRepository.findByIdWithAssociation(userRole.getRoleId()).orElse(null);
            }
            if (role == null) {
                log.debug("UserRole {} has no linked Role entity (roleId={})", userRole.getId(), userRole.getRoleId());
                continue;
            }

            Long roleAssociationId = role.getAssociation() != null ? role.getAssociation().getId() : null;
            if (userAssociationId != null && roleAssociationId != null && !userAssociationId.equals(roleAssociationId)) {
                log.warn("Role {} ignoré pour user {}: association mismatch (user={}, role={})",
                        role.getId(), userId, userAssociationId, roleAssociationId);
                continue;
            }

            Map<String, Boolean> permissions = role.getPermissions();
            if (permissions == null || permissions.isEmpty()) {
                log.debug("Role {} has empty permissions map", role.getId());
                continue;
            }

            Set<String> enabledPerms = permissions.entrySet().stream()
                    .filter(Map.Entry::getValue)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

            Set<String> expandedPerms = permissionsConfig.expandPermissions(enabledPerms);
            allPermissions.addAll(expandedPerms);
            log.debug("Role {} expanded permissions: {}", role.getId(), expandedPerms);
        }

        return allPermissions;
    }

    /**
     * Récupère les permissions groupées par catégorie
     */
    @Cacheable(value = "user-permissions-by-category", key = "#userId")
    @Transactional(readOnly = true)
    public Map<String, Set<String>> getUserPermissionsByCategory(Long userId) {
        Set<String> allPermissions = getUserPermissions(userId);

        Map<String, Set<String>> byCategory = new HashMap<>();

        PermissionsConfig.CATEGORIES.forEach(category -> {
            Set<String> categoryPerms = category.permissions().stream()
                    .filter(allPermissions::contains)
                    .collect(Collectors.toSet());

            if (!categoryPerms.isEmpty()) {
                byCategory.put(category.key(), categoryPerms);
            }
        });

        return byCategory;
    }

    // ==================== INVALIDATION CACHE ====================

    /**
     * Invalide le cache des permissions d'un utilisateur
     */
    @CacheEvict(value = { "user-permissions", "user-permissions-by-category" }, key = "#userId")
    public void invalidateUserPermissionsCache(Long userId) {
        log.info("🗑️ Cache permissions invalidé pour user ID: {}", userId);
    }

    /**
     * Invalide le cache de TOUS les utilisateurs
     */
    @CacheEvict(value = { "user-permissions", "user-permissions-by-category" }, allEntries = true)
    public void invalidateAllPermissionsCache() {
        log.warn("🗑️ TOUS les caches permissions invalidés");
    }

    // ==================== VALIDATION ====================

    /**
     * Valide un ensemble de permissions
     */
    public void validatePermissions(Map<String, Boolean> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            throw new IllegalArgumentException("Les permissions ne peuvent pas être vides");
        }

        for (String permission : permissions.keySet()) {
            if (!permissionsConfig.isValidPermission(permission)) {
                throw new IllegalArgumentException(
                        "Permission invalide : " + permission);
            }
        }
    }

    /**
     * Optimise les permissions en supprimant les redondances
     */
    public Map<String, Boolean> optimizePermissions(Map<String, Boolean> permissions) {
        Map<String, Boolean> optimized = new HashMap<>(permissions);

        // Si admin_all existe, supprimer toutes les autres
        if (optimized.getOrDefault("admin_all", false)) {
            optimized.clear();
            optimized.put("admin_all", true);
            return optimized;
        }

        // Pour chaque macro, supprimer les permissions individuelles
        for (String macro : PermissionsConfig.MACROS.keySet()) {
            if (optimized.getOrDefault(macro, false)) {
                Set<String> macroPerms = PermissionsConfig.MACROS.get(macro);
                macroPerms.forEach(optimized::remove);
            }
        }

        return optimized;
    }

    // ==================== GESTION RÔLES ====================

    /**
     * Assigne un rôle à un utilisateur
     */
    @Transactional
    @CacheEvict(value = { "user-permissions", "user-permissions-by-category" }, key = "#userId")
    public UserRole assignRoleToUser(Long userId, Long roleId) {
        Optional<UserRole> existing = userRoleRepository
                .findByUserIdAndRoleIdAndIsActiveTrue(userId, roleId);

        if (existing.isPresent()) {
            log.warn("⚠️ User {} a déjà le rôle {} actif", userId, roleId);
            return existing.get();
        }

        UserRole userRole = UserRole.builder()
                .userId(userId)
                .roleId(roleId)
                .isActive(true)
                .build();

        UserRole saved = userRoleRepository.save(userRole);
        log.info("✅ Rôle {} assigné à user {}", roleId, userId);

        return saved;
    }

    /**
     * Révoque un rôle d'un utilisateur
     */
    @Transactional
    @CacheEvict(value = { "user-permissions", "user-permissions-by-category" }, key = "#userId")
    public void revokeRoleFromUser(Long userId, Long roleId) {
        UserRole userRole = userRoleRepository
                .findByUserIdAndRoleIdAndIsActiveTrue(userId, roleId)
                .orElseThrow(() -> new IllegalStateException(
                        "User n'a pas ce rôle actif"));

        userRole.setIsActive(false);
        userRoleRepository.save(userRole);

        log.info("🚫 Rôle {} révoqué de user {}", roleId, userId);
    }

    /**
     * Révoque TOUS les rôles d'un utilisateur
     */
    @Transactional
    @CacheEvict(value = { "user-permissions", "user-permissions-by-category" }, key = "#userId")
    public void revokeAllRolesFromUser(Long userId) {
        List<UserRole> activeRoles = userRoleRepository
                .findByUserIdAndIsActiveTrue(userId);

        activeRoles.forEach(ur -> ur.setIsActive(false));
        userRoleRepository.saveAll(activeRoles);

        log.warn(" TOUS les rôles révoqués pour user {}", userId);
    }

    // ==================== HELPERS ====================

    /**
     * Vérifie si une permission est valide
     */
    public boolean isValidPermission(String permission) {
        return permissionsConfig.isValidPermission(permission);
    }

    /**
     * Retourne toutes les permissions disponibles
     */
    public Set<String> getAllAvailablePermissions() {
        return PermissionsConfig.ALL_PERMISSIONS;
    }

    /**
     * Retourne toutes les catégories de permissions
     */
    public List<PermissionsConfig.PermissionCategory> getPermissionCategories() {
        return PermissionsConfig.CATEGORIES;
    }

    private PermissionService proxiedSelf() {
        PermissionService proxy = selfProvider.getIfAvailable();
        return proxy != null ? proxy : this;
    }
}
