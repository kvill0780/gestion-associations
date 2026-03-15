package bf.kvill.associa.members.mandate;

import bf.kvill.associa.members.mandate.dto.AssignPostRequest;
import bf.kvill.associa.members.mandate.dto.MandateResponseDto;
import bf.kvill.associa.members.mandate.dto.RevokeMandateRequest;
import bf.kvill.associa.members.mandate.mapper.MandateMapper;
import bf.kvill.associa.members.mandate.policy.MandateTransitionPolicy;
import bf.kvill.associa.members.mandate.policy.MandateValidationPolicy;
import bf.kvill.associa.shared.exception.*;
import bf.kvill.associa.members.post.Post;
import bf.kvill.associa.members.post.PostRepository;
import bf.kvill.associa.members.role.Role;
import bf.kvill.associa.members.role.RoleRepository;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.members.role.RoleService;
import bf.kvill.associa.system.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service pour la gestion des mandats
 * 
 * Gère l'attribution atomique des postes avec attribution optionnelle de rôles
 */
@Service
@RequiredArgsConstructor
public class MandateService {

    private static final Logger log = LoggerFactory.getLogger(MandateService.class);

    private final MandateRepository mandateRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoleService roleService;
    private final MandateValidationPolicy validationPolicy;
    private final MandateTransitionPolicy transitionPolicy;
    private final AuditService auditService;

    /**
     * Recherche un mandat par ID
     */
    public Mandate findById(Long id) {
        return mandateRepository.findByIdWithDetails(id)
                .or(() -> mandateRepository.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException("Mandata", id));
    }

    /**
     * Recherche les mandats actifs d'un utilisateur
     */
    public List<Mandate> findActiveUserMandates(Long userId) {
        return mandateRepository.findByUserIdAndActiveTrue(userId);
    }

    /**
     * Vérifie si un utilisateur a un mandat actif sur un poste
     */
    public boolean hasActiveMandate(Long userId, Long postId) {
        return mandateRepository.existsByUserIdAndPostIdAndActiveTrue(userId, postId);
    }

    /**
     * Recherche les mandats actifs d'une association
     */
    public List<Mandate> findActiveAssociationMandates(Long associationId) {
        return mandateRepository.findByAssociationIdAndActiveTrue(associationId);
    }

    public List<Mandate> findAllUserMandates(Long userId) {
        return mandateRepository.findByUserId(userId);
    }

    /**
     * Recherche les mandats en cours (actifs et non expirés)
     */
    public List<Mandate> findCurrentMandates(Long associationId) {
        return mandateRepository.findCurrentMandatesByAssociation(
                associationId,
                LocalDate.now());
    }

