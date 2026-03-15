package bf.kvill.associa.members.user;

import bf.kvill.associa.members.user.dto.ApproveMemberRequest;
import bf.kvill.associa.members.user.dto.CreateMemberRequest;
import bf.kvill.associa.members.user.dto.UpdateMemberRequest;
import bf.kvill.associa.members.user.policy.MemberApprovalPolicy;
import bf.kvill.associa.shared.exception.ResourceNotFoundException;
import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.shared.enums.MembershipStatus;
import bf.kvill.associa.core.security.permission.PermissionService;
import bf.kvill.associa.system.association.AssociationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service pour la gestion des utilisateurs
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final AssociationRepository associationRepository;
    private final MemberApprovalPolicy approvalPolicy;
    private final PermissionService permissionService;
    private final PasswordEncoder passwordEncoder;

    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int TEMP_PASSWORD_LENGTH = 12;

    /**
     * Recherche un utilisateur par ID
     */
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findByIdWithUserRoles(id)
                .or(() -> userRepository.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Transactional(readOnly = true)
    public User findByIdWithUserRoles(Long id) {
        return userRepository.findByIdWithUserRoles(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Recherche un utilisateur par email
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    /**
     * Recherche tous les utilisateurs d'une association
     */
    public List<User> findByAssociation(Long associationId) {
        return userRepository.findByAssociationId(associationId);
    }

    /**
     * Récupère les membres avec filtres et pagination
     * 
     * @param associationId ID de l'association
     * @param search        Recherche par nom/email (optionnel)
     * @param status        Filtre par statut (optionnel)
     * @param roleSlug      Filtre par rôle (optionnel)
     * @param pageable      Pagination
     * @return Page de membres
     */
    public Page<User> getMembers(
            Long associationId,
            String search,
            MembershipStatus status,
            String roleSlug,
            Pageable pageable) {
        // TODO: Implémenter filtres avancés avec Specification
        // Pour l'instant, retourne tous les membres de l'association
        if (status != null) {
            return userRepository.findByAssociationIdAndMembershipStatus(associationId, status, pageable);
        }
        return userRepository.findByAssociationId(associationId, pageable);
    }

    /**
     * Recherche les utilisateurs par statut d'adhésion
     */
    public List<User> findByMembershipStatus(Long associationId, MembershipStatus status) {
        return userRepository.findByAssociationIdAndMembershipStatus(associationId, status);
    }

    /**
     * Crée un nouvel utilisateur
     */
    @Transactional
    public User createUser(CreateMemberRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        Association association = associationRepository.findById(request.getAssociationId())
                .orElseThrow(() -> new ResourceNotFoundException("Association", request.getAssociationId()));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName((request.getFirstName()))
                .lastName(request.getLastName())
                .whatsapp(request.getWhatsapp())
                .interests(request.getInterests())
                .association(association)
                .membershipStatus(MembershipStatus.PENDING)
                .isSuperAdmin(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Created user: {} ({})", savedUser.getId(), savedUser.getEmail());

        return findByIdWithUserRoles(savedUser.getId());
    }

    @Transactional
    public User approveMember(Long userId, ApproveMemberRequest request) {
        User user = findById(userId);

        approvalPolicy.validateApproval(user);

        user.activateMembership();
        if (request.getMembershipDate() != null) {
            user.setMembershipDate(request.getMembershipDate());
        }

        User saved = userRepository.save(user);
        log.info("Membre approuvé: {}", userId);

        return saved;
    }

    /**
     * Invite un nouveau membre
     * Génère un mot de passe temporaire et envoie par email
     * 
     * @param email         Email du nouveau membre
     * @param firstName     Prénom
     * @param lastName      Nom
     * @param associationId ID de l'association
     * @param roleSlug      Slug du rôle à attribuer (optionnel)
     * @return Utilisateur créé avec mot de passe temporaire
     */
    @Transactional
    public User inviteMember(String email, String firstName, String lastName,
            Long associationId, String roleSlug) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
        }

        // Générer mot de passe temporaire
        String tempPassword = generateTemporaryPassword();

        // Créer l'utilisateur
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setMembershipStatus(MembershipStatus.PENDING);
        user.setIsSuperAdmin(false);
        // TODO: Définir association

        User savedUser = userRepository.save(user);

        // TODO: Attribuer rôle si roleSlug fourni
        // TODO: Envoyer email avec mot de passe temporaire

        log.info("Invited member: {} ({}) with temp password", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }

    /**
     * Génère un mot de passe temporaire aléatoire
     */
    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(TEMP_PASSWORD_LENGTH);

        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            int index = random.nextInt(TEMP_PASSWORD_CHARS.length());
            password.append(TEMP_PASSWORD_CHARS.charAt(index));
        }

        return password.toString();
    }

    /**
     * Met à jour un utilisateur
     */
    @Transactional
    public User updateUser(Long userId, UpdateMemberRequest request) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        // Mettre à jour les champs modifiables
        if (request.getFirstName() != null) {
            existingUser.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            existingUser.setLastName(request.getLastName());
        }
        if (request.getWhatsapp() != null) {
            existingUser.setWhatsapp(request.getWhatsapp());
        }
        if (request.getInterests() != null) {
            existingUser.setInterests(request.getInterests());
        }
        if (request.getProfilePicturePath() != null) {
            existingUser.setProfilePicturePath(request.getProfilePicturePath());
        }

        User savedUser = userRepository.save(existingUser);
        log.info("Updated user: {}", savedUser.getId());

        return findByIdWithUserRoles(savedUser.getId());
    }

    /**
     * Change le mot de passe d'un utilisateur
     */
    @Transactional
    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed for user: {}", userId);
    }

    /**
     * Change le mot de passe d'un utilisateur après vérification de l'ancien
     */
    @Transactional
    public void changePasswordWithCurrent(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mot de passe actuel incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed for user after verification: {}", userId);
    }

    /**
     * Vérifie l'email d'un utilisateur
     */
    @Transactional
    public User verifyEmail(Long userId) {
        User user = findById(userId);

        user.verifyEmail();
        User savedUser = userRepository.save(user);

        log.info("Email verified for user: {}", userId);
        return findByIdWithUserRoles(savedUser.getId());
    }

    /**
     * Active l'adhésion d'un utilisateur
     */
    @Transactional
    public User activateMembership(Long userId) {
        User user = findById(userId);

        user.activateMembership();
        User savedUser = userRepository.save(user);

        log.info("Membership activated for user: {}", userId);
        return findByIdWithUserRoles(savedUser.getId());
    }

    /**
     * Suspend un utilisateur
     */
    @Transactional
    public User suspendUser(Long userId) {
        User user = findById(userId);

        approvalPolicy.validateSuspension(user);

        user.suspend();
        User savedUser = userRepository.save(user);

        log.info("User suspended: {}", userId);
        return findByIdWithUserRoles(savedUser.getId());
    }

    /**
     * Met à jour le statut d'adhésion
     */
    @Transactional
    public User updateMembershipStatus(Long userId, MembershipStatus newStatus) {
        User user = findById(userId);

        MembershipStatus oldStatus = user.getMembershipStatus();
        user.setMembershipStatus(newStatus);

        // Si passage à ACTIVE, définir la date d'adhésion
        if (newStatus == MembershipStatus.ACTIVE && user.getMembershipDate() == null) {
            user.setMembershipDate(LocalDate.now());
        }

        User savedUser = userRepository.save(user);

        log.info("Membership status updated for user {}: {} -> {}", userId, oldStatus, newStatus);
        return findByIdWithUserRoles(savedUser.getId());
    }

    /**
     * Supprime un utilisateur (soft delete)
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = findById(userId);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User soft deleted: {}", userId);
    }

    /**
     * Compte les membres actifs d'une association
     */
    public long countActiveMembers(Long associationId) {
        return userRepository.countByAssociationIdAndMembershipStatus(
                associationId,
                MembershipStatus.ACTIVE);
    }

    /**
     * Vérifie si un utilisateur a une permission
     */
    public boolean hasPermission(User user, String permission) {
        return permissionService.hasPermission(user, permission);
    }

    /**
     * Récupère toutes les permissions d'un utilisateur
     */
    public List<String> getUserPermissions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        return new ArrayList<>(permissionService.getUserPermissions(user.getId()));
    }

}
