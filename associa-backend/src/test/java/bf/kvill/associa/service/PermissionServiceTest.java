package bf.kvill.associa.service;

import bf.kvill.associa.core.config.PermissionsConfig;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.members.role.Role;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.role.UserRole;
import bf.kvill.associa.members.role.RoleRepository;
import bf.kvill.associa.members.role.UserRoleRepository;
import bf.kvill.associa.core.security.permission.PermissionService;
import bf.kvill.associa.shared.enums.AssociationType;
import bf.kvill.associa.shared.enums.RoleType;
import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.system.association.AssociationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PermissionServiceTest {

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

    @Autowired
    private PermissionsConfig permissionsConfig;

    private User testUser;
    private Role treasurerRole;
    private Association testAssociation;

    @BeforeEach
    void setUp() {
        // Créer une association (obligatoire pour User et Role)
        testAssociation = Association.builder()
                .name("Test Association")
                .slug("test-association")
                .type(AssociationType.STUDENT)
                .build();
        testAssociation = associationRepository.save(testAssociation);

        // Créer un utilisateur
        testUser = User.builder()
                .email("test@example.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .association(testAssociation)
                .membershipStatus(bf.kvill.associa.shared.enums.MembershipStatus.ACTIVE)
                .isSuperAdmin(false)
                .build();
        testUser = userRepository.save(testUser);

        // Créer un rôle Trésorier
        treasurerRole = Role.builder()
                .association(testAssociation)
                .name("Trésorier")
                .slug("treasurer")
                .type(RoleType.LEADERSHIP)
                .permissions(Map.of(
                        "finances_all", true,
                        "members.view", true))
                .build();
        treasurerRole = roleRepository.save(treasurerRole);

        // Assigner le rôle à l'utilisateur
        UserRole userRole = UserRole.builder()
                .userId(testUser.getId())
                .roleId(treasurerRole.getId())
                .isActive(true)
                .build();
        userRoleRepository.save(userRole);
        userRoleRepository.flush();
    }

    @Test
    void testHasPermission_WithValidPermission() {
        assertTrue(permissionService.hasPermission(testUser, "finances.view"));
        assertTrue(permissionService.hasPermission(testUser, "finances.approve"));
        assertTrue(permissionService.hasPermission(testUser, "members.view"));
    }

    @Test
    void testHasPermission_WithInvalidPermission() {
        assertFalse(permissionService.hasPermission(testUser, "events.create"));
        assertFalse(permissionService.hasPermission(testUser, "documents.upload"));
    }

    @Test
    void testHasAnyPermission() {
        assertTrue(permissionService.hasAnyPermission(testUser,
                "events.create", "finances.view"));

        assertFalse(permissionService.hasAnyPermission(testUser,
                "events.create", "documents.upload"));
    }

    @Test
    void testHasAllPermissions() {
        assertTrue(permissionService.hasAllPermissions(testUser,
                "finances.view", "finances.approve"));

        assertFalse(permissionService.hasAllPermissions(testUser,
                "finances.view", "events.create"));
    }

    @Test
    void testGetUserPermissions_ResolveMacros() {
        var permissions = permissionService.getUserPermissions(testUser.getId());

        // finances_all doit être résolu en 6 permissions
        assertTrue(permissions.contains("finances.view"));
        assertTrue(permissions.contains("finances.create"));
        assertTrue(permissions.contains("finances.approve"));
        assertTrue(permissions.contains("finances.export"));

        // members.view doit être présent
        assertTrue(permissions.contains("members.view"));
    }

    @Test
    void testGetUserPermissions_IgnoresRolesFromAnotherAssociation() {
        Association otherAssociation = Association.builder()
                .name("Other Association")
                .slug("other-association")
                .type(AssociationType.STUDENT)
                .build();
        otherAssociation = associationRepository.save(otherAssociation);

        Role foreignRole = Role.builder()
                .association(otherAssociation)
                .name("Foreign Role")
                .slug("foreign-role")
                .type(RoleType.LEADERSHIP)
                .permissions(Map.of("events.create", true))
                .build();
        foreignRole = roleRepository.save(foreignRole);

        userRoleRepository.save(UserRole.builder()
                .userId(testUser.getId())
                .roleId(foreignRole.getId())
                .isActive(true)
                .build());

        permissionService.invalidateUserPermissionsCache(testUser.getId());

        var permissions = permissionService.getUserPermissions(testUser.getId());
        assertFalse(permissions.contains("events.create"));
    }

    @Test
    void testGetUserPermissionsByCategory() {
        var byCategory = permissionService.getUserPermissionsByCategory(testUser.getId());

        assertTrue(byCategory.containsKey("finances"));
        assertTrue(byCategory.get("finances").contains("finances.view"));
        assertTrue(byCategory.get("finances").contains("finances.approve"));
    }

    @Test
    void testValidatePermissions_Valid() {
        assertDoesNotThrow(() -> permissionService.validatePermissions(Map.of(
                "finances.view", true,
                "members.create", true)));
    }

    @Test
    void testValidatePermissions_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> permissionService.validatePermissions(Map.of(
                "invalid.permission", true)));
    }

    @Test
    void testOptimizePermissions_RemovesRedundancy() {
        Map<String, Boolean> optimized = permissionService.optimizePermissions(Map.of(
                "finances_all", true,
                "finances.view", true,
                "finances.create", true));

        // Doit garder seulement finances_all
        assertTrue(optimized.containsKey("finances_all"));
        assertFalse(optimized.containsKey("finances.view"));
    }

    @Test
    void testOptimizePermissions_AdminAll() {
        Map<String, Boolean> optimized = permissionService.optimizePermissions(Map.of(
                "admin_all", true,
                "finances_all", true,
                "members.view", true));

        // Doit garder seulement admin_all
        assertEquals(1, optimized.size());
        assertTrue(optimized.containsKey("admin_all"));
    }

    @Test
    void testLoadTest_PermissionChecks() {
        // Warm-up cache
        permissionService.getUserPermissions(testUser.getId());

        // Mesure
        long start = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            permissionService.hasPermission(testUser, "finances.view");
        }

        long duration = System.currentTimeMillis() - start;

        // Le but ici est de détecter une régression majeure, pas de benchmark micro-optimal.
        assertTrue(duration < 6000, "Trop lent: " + duration + "ms");
    }

    @Test
    void testCacheInvalidation() {
        // Première vérification
        var perms1 = permissionService.getUserPermissions(testUser.getId());
        assertTrue(perms1.contains("finances.view"));

        // Invalider le cache
        permissionService.invalidateUserPermissionsCache(testUser.getId());

        // Deuxième vérification (rechargée depuis DB)
        var perms2 = permissionService.getUserPermissions(testUser.getId());
        assertTrue(perms2.contains("finances.view"));
    }
}
