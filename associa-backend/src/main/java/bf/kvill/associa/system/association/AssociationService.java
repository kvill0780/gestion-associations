package bf.kvill.associa.system.association;

import bf.kvill.associa.system.association.dto.*;
import bf.kvill.associa.members.user.dto.MemberSummaryDto;
import bf.kvill.associa.system.association.listener.AssociationCreatedEvent;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.mandate.Mandate;
import bf.kvill.associa.members.mandate.MandateRepository;
import bf.kvill.associa.members.post.Post;
import bf.kvill.associa.members.post.PostRepository;
import bf.kvill.associa.system.audit.AuditService;
import bf.kvill.associa.shared.enums.AssociationStatus;
import bf.kvill.associa.shared.enums.AssociationType;
import bf.kvill.associa.shared.exception.ResourceNotFoundException;
import bf.kvill.associa.shared.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service métier pour la gestion des associations
 */
@Service
@RequiredArgsConstructor
public class AssociationService {

    private static final Logger log = LoggerFactory.getLogger(AssociationService.class);

    private final AssociationRepository associationRepository;
    private final PostRepository postRepository;
    private final MandateRepository mandateRepository;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    // ==================== LECTURE ====================

    public Association findById(Long id) {
        return associationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Association", id));
    }

    public Association findBySlug(String slug) {
        return associationRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Association non trouvée : " + slug));
    }

    public List<Association> findAll() {
        return associationRepository.findAll();
    }

    public Page<Association> findAllPaginated(Pageable pageable) {
        return associationRepository.findAll(pageable);
    }

    public List<Association> findByStatus(String status) {
        AssociationStatus associationStatus = AssociationStatus.valueOf(status.toUpperCase());
        return associationRepository.findByStatusOrderByNameAsc(associationStatus);
    }

    public Page<Association> findByStatusPaginated(String status, Pageable pageable) {
        AssociationStatus associationStatus = AssociationStatus.valueOf(status.toUpperCase());
        return associationRepository.findByStatus(associationStatus, pageable);
    }

    public List<Association> findByType(String type) {
        AssociationType associationType = AssociationType.valueOf(type.toUpperCase());
        return associationRepository.findByTypeOrderByNameAsc(associationType);
    }

    public List<Association> findByTypeAndStatus(String type, String status) {
        AssociationType associationType = AssociationType.valueOf(type.toUpperCase());
        AssociationStatus associationStatus = AssociationStatus.valueOf(status.toUpperCase());
        return associationRepository.findByTypeAndStatus(associationType, associationStatus);
    }

    public List<Association> searchByName(String query) {
        return associationRepository.findByNameContainingIgnoreCase(query);
    }

    public boolean existsBySlug(String slug) {
        return associationRepository.existsBySlug(slug);
    }

    // ==================== CRÉATION ====================

    /**
     * Créer une nouvelle association
     *
     * IMPORTANT : Émet un événement AssociationCreatedEvent
     * qui déclenche la création automatique des rôles templates
     */
    @Transactional
    public Association createAssociation(CreateAssociationRequest request, User createdBy) {
        log.info("Création d'une nouvelle association : {}", request.getName());

        // Générer le slug
        String slug = SlugUtils.generateSlug(request.getName());

        // Vérifier unicité du slug
        if (associationRepository.existsBySlug(slug)) {
            // Ajouter un suffixe numérique si déjà pris
            int counter = 1;
            String originalSlug = slug;
            while (associationRepository.existsBySlug(slug)) {
                slug = originalSlug + "-" + counter;
                counter++;
            }
            log.info("Slug modifié pour unicité : {}", slug);
        }

        // Créer l'association
        Association association = Association.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .address(request.getAddress())
                .type(request.getType())
                .status(AssociationStatus.ACTIVE)
                .defaultMembershipFee(request.getDefaultMembershipFee())
                .membershipValidityMonths(request.getMembershipValidityMonths())
                .financeApprovalWorkflow(request.getFinanceApprovalWorkflow())
                .autoApproveMembers(request.getAutoApproveMembers())
                .foundedYear(request.getFoundedYear())
                .website(request.getWebsite())
                .createdBy(createdBy)
                .build();

        Association saved = associationRepository.save(association);

        log.info("✅ Association créée : {} (ID: {}, Slug: {})",
                saved.getName(), saved.getId(), saved.getSlug());

        // ⭐ ÉVÉNEMENT : Déclenche la création des rôles templates
        eventPublisher.publishEvent(new AssociationCreatedEvent(this, saved));
        log.info("Événement AssociationCreatedEvent émis");

        // Audit
        auditService.log(
                "CREATE_ASSOCIATION",
                "Association",
                saved.getId(),
                createdBy != null ? createdBy.getId() : null,
                Map.of(
                        "name", saved.getName(),
                        "slug", saved.getSlug(),
                        "type", saved.getType().toString()));

        return saved;
    }

