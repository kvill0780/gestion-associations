package bf.kvill.associa.shared.config;

import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.members.post.Post;
import bf.kvill.associa.members.post.PostRepository;
import bf.kvill.associa.members.mandate.MandateService;
import bf.kvill.associa.members.role.Role;
import bf.kvill.associa.members.role.RoleRepository;
import bf.kvill.associa.members.role.RoleService;
import bf.kvill.associa.shared.enums.RoleType;
import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.system.association.AssociationRepository;
import bf.kvill.associa.shared.enums.AssociationStatus;
import bf.kvill.associa.shared.enums.AssociationType;
import bf.kvill.associa.shared.enums.MembershipStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AssociationRepository associationRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final MandateService mandateService;
    private final RoleRepository roleRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    @Value("${app.seed.default-password:password}")
    private String seedDefaultPassword;

    @Bean
    @Profile("dev")
    @Transactional
    CommandLineRunner initData() {
        return args -> {
            log.info("Starting data seeding...");

            // 0. Association système (pour le super admin — ne correspond à aucun vrai
            // club)
            Association systemAssociation = associationRepository.findBySlug("system-platform")
                    .orElseGet(() -> {
                        Association a = Association.builder()
                                .name("Associa Platform")
                                .slug("system-platform")
                                .description("Association système — ne pas supprimer")
                                .type(AssociationType.SYSTEM)
                                .status(AssociationStatus.ACTIVE)
                                .contactEmail("platform@associa.bf")
                                .autoApproveMembers(false)
                                .financeApprovalWorkflow(false)
                                .build();
                        log.info("Association système créée : {}", a.getName());
                        return associationRepository.save(a);
                    });
            log.info("Association système : id={}", systemAssociation.getId());

            // 1. Créer association C2I si n'existe pas
            Association c2i = associationRepository.findBySlug("c2i-ibam")
                    .orElseGet(() -> {
                        Association a = Association.builder()
                                .name("C2I - Club Informatique de l'IBAM")
                                .slug("c2i-ibam")
                                .description("Club Informatique de l'Institut Burkinabè des Arts et Métiers")
                                .type(AssociationType.STUDENT)
                                .status(AssociationStatus.ACTIVE)
                                .contactEmail("c2i@ibam.bf")
                                .contactPhone("+22665432100")
                                .autoApproveMembers(false)
                                .financeApprovalWorkflow(true)
                                .foundedYear(2018)
                                .build();
                        log.info("Association créée : {}", a.getName());
                        return associationRepository.save(a);
                    });
            log.info("Association C2I : id={}", c2i.getId());

            // 2. Créer Rôle Président pour C2I avec admin_all
            Role presidentRole = roleRepository.findBySlugAndAssociationId("president", c2i.getId())
                    .orElseGet(() -> {
                        Role r = Role.builder()
                                .association(c2i)
                                .name("Président")
                                .slug("president")
                                .description("Président de l'association avec tous les pouvoirs")
                                .type(RoleType.LEADERSHIP)
                                .permissions(Map.of("admin_all", true)) // Toutes les permissions
                                .displayOrder(1)
                                .isTemplate(false)
                                .build();
                        log.info("Rôle créé : {}", r.getName());
                        return roleRepository.save(r);
                    });
            log.info("Rôle Président C2I : id={}", presidentRole.getId());

            // 3. Président C2I
            if (!userRepository.existsByEmail("president.c2i@associa.bf")) {
                User president = User.builder()
                        .association(c2i)
                        .firstName("Président")
                        .lastName("C2I")
                        .email("president.c2i@associa.bf")
                        .password(passwordEncoder.encode(seedDefaultPassword))
                        .whatsapp("+22670000001")
                        .membershipStatus(MembershipStatus.ACTIVE)
                        .membershipDate(LocalDate.now())
                        .isSuperAdmin(false)
                        .build();

                User savedPresident = userRepository.save(president);
                roleService.assignRoleToUser(savedPresident.getId(), presidentRole.getId(), null);
                log.info("Président C2I créé : president.c2i@associa.bf");
            }

            // 4. Super admin — rattaché à l'association système
            if (!userRepository.existsByEmail("admin@associa.bf")) {
                User admin = User.builder()
                        .association(systemAssociation)
                        .firstName("Super")
                        .lastName("Admin")
                        .email("admin@associa.bf")
                        .password(passwordEncoder.encode(seedDefaultPassword))
                        .membershipStatus(MembershipStatus.ACTIVE)
                        .membershipDate(LocalDate.now())
                        .isSuperAdmin(true)
                        .build();

                userRepository.save(admin);
                log.info("Super Admin créé : admin@associa.bf");
            }

            // 5. Membre test
            if (!userRepository.existsByEmail("test@associa.bf")) {
                User testUser = User.builder()
                        .association(c2i)
                        .firstName("Test")
                        .lastName("User")
                        .email("test@associa.bf")
                        .password(passwordEncoder.encode(seedDefaultPassword))
                        .whatsapp("+22670111111")
                        .membershipStatus(MembershipStatus.ACTIVE)
                        .membershipDate(LocalDate.now())
                        .isSuperAdmin(false)
                        .build();

                userRepository.save(testUser);
                log.info("Test User créé : test@associa.bf");
            }

            // 6. Association de test avec bureau complet
            Association testAssociation = associationRepository.findBySlug("associa-test")
                    .orElseGet(() -> {
                        Association a = Association.builder()
                                .name("Associa Test")
                                .slug("associa-test")
                                .description("Association de test pour valider les workflows")
                                .type(AssociationType.STUDENT)
                                .status(AssociationStatus.ACTIVE)
                                .contactEmail("contact@test.associa.bf")
                                .contactPhone("+22670009999")
                                .autoApproveMembers(true)
                                .financeApprovalWorkflow(true)
                                .foundedYear(2024)
                                .build();
                        log.info("Association de test créée : {}", a.getName());
                        return associationRepository.save(a);
                    });

            Role testPresidentRole = ensureRole(
                    testAssociation,
                    "president",
                    "Président",
                    RoleType.LEADERSHIP,
                    Map.of("admin_all", true),
                    1);

            Role testVicePresidentRole = ensureRole(
                    testAssociation,
                    "vice-president",
                    "Vice-Président",
                    RoleType.LEADERSHIP,
                    Map.of("events_all", true),
                    2);

            Role testSecretaryRole = ensureRole(
                    testAssociation,
                    "secretary",
                    "Secrétaire Général",
                    RoleType.LEADERSHIP,
                    Map.of(
                            "roles.manage", true,
                            "posts.manage", true,
                            "settings.view", true,
                            "settings.update", true),
                    3);

            Role testTreasurerRole = ensureRole(
                    testAssociation,
                    "treasurer",
                    "Trésorier",
                    RoleType.LEADERSHIP,
                    Map.of("finances_all", true),
                    4);

            Role testMemberRole = ensureRole(
                    testAssociation,
                    "member",
                    "Membre",
                    RoleType.MEMBER,
                    Map.of("members.view", true, "events.view", true),
                    99);

            Post testPresidentPost = ensurePost(testAssociation, "Président", 1, true);
            Post testVicePresidentPost = ensurePost(testAssociation, "Vice-Président", 2, true);
            Post testSecretaryPost = ensurePost(testAssociation, "Secrétaire Général", 3, true);
            Post testTreasurerPost = ensurePost(testAssociation, "Trésorier", 4, true);

            ensurePostRole(testPresidentPost, testPresidentRole);
            ensurePostRole(testVicePresidentPost, testVicePresidentRole);
            ensurePostRole(testSecretaryPost, testSecretaryRole);
            ensurePostRole(testTreasurerPost, testTreasurerRole);

            User testPresident = ensureUser(
                    testAssociation,
                    "president.test@associa.bf",
                    "Président",
                    "Test",
                    "+22670010001");
            User testVicePresident = ensureUser(
                    testAssociation,
                    "vice.test@associa.bf",
                    "Vice",
                    "Président",
                    "+22670010002");
            User testSecretary = ensureUser(
                    testAssociation,
                    "secretary.test@associa.bf",
                    "Secrétaire",
                    "Test",
                    "+22670010003");
            User testTreasurer = ensureUser(
                    testAssociation,
                    "treasurer.test@associa.bf",
                    "Trésorier",
                    "Test",
                    "+22670010004");

            ensureMandate(testPresident, testPresidentPost, true, "Mandat seed Président");
            ensureMandate(testVicePresident, testVicePresidentPost, true, "Mandat seed Vice-Président");
            ensureMandate(testSecretary, testSecretaryPost, true, "Mandat seed Secrétaire");
            ensureMandate(testTreasurer, testTreasurerPost, true, "Mandat seed Trésorier");

            // Membres additionnels de test
            User member1 = ensureUser(testAssociation, "member1@test.associa.bf", "Membre", "Un", "+22670010101");
            User member2 = ensureUser(testAssociation, "member2@test.associa.bf", "Membre", "Deux", "+22670010102");
            User member3 = ensureUser(testAssociation, "member3@test.associa.bf", "Membre", "Trois", "+22670010103");
            User member4 = ensureUser(testAssociation, "member4@test.associa.bf", "Membre", "Quatre", "+22670010104");
            User member5 = ensureUser(testAssociation, "member5@test.associa.bf", "Membre", "Cinq", "+22670010105");

            // Optionnel: donner un rôle membre de base
            roleService.assignRoleToUser(member1.getId(), testMemberRole.getId(), null);
            roleService.assignRoleToUser(member2.getId(), testMemberRole.getId(), null);
            roleService.assignRoleToUser(member3.getId(), testMemberRole.getId(), null);
            roleService.assignRoleToUser(member4.getId(), testMemberRole.getId(), null);
            roleService.assignRoleToUser(member5.getId(), testMemberRole.getId(), null);

            log.info("Data seeding completed!");
            log.info("Comptes disponibles :");
            log.info("   - Super Admin      : admin@associa.bf");
            log.info("   - Président C2I    : president.c2i@associa.bf (admin_all)");
            log.info("   - Test User        : test@associa.bf");
            log.info("   - Bureau test      : president.test@associa.bf / vice.test@associa.bf / secretary.test@associa.bf / treasurer.test@associa.bf");
            log.info("   - Membres test     : member1@test.associa.bf ... member5@test.associa.bf");
            log.info("Mot de passe seed dev configurable via SEED_DEFAULT_PASSWORD");
        };
    }

    private Role ensureRole(
            Association association,
            String slug,
            String name,
            RoleType type,
            Map<String, Boolean> permissions,
            int displayOrder) {
        Role role = roleRepository.findBySlugAndAssociationId(slug, association.getId())
                .orElseGet(() -> Role.builder()
                        .association(association)
                        .name(name)
                        .slug(slug)
                        .description(name + " de l'association")
                        .type(type)
                        .permissions(permissions)
                        .displayOrder(displayOrder)
                        .isTemplate(false)
                        .build());

        boolean changed = false;
        if (!Objects.equals(role.getName(), name)) {
            role.setName(name);
            changed = true;
        }
        if (!Objects.equals(role.getDescription(), name + " de l'association")) {
            role.setDescription(name + " de l'association");
            changed = true;
        }
        if (role.getType() != type) {
            role.setType(type);
            changed = true;
        }
        if (!Objects.equals(role.getPermissions(), permissions)) {
            role.setPermissions(permissions);
            changed = true;
        }
        if (role.getDisplayOrder() == null || role.getDisplayOrder() != displayOrder) {
            role.setDisplayOrder(displayOrder);
            changed = true;
        }
        if (role.getAssociation() == null || !Objects.equals(role.getAssociation().getId(), association.getId())) {
            role.setAssociation(association);
            changed = true;
        }

        return role.getId() == null || changed ? roleRepository.save(role) : role;
    }

    private Post ensurePost(Association association, String name, int displayOrder, boolean isExecutive) {
        return postRepository.findByNameAndAssociationId(name, association.getId())
                .orElseGet(() -> {
                    Post post = Post.builder()
                            .association(association)
                            .name(name)
                            .description("Poste " + name)
                            .isExecutive(isExecutive)
                            .maxOccupants(1)
                            .displayOrder(displayOrder)
                            .requiresElection(false)
                            .isActive(true)
                            .build();
                    return postRepository.save(post);
                });
    }

    private void ensurePostRole(Post post, Role role) {
        Post loaded = postRepository.findByIdWithRoles(post.getId()).orElse(post);

        if (!postRepository.existsRoleLink(post.getId(), role.getId())) {
            loaded.addSuggestedRole(role);
        }

        if (loaded.getDefaultRole() == null) {
            loaded.setDefaultRole(role);
        }

        postRepository.save(loaded);
    }

    private User ensureUser(
            Association association,
            String email,
            String firstName,
            String lastName,
            String whatsapp) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User user = User.builder()
                            .association(association)
                            .firstName(firstName)
                            .lastName(lastName)
                            .email(email)
                            .password(passwordEncoder.encode(seedDefaultPassword))
                            .whatsapp(whatsapp)
                            .membershipStatus(MembershipStatus.ACTIVE)
                            .membershipDate(LocalDate.now())
                            .isSuperAdmin(false)
                            .build();
                    return userRepository.save(user);
                });
    }

    private void ensureMandate(User user, Post post, boolean assignRole, String notes) {
        if (mandateService.hasActiveMandate(user.getId(), post.getId())) {
            return;
        }
        mandateService.assignPost(
                user.getId(),
                post.getId(),
                LocalDate.now().minusMonths(2),
                null,
                null,
                assignRole,
                null,
                notes);
    }
}
