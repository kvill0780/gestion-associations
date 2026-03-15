package bf.kvill.associa.members.post;

import bf.kvill.associa.members.post.dto.*;
import bf.kvill.associa.members.mandate.Mandate;
import bf.kvill.associa.members.mandate.MandateRepository;
import bf.kvill.associa.members.role.Role;
import bf.kvill.associa.members.role.RoleRepository;
import bf.kvill.associa.members.role.dto.RoleSummaryDto;
import bf.kvill.associa.members.role.mapper.RoleMapper;
import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.system.association.AssociationRepository;
import bf.kvill.associa.system.audit.AuditService;
import bf.kvill.associa.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service métier pour la gestion des postes
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final AssociationRepository associationRepository;
    private final RoleRepository roleRepository;
    private final MandateRepository mandateRepository;
    private final AuditService auditService;
    private final RoleMapper roleMapper;

    // ==================== LECTURE ====================

    public Post findById(Long id) {
        return postRepository.findByIdWithAssociationAndRoles(id)
                .or(() -> postRepository.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException("Post", id));
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public Page<Post> findAllPaginated(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    public List<Post> findByAssociation(Long associationId) {
        return postRepository.findByAssociationIdOrderByDisplayOrderAsc(associationId);
    }

    public Page<Post> findByAssociationPaginated(Long associationId, Pageable pageable) {
        return postRepository.findByAssociationId(associationId, pageable);
    }

    public List<Post> findExecutivePostsByAssociation(Long associationId) {
        return postRepository.findByAssociationIdAndIsExecutiveTrueOrderByDisplayOrderAsc(associationId);
    }

    public List<Post> findAllExecutivePosts() {
        return postRepository.findByIsExecutiveTrueOrderByDisplayOrderAsc();
    }

    public List<Post> findActivePostsByAssociation(Long associationId) {
        return postRepository.findByAssociationIdAndIsActiveTrueOrderByDisplayOrderAsc(associationId);
    }

    // ==================== CRÉATION ====================

    @Transactional
    public Post createPost(CreatePostRequest request) {
        log.info("Création d'un nouveau poste : {}", request.getName());

        // Vérifier que l'association existe
        Association association = associationRepository.findById(request.getAssociationId())
                .orElseThrow(() -> new ResourceNotFoundException("Association", request.getAssociationId()));

        // Vérifier unicité du nom dans l'association
        if (postRepository.existsByNameAndAssociationId(request.getName(), request.getAssociationId())) {
            throw new IllegalArgumentException(
                    "Un poste avec ce nom existe déjà dans cette association");
        }

        Role defaultRole = resolveRoleForAssociation(request.getDefaultRoleId(), association.getId());

        // Créer le poste
        Post post = Post.builder()
                .association(association)
                .name(request.getName())
                .description(request.getDescription())
                .isExecutive(request.getIsExecutive() != null ? request.getIsExecutive() : false)
                .maxOccupants(request.getMaxOccupants() != null ? request.getMaxOccupants() : 1)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .mandateDurationMonths(request.getMandateDurationMonths())
                .requiresElection(request.getRequiresElection() != null ? request.getRequiresElection() : false)
                .isActive(true)
                .defaultRole(defaultRole)
                .build();

        if (defaultRole != null) {
            post.addSuggestedRole(defaultRole);
        }

        Post saved = postRepository.save(post);

        log.info("✅ Poste créé : {} (ID: {})", saved.getName(), saved.getId());

        // Audit
        auditService.log(
                "CREATE_POST",
                "Post",
                saved.getId(),
                (Long) null,
                Map.of(
                        "name", saved.getName(),
                        "associationId", association.getId(),
                        "isExecutive", saved.getIsExecutive()));

        return saved;
    }

    // ==================== MODIFICATION ====================

    @Transactional
    public Post updatePost(Long postId, UpdatePostRequest request) {
        log.info("Mise à jour du poste : {}", postId);

        Post post = findById(postId);

        // Mise à jour des champs
        if (request.getName() != null) {
            // Vérifier unicité
            if (!request.getName().equals(post.getName()) &&
                    postRepository.existsByNameAndAssociationId(request.getName(), post.getAssociation().getId())) {
                throw new IllegalArgumentException(
                        "Un poste avec ce nom existe déjà dans cette association");
            }
            post.setName(request.getName());
        }

        if (request.getDescription() != null) {
            post.setDescription(request.getDescription());
        }

        if (request.getIsExecutive() != null) {
            post.setIsExecutive(request.getIsExecutive());
        }

        if (request.getMaxOccupants() != null) {
            // Vérifier que la nouvelle limite ne crée pas de problème
            long currentActive = mandateRepository.countByPostIdAndActiveTrue(postId);
            if (request.getMaxOccupants() < currentActive) {
                throw new IllegalArgumentException(
                        String.format("Impossible de réduire la limite à %d : %d mandats actifs existent",
                                request.getMaxOccupants(), currentActive));
            }
            post.setMaxOccupants(request.getMaxOccupants());
        }

        if (request.getDisplayOrder() != null) {
            post.setDisplayOrder(request.getDisplayOrder());
        }

        if (request.getMandateDurationMonths() != null) {
            post.setMandateDurationMonths(request.getMandateDurationMonths());
        }

        if (request.getRequiresElection() != null) {
            post.setRequiresElection(request.getRequiresElection());
        }

        if (Boolean.TRUE.equals(request.getClearDefaultRole())) {
            post.setDefaultRole(null);
        }

        if (request.getDefaultRoleId() != null) {
            Role defaultRole = resolveRoleForAssociation(request.getDefaultRoleId(), post.getAssociation().getId());
            post.setDefaultRole(defaultRole);
            post.addSuggestedRole(defaultRole);
        }

        Post saved = postRepository.save(post);

        log.info("✅ Poste mis à jour : {}", postId);

        // Audit
        auditService.log(
                "UPDATE_POST",
                "Post",
                postId,
                (Long) null,
                Map.of("name", saved.getName()));

        return saved;
    }

    // ==================== SUPPRESSION ====================

    @Transactional
    public void deletePost(Long postId) {
        log.info("Suppression du poste : {}", postId);

        Post post = findById(postId);

        // Vérifier qu'aucun mandat actif n'existe
        long activeMandates = mandateRepository.countByPostIdAndActiveTrue(postId);
        if (activeMandates > 0) {
            throw new IllegalStateException(
                    String.format("Impossible de supprimer le poste : %d mandat(s) actif(s) existent",
                            activeMandates));
        }

        // Vérifier qu'aucun historique n'existe (optionnel)
        long totalMandates = mandateRepository.countByPostId(postId);
        if (totalMandates > 0) {
            log.warn("Suppression d'un poste avec {} mandat(s) dans l'historique", totalMandates);
        }

        postRepository.delete(post);

        log.info("✅ Poste supprimé : {}", postId);

        // Audit
        auditService.log(
                "DELETE_POST",
                "Post",
                postId,
                (Long) null,
                Map.of("name", post.getName()));
    }

    // ==================== ACTIVATION / DÉSACTIVATION ====================

    @Transactional
    public Post deactivatePost(Long postId) {
        log.info("🔒 Désactivation du poste : {}", postId);

        Post post = findById(postId);

        if (!post.isActive()) {
            throw new IllegalStateException("Le poste est déjà désactivé");
        }

        post.deactivate();
        Post saved = postRepository.save(post);

        log.info("✅ Poste désactivé : {}", postId);

        // Audit
        auditService.log(
                "DEACTIVATE_POST",
                "Post",
                postId,
                (Long) null,
                Map.of("name", post.getName()));

        return saved;
    }

    @Transactional
    public Post activatePost(Long postId) {
        log.info("Activation du poste : {}", postId);

        Post post = findById(postId);

        if (post.isActive()) {
            throw new IllegalStateException("Le poste est déjà actif");
        }

        post.activate();
        Post saved = postRepository.save(post);

        log.info("✅ Poste activé : {}", postId);

        // Audit
        auditService.log(
                "ACTIVATE_POST",
                "Post",
                postId,
                (Long) null,
                Map.of("name", post.getName()));

        return saved;
    }

    // ==================== GESTION RÔLES SUGGÉRÉS ====================

    @Transactional
    public void linkRoleToPost(Long postId, Long roleId) {
        log.info("Liaison rôle {} au poste {}", roleId, postId);

        Post post = findById(postId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));

        // Vérifier que le rôle et le poste sont dans la même association
        if (!post.getAssociation().getId().equals(role.getAssociation().getId())) {
            throw new IllegalArgumentException(
                    "Le rôle et le poste doivent appartenir à la même association");
        }

        post.addSuggestedRole(role);
        if (post.getDefaultRole() == null) {
            post.setDefaultRole(role);
        }
        postRepository.save(post);

        log.info("✅ Rôle {} lié au poste {}", roleId, postId);

        // Audit
        auditService.log(
                "LINK_ROLE_TO_POST",
                "Post",
                postId,
                (Long) null,
                Map.of("roleId", roleId, "roleName", role.getName()));
    }

    @Transactional
    public void unlinkRoleFromPost(Long postId, Long roleId) {
        log.info("Déliaison rôle {} du poste {}", roleId, postId);

        Post post = findById(postId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));

        post.removeSuggestedRole(role);

        if (post.getDefaultRole() != null && post.getDefaultRole().getId().equals(roleId)) {
            post.setDefaultRole(null);
        }

        postRepository.save(post);

        log.info("✅ Rôle {} délié du poste {}", roleId, postId);

        // Audit
        auditService.log(
                "UNLINK_ROLE_FROM_POST",
                "Post",
                postId,
                (Long) null,
                Map.of("roleId", roleId, "roleName", role.getName()));
    }

    public List<RoleSummaryDto> getSuggestedRoles(Long postId) {
        Post post = findById(postId);
        Long defaultRoleId = post.getDefaultRole() != null ? post.getDefaultRole().getId() : null;

        return post.getRoles().stream()
                .sorted(Comparator
                        .comparing((Role role) -> defaultRoleId != null && defaultRoleId.equals(role.getId()) ? 0 : 1)
                        .thenComparing(role -> role.getDisplayOrder() != null ? role.getDisplayOrder() : Integer.MAX_VALUE)
                        .thenComparing(role -> role.getId() != null ? role.getId() : Long.MAX_VALUE))
                .map(roleMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    private Role resolveRoleForAssociation(Long roleId, Long associationId) {
        if (roleId == null) {
            return null;
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));

        Long roleAssociationId = role.getAssociation() != null ? role.getAssociation().getId() : null;
        if (roleAssociationId == null || !roleAssociationId.equals(associationId)) {
            throw new IllegalArgumentException("Le rôle par défaut doit appartenir à la même association que le poste");
        }

        return role;
    }

    // ==================== TITULAIRES ACTUELS ====================

    public List<PostHolderDto> getCurrentHolders(Long postId) {
        Post post = findById(postId);

        List<Mandate> activeMandates = mandateRepository.findCurrentByPostId(postId, LocalDate.now());

        return activeMandates.stream()
                .map(mandate -> PostHolderDto.builder()
                        .mandateId(mandate.getId())
                        .userId(mandate.getUser().getId())
                        .userFullName(mandate.getUser().getFullName())
                        .userEmail(mandate.getUser().getEmail())
                        .startDate(mandate.getStartDate())
                        .endDate(mandate.getEndDate())
                        .build())
                .collect(Collectors.toList());
    }

    // ==================== STATISTIQUES ====================

    public PostStatsDto getPostStats(Long postId) {
        Post post = findById(postId);

        long totalMandates = mandateRepository.countByPostId(postId);
        long activeMandates = mandateRepository.countCurrentActiveByPostId(postId, LocalDate.now());
        long availableSlots = post.hasOccupantLimit()
                ? Math.max(0, post.getMaxOccupants() - activeMandates)
                : Long.MAX_VALUE;
        boolean canAcceptNewMandate = post.isActive() && (!post.hasOccupantLimit() || activeMandates < post.getMaxOccupants());

        return PostStatsDto.builder()
                .postId(postId)
                .postName(post.getName())
                .totalMandates(totalMandates)
                .activeMandates(activeMandates)
                .maxOccupants(post.getMaxOccupants())
                .availableSlots(availableSlots)
                .canAcceptNewMandate(canAcceptNewMandate)
                .isExecutive(post.isExecutive())
                .isActive(post.isActive())
                .suggestedRolesCount(post.getRoles().size())
                .build();
    }
}