    // ==================== MODIFICATION ====================

    @Transactional
    public Association updateAssociation(Long associationId, UpdateAssociationRequest request) {
        log.info("Mise à jour de l'association : {}", associationId);

        Association association = findById(associationId);

        // Mise à jour des champs
        if (request.getName() != null) {
            String newSlug = SlugUtils.generateSlug(request.getName());

            // Vérifier si le nouveau slug est disponible
            if (!newSlug.equals(association.getSlug()) &&
                    associationRepository.existsBySlug(newSlug)) {
                throw new IllegalArgumentException(
                        "Une association avec ce nom existe déjà (slug : " + newSlug + ")");
            }

            association.setName(request.getName());
            association.setSlug(newSlug);
        }

        if (request.getDescription() != null) {
            association.setDescription(request.getDescription());
        }

        if (request.getLogoPath() != null) {
            association.setLogoPath(request.getLogoPath());
        }

        if (request.getContactEmail() != null) {
            association.setContactEmail(request.getContactEmail());
        }

        if (request.getContactPhone() != null) {
            association.setContactPhone(request.getContactPhone());
        }

        if (request.getAddress() != null) {
            association.setAddress(request.getAddress());
        }

        if (request.getDefaultMembershipFee() != null) {
            association.setDefaultMembershipFee(request.getDefaultMembershipFee());
        }

        if (request.getMembershipValidityMonths() != null) {
            association.setMembershipValidityMonths(request.getMembershipValidityMonths());
        }

        if (request.getFinanceApprovalWorkflow() != null) {
            association.setFinanceApprovalWorkflow(request.getFinanceApprovalWorkflow());
        }

        if (request.getAutoApproveMembers() != null) {
            association.setAutoApproveMembers(request.getAutoApproveMembers());
        }

        if (request.getWebsite() != null) {
            association.setWebsite(request.getWebsite());
        }

        Association saved = associationRepository.save(association);

        log.info("✅ Association mise à jour : {}", associationId);

        // Audit
        auditService.log(
                "UPDATE_ASSOCIATION",
                "Association",
                associationId,
                (Long) null,
                Map.of("name", saved.getName()));

        return saved;
    }

    // ==================== SUPPRESSION ====================

    @Transactional
    public void deleteAssociation(Long associationId) {
        log.info("Suppression de l'association : {}", associationId);

        Association association = findById(associationId);

        // Vérifier qu'il n'y a pas de données critiques
        long activeMembers = association.countActiveMembers();
        if (activeMembers > 0) {
            throw new IllegalStateException(
                    String.format("Impossible de supprimer : %d membre(s) actif(s)", activeMembers));
        }

        association.setDeletedAt(LocalDateTime.now());
        associationRepository.save(association);

        log.info("✅ Association soft deleted : {}", associationId);

        // Audit
        auditService.log(
                "DELETE_ASSOCIATION",
                "Association",
                associationId,
                (Long) null,
                Map.of("name", association.getName()));
    }

    // ==================== CHANGEMENT DE STATUT ====================