    /**
     * MÉTHODE CLÉ : Attribution d'un poste
     *
     * Transaction atomique qui :
     * 1. Valide la demande
     * 2. Désactive l'ancien mandat
     * 3. Crée le nouveau mandat
     * 4. Attribue le rôle (optionnel)
     * 5. Log dans l'audit
     */
    @Transactional
    public Mandate assignPost(
            Long userId,
            Long postId,
            LocalDate startDate,
            LocalDate endDate,
            Long assignedById,
            Boolean assignRole,
            Long roleOverrideId,
            String notes) {
        log.info("Attribution poste: user={}, post={}", userId, postId);

        // Étape 1 : Construire la requête de validation
        AssignPostRequest request = new AssignPostRequest();
        request.setUserId(userId);
        request.setPostId(postId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);

        // Étape 2 : Récupérer les entités
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Long associationId = post.getAssociation() != null ? post.getAssociation().getId() : null;

        validateUserAndPostAssociation(user, post);
        ensureActorCanManageAssociation(assignedById, associationId);

        // Étape 3 : Fermer ancien mandat de CET utilisateur (renouvellement)
        // Important: on ferme avant validation de disponibilité pour permettre un renouvellement
        // propre sur un poste avec maxOccupants=1.
        transitionPolicy.closeUserMandate(userId, postId)
                .ifPresent(oldMandate -> {
                    log.info("Ancien mandat {} fermé (renouvellement)", oldMandate.getId());
                });

        // Étape 4 : Validation métier (après fermeture éventuelle du mandat précédent)
        validationPolicy.validate(request);

        // Étape 5 : Créer nouveau mandat
        Mandate newMandate = Mandate.builder()
                .association(post.getAssociation())
                .user(user)
                .post(post)
                .startDate(startDate)
                .endDate(endDate)
                .active(true)
                .assignedById(assignedById)
                .assignRole(assignRole) // Tracer si un rôle sera attribué
                .notes(notes)
                .build();

        Mandate savedMandate = mandateRepository.save(newMandate);
        log.info("Nouveau mandat {} créé", savedMandate.getId());

        // Étape 6 : Attribuer rôle (optionnel)
        if (Boolean.TRUE.equals(assignRole)) {
            Long roleId = roleOverrideId != null
                    ? roleOverrideId
                    : getSuggestedRoleId(postId);

            if (roleId != null) {
                if (roleOverrideId != null) {
                    ensureRoleBelongsToAssociation(roleOverrideId, associationId);
                }
                roleService.assignRoleToUser(
                        userId,
                        roleId,
                        assignedById,
                        startDate,
                        endDate);

                // Enregistrer quel rôle a été attribué pour révocation future
                savedMandate.setAssignedRoleId(roleId);
                savedMandate = mandateRepository.save(savedMandate);

                log.info("Rôle {} attribué à user {} pour mandat {}",
                        roleId, userId, savedMandate.getId());
            }
        }

        // Étape 7 : Audit
        auditService.log(
                "ASSIGN_POST",
                "Mandate",
                savedMandate.getId(),
                assignedById,
                Map.of(
                        "userId", userId,
                        "postId", postId,
                        "postName", post.getName(),
                        "userName", user.getFullName()));

        return savedMandate;
    }

    /**
     * Révoque un mandat
     */
    @Transactional
    public Mandate revokeMandate(Long mandateId, RevokeMandateRequest request) {
        return revokeMandate(mandateId, request, null);
    }

    @Transactional
    public Mandate revokeMandate(Long mandateId, RevokeMandateRequest request, Long revokedById) {
        Mandate mandate = findById(mandateId);
        Long associationId = mandate.getAssociation() != null ? mandate.getAssociation().getId() : null;
        ensureActorCanManageAssociation(revokedById, associationId);

        if (!mandate.isActive()) {
            throw new IllegalStateException("Le mandat est déjà inactif");
        }

        mandate.deactivate();
        if (request.getEndDate() != null) {
            mandate.setEndDate(request.getEndDate());
        }

        Mandate saved = mandateRepository.save(mandate);
        log.info("🔒 Mandat {} révoqué", mandateId);

        // Révoquer automatiquement le rôle associé (sécurité)
        if (Boolean.TRUE.equals(mandate.getAssignRole()) && mandate.getAssignedRoleId() != null) {
            try {
                roleService.revokeRoleFromUser(mandate.getUser().getId(), mandate.getAssignedRoleId(), revokedById);
                log.info("Rôle {} révoqué pour user {} (fin de mandat)",
                        mandate.getAssignedRoleId(), mandate.getUser().getId());
            } catch (Exception e) {
                log.warn("Échec révocation rôle: {}", e.getMessage());
                // Ne pas faire échouer la révocation du mandat
            }
        }

        auditService.log(
                "REVOKE_MANDATE",
                "Mandate",
                mandateId,
                revokedById,
                Map.of("reason", request.getReason() != null ? request.getReason() : ""));

        return saved;
    }

    /**
     * Prolonge un mandat
     */
    @Transactional
    public Mandate extendMandate(Long mandateId, LocalDate newEndDate) {
        return extendMandate(mandateId, newEndDate, null);
    }

