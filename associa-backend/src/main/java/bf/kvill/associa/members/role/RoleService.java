package bf.kvill.associa.members.role;

import bf.kvill.associa.members.role.dto.CreateRoleRequest;
import bf.kvill.associa.core.security.permission.PermissionService;
import bf.kvill.associa.members.role.dto.UpdateRoleRequest;
import bf.kvill.associa.shared.exception.ResourceNotFoundException;
import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.system.association.AssociationRepository;
import bf.kvill.associa.system.audit.AuditService;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service pour la gestion des rôles et permissions
 */
@Service
@RequiredArgsConstructor
public class RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleService.class);

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final AssociationRepository associationRepository;
    private final PermissionService permissionService;
    private final AuditService auditService;
    private final UserRepository userRepository;

    /**
     * Recherche un rôle par ID
     */
    public Role findById(Long id) {
        return roleRepository.findByIdWithAssociation(id)
                .or(() -> roleRepository.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    /**
     * Recherche tous les rôles d'une association
     */
    public List<Role> findByAssociation(Long associationId) {
        return roleRepository.findByAssociationIdOrderByDisplayOrderAsc(associationId);
    }

    /**
     * Recherche un rôle par slug et association
     */
    public Optional<Role> findBySlugAndAssociation(String slug, Long associationId) {
        return roleRepository.findBySlugAndAssociationId(slug, associationId);
    }

    /**
     * Recherche les rôles de leadership d'une association
     */
    public List<Role> findLeadershipRoles(Long associationId) {
        return roleRepository.findLeadershipRolesByAssociation(associationId);
    }

    /**
     * Crée un nouveau rôle à partir d'un DTO Request
     */
    @Transactional
    public Role createRole(CreateRoleRequest request) {
        return createRole(request, null);
    }

    @Transactional
    public Role createRole(CreateRoleRequest request, Long actorId) {
        ensureActorCanManageAssociation(actorId, request.getAssociationId());

        Association association = associationRepository.findById(request.getAssociationId())
                .orElseThrow(() -> new ResourceNotFoundException("Association", request.getAssociationId()));

        if (roleRepository.existsBySlugAndAssociationId(request.getSlug(), request.getAssociationId())) {
            throw new IllegalArgumentException("Un rôle avec ce slug existe déjà");
        }

        // Valider les permissions
        permissionService.validatePermissions(request.getPermissions());

        Role role = Role.builder()
                .association(association)
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .type(request.getType())
                .permissions(request.getPermissions())
                .isTemplate(request.getIsTemplate() != null ? request.getIsTemplate() : false)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();

        Role saved = roleRepository.save(role);
        log.info("Rôle créé: {} ({})", saved.getId(), saved.getName());

        return saved;
    }

    /**
     * Met à jour un rôle à partir d'un DTO Request
     * Enregistre automatiquement dans l'audit log (comme Laravel)
     */
    @Transactional
    public Role updateRole(Long roleId, UpdateRoleRequest request) {
        return updateRole(roleId, request, null);
    }

    @Transactional
    public Role updateRole(Long roleId, UpdateRoleRequest request, Long actorId) {
        Role role = findById(roleId);
        ensureActorCanManageAssociation(actorId, role.getAssociation().getId());

        if (request.getName() != null)
            role.setName(request.getName());
        if (request.getDescription() != null)
            role.setDescription(request.getDescription());
        if (request.getType() != null)
            role.setType(request.getType());
        if (request.getDisplayOrder() != null)
            role.setDisplayOrder(request.getDisplayOrder());

        if (request.getPermissions() != null) {
            permissionService.validatePermissions(request.getPermissions());
            role.setPermissions(request.getPermissions());
        }

        Role saved = roleRepository.save(role);
        log.info("Rôle mis à jour: {}", roleId);

        // Invalider le cache des permissions
        permissionService.invalidateAllPermissionsCache();

        return saved;
    }

    /**
     * Met à jour les permissions d'un rôle
     */
    @Transactional
    public Role updatePermissions(Long roleId, Map<String, Object> permissions) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Rôle non trouvé"));

        // Valider les permissions
        validatePermissions(permissions);

        // Convertir en Map<String, Boolean>
        Map<String, Boolean> typedPermissions = permissions.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (Boolean) e.getValue()));

        role.setPermissions(typedPermissions);
        Role savedRole = roleRepository.save(role);

        log.info("Updated permissions for role: {}", roleId);
        return savedRole;
    }

    /**
     * Supprime un rôle
     * Vérifie qu'aucun utilisateur actif n'a ce rôle (comme Laravel)
     * Enregistre automatiquement dans l'audit log
     */
    @Transactional
    public void deleteRole(Long roleId) {
        deleteRole(roleId, null);
    }

    @Transactional
    public void deleteRole(Long roleId, Long actorId) {
        Role role = findById(roleId);
        ensureActorCanManageAssociation(actorId, role.getAssociation().getId());

        long activeUsersCount = userRoleRepository.countByRoleIdAndIsActiveTrue(roleId);
        if (activeUsersCount > 0) {
            throw new IllegalStateException(
                    String.format("Impossible de supprimer le rôle : %d utilisateur(s) actif(s)", activeUsersCount));
        }

        roleRepository.delete(role);
        log.info("Rôle supprimé: {}", roleId);
    }

    /**
     * Attribue un rôle à un utilisateur
     */
    @Transactional
    @CacheEvict(value = { "user-permissions", "user-permissions-by-category" }, key = "#userId")
    public UserRole assignRoleToUser(Long userId, Long roleId, Long assignedById) {
        return assignRoleToUser(userId, roleId, assignedById, null, null);
    }

    /**
     * Attribue un rôle à un utilisateur avec période
     */
    @Transactional
    @CacheEvict(value = { "user-permissions", "user-permissions-by-category" }, key = "#userId")
    public UserRole assignRoleToUser(
            Long userId,
            Long roleId,
            Long assignedById,
            LocalDate termStart,
            LocalDate termEnd) {
        Role role = findById(roleId);
        User user = findUserById(userId);
        validateRoleAndUserAssociation(role, user);
        ensureActorCanManageAssociation(assignedById, role.getAssociation().getId());
        if (termStart != null && termEnd != null && termEnd.isBefore(termStart)) {
            throw new IllegalArgumentException("termEnd ne peut pas être antérieure à termStart");
        }

        Optional<UserRole> existing = userRoleRepository.findByUserIdAndRoleId(userId, roleId);

        UserRole userRole;
        if (existing.isPresent()) {
            userRole = existing.get();
            userRole.setIsActive(true);
            userRole.setTermStart(termStart);
            userRole.setTermEnd(termEnd);
            userRole.setAssignedById(assignedById);
            userRole.setAssignedAt(LocalDateTime.now());
        } else {
            userRole = UserRole.builder()
                    .userId(userId)
                    .roleId(roleId)
                    .assignedById(assignedById)
                    .assignedAt(LocalDateTime.now())
                    .termStart(termStart)
                    .termEnd(termEnd)
                    .isActive(true)
                    .build();
        }

        UserRole saved = userRoleRepository.save(userRole);
        log.info("Rôle {} attribué à user {}", roleId, userId);

        return saved;
    }

    /**
     * Révoque un rôle d'un utilisateur
     */
    @Transactional
    @CacheEvict(value = { "user-permissions", "user-permissions-by-category" }, key = "#userId")
    public void revokeRoleFromUser(Long userId, Long roleId) {
        revokeRoleFromUser(userId, roleId, null);
    }

    @Transactional
    @CacheEvict(value = { "user-permissions", "user-permissions-by-category" }, key = "#userId")
    public void revokeRoleFromUser(Long userId, Long roleId, Long revokedById) {
        Role role = findById(roleId);
        User user = findUserById(userId);

        validateRoleAndUserAssociation(role, user);
        ensureActorCanManageAssociation(revokedById, role.getAssociation().getId());

        UserRole userRole = userRoleRepository.findByUserIdAndRoleIdAndIsActiveTrue(userId, roleId)
                .orElseThrow(() -> new IllegalArgumentException("Attribution de rôle non trouvée"));

        userRole.setIsActive(false);
        userRole.setTermEnd(LocalDate.now());
        userRoleRepository.save(userRole);

        log.info("Rôle {} révoqué de user {}", roleId, userId);
    }

    /**
     * Récupère les rôles actifs d'un utilisateur
     */
    public List<UserRole> getActiveUserRoles(Long userId) {
        return userRoleRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .filter(UserRole::isCurrentlyValid)
                .toList();
    }

    /**
     * Valide les permissions d'un rôle
     */
    private void validatePermissions(Map<String, Object> permissions) {
        if (permissions == null) {
            return;
        }

        for (Map.Entry<String, Object> entry : permissions.entrySet()) {
            String permissionKey = entry.getKey();
            Object permissionValue = entry.getValue();

            // Vérifier que la valeur est un boolean
            if (!(permissionValue instanceof Boolean)) {
                throw new IllegalArgumentException(
                        String.format("La valeur de la permission '%s' doit être un boolean", permissionKey));
            }

            // Vérifier le format de la clé
            if (!permissionService.isValidPermission(permissionKey)) {
                throw new IllegalArgumentException(
                        String.format("Format de permission invalide: '%s'", permissionKey));
            }
        }
    }

    /**
     * Valide que le rôle et l'utilisateur appartiennent à la même association
     */
    private void validateRoleAndUserAssociation(Role role, User user) {
        Long roleAssociationId = role.getAssociation() != null ? role.getAssociation().getId() : null;
        Long userAssociationId = user.getAssociation() != null ? user.getAssociation().getId() : null;
        if (roleAssociationId == null || userAssociationId == null || !roleAssociationId.equals(userAssociationId)) {
            throw new IllegalArgumentException("Le rôle et l'utilisateur doivent appartenir à la même association");
        }
    }

    /**
     * Valide que l'acteur peut gérer l'association ciblée
     */
    private void ensureActorCanManageAssociation(Long actorId, Long associationId) {
        if (actorId == null) {
            return;
        }

        User actor = findUserById(actorId);
        if (actor.isSuperAdmin()) {
            return;
        }

        Long actorAssociationId = actor.getAssociation() != null ? actor.getAssociation().getId() : null;
        if (actorAssociationId == null || !actorAssociationId.equals(associationId)) {
            throw new AccessDeniedException("Action interdite hors de votre association");
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}