    @Transactional
    public Association suspendAssociation(Long associationId) {
        log.info("Suspension de l'association : {}", associationId);

        Association association = findById(associationId);

        if (association.isSuspended()) {
            throw new IllegalStateException("L'association est déjà suspendue");
        }

        association.suspend();
        Association saved = associationRepository.save(association);

        log.info("✅ Association suspendue : {}", associationId);

        // Audit
        auditService.log(
                "SUSPEND_ASSOCIATION",
                "Association",
                associationId,
                (Long) null,
                Map.of("name", association.getName()));

        return saved;
    }

    @Transactional
    public Association activateAssociation(Long associationId) {
        log.info("🔓 Activation de l'association : {}", associationId);

        Association association = findById(associationId);

        if (association.isActive()) {
            throw new IllegalStateException("L'association est déjà active");
        }

        association.activate();
        Association saved = associationRepository.save(association);

        log.info("✅ Association activée : {}", associationId);

        // Audit
        auditService.log(
                "ACTIVATE_ASSOCIATION",
                "Association",
                associationId,
                (Long) null,
                Map.of("name", association.getName()));

        return saved;
    }

    @Transactional
    public Association archiveAssociation(Long associationId) {
        log.info("Archivage de l'association : {}", associationId);

        Association association = findById(associationId);

        if (association.isArchived()) {
            throw new IllegalStateException("L'association est déjà archivée");
        }

        association.archive();
        Association saved = associationRepository.save(association);

        log.info("✅ Association archivée : {}", associationId);

        // Audit
        auditService.log(
                "ARCHIVE_ASSOCIATION",
                "Association",
                associationId,
                (Long) null,
                Map.of("name", association.getName()));

        return saved;
    }

    // ==================== STATISTIQUES ====================

    public AssociationStatsDto getAssociationStats(Long associationId) {
        Association association = findById(associationId);

        long totalMembers = association.countTotalMembers();
        long activeMembers = association.countActiveMembers();
        long totalPosts = association.countPosts();
        long activeMandates = association.countActiveMandates();

        return AssociationStatsDto.builder()
                .associationId(associationId)
                .associationName(association.getName())
                .totalMembers(totalMembers)
                .activeMembers(activeMembers)
                .inactiveMembers(totalMembers - activeMembers)
                .totalPosts(totalPosts)
                .activeMandates(activeMandates)
                .totalRoles(association.countRoles())
                .status(association.getStatus())
                .type(association.getType())
                .createdAt(association.getCreatedAt())
                .build();
    }

    // ==================== BUREAU EXÉCUTIF ====================

    public List<ExecutiveBoardMemberDto> getExecutiveBoard(Long associationId) {
        Association association = findById(associationId);

        // Récupérer les postes exécutifs
        List<Post> executivePosts = postRepository
                .findByAssociationIdAndIsExecutiveTrueOrderByDisplayOrderAsc(associationId);

        LocalDate today = LocalDate.now();

        return executivePosts.stream()
                .map(post -> {
                    // Récupérer le mandat actif pour ce poste
                    List<Mandate> activeMandates = mandateRepository
                            .findByPostIdAndActiveTrue(post.getId());

                    if (activeMandates.isEmpty()) {
                        return ExecutiveBoardMemberDto.builder()
                                .postId(post.getId())
                                .postName(post.getName())
                                .vacant(true)
                                .build();
                    }

                    Mandate mandate = activeMandates.get(0);
                    User user = mandate.getUser();

                    return ExecutiveBoardMemberDto.builder()
                            .postId(post.getId())
                            .postName(post.getName())
                            .userId(user.getId())
                            .userFullName(user.getFullName())
                            .userEmail(user.getEmail())
                            .startDate(mandate.getStartDate())
                            .endDate(mandate.getEndDate())
                            .vacant(false)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ==================== MEMBRES ACTIFS ====================

    public List<MemberSummaryDto> getActiveMembers(Long associationId) {
        Association association = findById(associationId);

        return association.getMembers().stream()
                .filter(User::isActiveMember)
                .map(user -> MemberSummaryDto.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .membershipStatus(user.getMembershipStatus())
                        .membershipDate(user.getMembershipDate())
                        .profilePicturePath(user.getProfilePicturePath())
                        .build())
                .collect(Collectors.toList());
    }
}