    @Transactional
    public Mandate extendMandate(Long mandateId, LocalDate newEndDate, Long extendedById) {
        Mandate mandate = findById(mandateId);
        Long associationId = mandate.getAssociation() != null ? mandate.getAssociation().getId() : null;
        ensureActorCanManageAssociation(extendedById, associationId);

        if (!mandate.isActive()) {
            throw new IllegalStateException("Impossible de prolonger un mandat inactif");
        }

        if (newEndDate.isBefore(mandate.getStartDate())) {
            throw new IllegalArgumentException("La nouvelle date de fin ne peut pas être avant la date de début");
        }

        mandate.setEndDate(newEndDate);
        Mandate saved = mandateRepository.save(mandate);

        log.info("Mandat {} prolongé jusqu'au {}", mandateId, newEndDate);

        return saved;
    }

    /**
     * Récupère le rôle suggéré pour un poste
     */
    private Long getSuggestedRoleId(Long postId) {
        Post post = postRepository.findByIdWithRoles(postId).orElse(null);
        if (post == null) {
            return null;
        }

        Role effectiveDefaultRole = post.getEffectiveDefaultRole();
        return effectiveDefaultRole != null ? effectiveDefaultRole.getId() : null;
    }

    // ==================== Méthodes Utilitaires ====================

    /**
     * Ferme le mandat actif d'un utilisateur pour un poste
     * Méthode publique recommandée pour les démissions ou révocations ciblées
     *
     * @param userId ID de l'utilisateur
     * @param postId ID du poste
     * @return Le mandat fermé (si existe)
     */
    @Transactional
    public Optional<Mandate> closeUserMandate(Long userId, Long postId) {
        return transitionPolicy.closeUserMandate(userId, postId);
    }

    /**
     * Récupère les utilisateurs occupant actuellement un poste
     *
     * @param postId ID du poste
     * @return Liste des utilisateurs avec mandat actif
     */
    public List<User> getCurrentHolders(Long postId) {
        return transitionPolicy.getCurrentHolders(postId);
    }

    /**
     * Vérifie si un poste est plein (a atteint maxOccupants)
     *
     * @param postId ID du poste
     * @return true si le poste est plein
     */
    public boolean isPostFull(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
        if (post.getMaxOccupants() == null || post.getMaxOccupants() <= 0) {
            return false;
        }

        long activeCount = mandateRepository.countCurrentActiveByPostId(postId, LocalDate.now());
        return activeCount >= post.getMaxOccupants();
    }

    /**
     * Compte le nombre de places disponibles pour un poste
     *
     * @param postId ID du poste
     * @return Nombre de places disponibles (0 si plein)
     */
    public int getAvailableSlots(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
        if (post.getMaxOccupants() == null || post.getMaxOccupants() <= 0) {
            return Integer.MAX_VALUE;
        }

        long activeCount = mandateRepository.countCurrentActiveByPostId(postId, LocalDate.now());
        int available = post.getMaxOccupants() - (int) activeCount;
        return Math.max(0, available);
    }

    private void validateUserAndPostAssociation(User user, Post post) {
        Long userAssociationId = user.getAssociation() != null ? user.getAssociation().getId() : null;
        Long postAssociationId = post.getAssociation() != null ? post.getAssociation().getId() : null;
        if (userAssociationId == null || postAssociationId == null || !userAssociationId.equals(postAssociationId)) {
            throw new IllegalArgumentException("L'utilisateur et le poste doivent appartenir à la même association");
        }
    }

    private void ensureRoleBelongsToAssociation(Long roleId, Long associationId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));
        Long roleAssociationId = role.getAssociation() != null ? role.getAssociation().getId() : null;
        if (roleAssociationId == null || !roleAssociationId.equals(associationId)) {
            throw new IllegalArgumentException("Le rôle sélectionné n'appartient pas à l'association du poste");
        }
    }

    private void ensureActorCanManageAssociation(Long actorId, Long associationId) {
        if (actorId == null) {
            return;
        }

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", actorId));
        if (actor.isSuperAdmin()) {
            return;
        }

        Long actorAssociationId = actor.getAssociation() != null ? actor.getAssociation().getId() : null;
        if (actorAssociationId == null || !actorAssociationId.equals(associationId)) {
            throw new AccessDeniedException("Action interdite hors de votre association");
        }
    }
}
