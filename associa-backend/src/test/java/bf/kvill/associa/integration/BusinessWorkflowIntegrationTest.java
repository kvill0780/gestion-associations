package bf.kvill.associa.integration;

import bf.kvill.associa.core.security.permission.PermissionService;
import bf.kvill.associa.members.mandate.Mandate;
import bf.kvill.associa.members.mandate.MandateRepository;
import bf.kvill.associa.members.mandate.MandateService;
import bf.kvill.associa.members.mandate.dto.RevokeMandateRequest;
import bf.kvill.associa.members.post.Post;
import bf.kvill.associa.members.post.PostRepository;
import bf.kvill.associa.members.role.Role;
import bf.kvill.associa.members.role.RoleRepository;
import bf.kvill.associa.members.role.UserRoleRepository;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.shared.enums.AssociationStatus;
import bf.kvill.associa.shared.enums.AssociationType;
import bf.kvill.associa.shared.enums.MembershipStatus;
import bf.kvill.associa.shared.enums.RoleType;
import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.system.association.AssociationRepository;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BusinessWorkflowIntegrationTest {

    @Autowired
    private MandateService mandateService;

    @Autowired
    private MandateRepository mandateRepository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private AssociationRepository associationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Test
    @DisplayName("Workflow - assignation mandat avec rôle puis révocation met à jour les permissions")
    void mandateRolePermissionLifecycle_ShouldGrantThenRevokePermissions() {
        Association association = createAssociation("workflow-main");
        User actor = createUser(association, "actor");
        User member = createUser(association, "member");

        Role secretaryRole = roleRepository.save(Role.builder()
                .association(association)
                .name("Secretaire")
                .slug("secretary-workflow")
                .type(RoleType.LEADERSHIP)
                .permissions(Map.of("events_all", true))
                .displayOrder(1)
                .build());

        Post secretaryPost = postRepository.save(Post.builder()
                .association(association)
                .name("Secretaire General")
                .isExecutive(true)
                .maxOccupants(1)
                .roles(new HashSet<>(Set.of(secretaryRole)))
                .build());

        Set<String> beforePermissions = permissionService.getUserPermissions(member.getId());
        assertThat(beforePermissions).doesNotContain("events.manage");

        Mandate mandate = mandateService.assignPost(
                member.getId(),
                secretaryPost.getId(),
                LocalDate.now(),
                LocalDate.now().plusMonths(6),
                actor.getId(),
                true,
                null,
                "Nomination du secretaire");

        assertThat(mandate.getAssignedRoleId()).isEqualTo(secretaryRole.getId());
        assertThat(userRoleRepository.findByUserIdAndRoleIdAndIsActiveTrue(member.getId(), secretaryRole.getId()))
                .isPresent();

        Set<String> afterAssignPermissions = permissionService.getUserPermissions(member.getId());
        assertThat(afterAssignPermissions)
                .contains("events.view", "events.create", "events.update", "events.delete", "events.manage");

        RevokeMandateRequest revokeRequest = new RevokeMandateRequest();
        revokeRequest.setReason("Fin de mission");
        mandateService.revokeMandate(mandate.getId(), revokeRequest, actor.getId());

        assertThat(userRoleRepository.findByUserIdAndRoleIdAndIsActiveTrue(member.getId(), secretaryRole.getId()))
                .isEmpty();
        assertThat(mandateRepository.findById(mandate.getId()))
                .isPresent()
                .get()
                .extracting(Mandate::getActive)
                .isEqualTo(false);

        Set<String> afterRevokePermissions = permissionService.getUserPermissions(member.getId());
        assertThat(afterRevokePermissions).doesNotContain("events.manage");
    }

    @Test
    @DisplayName("Workflow - assignation refusée si l'acteur est hors association cible")
    void assignPost_WhenActorIsFromAnotherAssociation_ShouldBeDenied() {
        Association associationA = createAssociation("workflow-a");
        Association associationB = createAssociation("workflow-b");

        User outsiderActor = createUser(associationA, "outsider");
        User targetMember = createUser(associationB, "target");
        Post targetPost = postRepository.save(Post.builder()
                .association(associationB)
                .name("Tresorier")
                .isExecutive(true)
                .maxOccupants(1)
                .build());

        assertThatThrownBy(() -> mandateService.assignPost(
                targetMember.getId(),
                targetPost.getId(),
                LocalDate.now(),
                LocalDate.now().plusMonths(3),
                outsiderActor.getId(),
                false,
                null,
                "Tentative cross-tenant"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("hors de votre association");
    }

    private Association createAssociation(String prefix) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return associationRepository.save(Association.builder()
                .name("Association " + prefix + " " + suffix)
                .slug(prefix + "-" + suffix)
                .type(AssociationType.STUDENT)
                .status(AssociationStatus.ACTIVE)
                .build());
    }

    private User createUser(Association association, String prefix) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return userRepository.save(User.builder()
                .association(association)
                .firstName("User")
                .lastName(prefix)
                .email(prefix + "." + suffix + "@associa.test")
                .password("encoded")
                .membershipStatus(MembershipStatus.ACTIVE)
                .isSuperAdmin(false)
                .build());
    }
}
