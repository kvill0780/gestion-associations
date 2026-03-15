package bf.kvill.associa.integration;

import bf.kvill.associa.core.security.permission.PermissionService;
import bf.kvill.associa.members.role.Role;
import bf.kvill.associa.members.role.RoleRepository;
import bf.kvill.associa.members.role.UserRole;
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
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PermissionSystemIntegrationTest {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private AssociationRepository associationRepository;

    @AfterEach
    void cleanCache() {
        permissionService.invalidateAllPermissionsCache();
    }

    @Test
    @DisplayName("Permissions - un rôle expiré ne doit pas donner d'accès")
    void getUserPermissions_WhenRoleIsExpired_ShouldIgnoreRole() {
        Association association = associationRepository.save(Association.builder()
                .name("Association Expiree")
                .slug("association-expiree")
                .type(AssociationType.STUDENT)
                .status(AssociationStatus.ACTIVE)
                .build());

        User user = userRepository.save(User.builder()
                .email("expired-role@test.com")
                .password("encoded")
                .firstName("Expired")
                .lastName("Role")
                .association(association)
                .membershipStatus(MembershipStatus.ACTIVE)
                .build());

        Role role = roleRepository.save(Role.builder()
                .association(association)
                .name("Role Expire")
                .slug("role-expire")
                .type(RoleType.LEADERSHIP)
                .permissions(Map.of("settings.view", true))
                .build());

        userRoleRepository.save(UserRole.builder()
                .userId(user.getId())
                .roleId(role.getId())
                .isActive(true)
                .termStart(LocalDate.now().minusMonths(2))
                .termEnd(LocalDate.now().minusDays(1))
                .build());

        permissionService.invalidateUserPermissionsCache(user.getId());
        Set<String> permissions = permissionService.getUserPermissions(user.getId());

        assertThat(permissions).doesNotContain("settings.view");
    }

    @Test
    @DisplayName("Permissions - un rôle futur ne doit pas donner d'accès avant le début du mandat")
    void getUserPermissions_WhenRoleStartsInFuture_ShouldIgnoreRoleForNow() {
        Association association = associationRepository.save(Association.builder()
                .name("Association Future")
                .slug("association-future")
                .type(AssociationType.STUDENT)
                .status(AssociationStatus.ACTIVE)
                .build());

        User user = userRepository.save(User.builder()
                .email("future-role@test.com")
                .password("encoded")
                .firstName("Future")
                .lastName("Role")
                .association(association)
                .membershipStatus(MembershipStatus.ACTIVE)
                .build());

        Role role = roleRepository.save(Role.builder()
                .association(association)
                .name("Role Future")
                .slug("role-future")
                .type(RoleType.LEADERSHIP)
                .permissions(Map.of("events.manage", true))
                .build());

        userRoleRepository.save(UserRole.builder()
                .userId(user.getId())
                .roleId(role.getId())
                .isActive(true)
                .termStart(LocalDate.now().plusDays(1))
                .termEnd(LocalDate.now().plusMonths(2))
                .build());

        permissionService.invalidateUserPermissionsCache(user.getId());
        Set<String> permissions = permissionService.getUserPermissions(user.getId());

        assertThat(permissions).doesNotContain("events.manage");
    }
}
