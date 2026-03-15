package bf.kvill.associa.members.mandate;

import bf.kvill.associa.members.mandate.dto.AssignPostRequest;
import bf.kvill.associa.members.mandate.dto.RevokeMandateRequest;
import bf.kvill.associa.members.mandate.policy.MandateTransitionPolicy;
import bf.kvill.associa.members.mandate.policy.MandateValidationPolicy;
import bf.kvill.associa.members.post.Post;
import bf.kvill.associa.members.post.PostRepository;
import bf.kvill.associa.members.role.Role;
import bf.kvill.associa.members.role.RoleRepository;
import bf.kvill.associa.members.role.RoleService;
import bf.kvill.associa.members.role.UserRole;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.system.audit.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour MandateService
 * Focus sur l'architecture MIXTE avec maxOccupants
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MandateService - Architecture MIXTE Tests")
class MandateServiceTest {

        @Mock
        private MandateRepository mandateRepository;

        @Mock
        private PostRepository postRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private RoleRepository roleRepository;

        @Mock
        private RoleService roleService;

        @Mock
        private MandateValidationPolicy validationPolicy;

        @Mock
        private MandateTransitionPolicy transitionPolicy;

        @Mock
        private AuditService auditService;

        @InjectMocks
        private MandateService mandateService;

        private Association association;
        private User user1;
        private User user2;
        private Post presidentPost;
        private Post conseillerPost;

        @BeforeEach
        void setUp() {
                association = Association.builder()
                                .id(1L)
                                .name("Association Test")
                                .build();

                user1 = User.builder()
                                .id(1L)
                                .email("user1@test.com")
                                .firstName("Jean")
                                .lastName("Dupont")
                                .association(association)
                                .build();

                user2 = User.builder()
                                .id(2L)
                                .email("user2@test.com")
                                .firstName("Marie")
                                .lastName("Martin")
                                .association(association)
                                .build();

                // Poste unique : Président (maxOccupants = 1)
                presidentPost = Post.builder()
                                .id(1L)
                                .association(association)
                                .name("Président")
                                .maxOccupants(1)
                                .isExecutive(true)
                                .build();

                // Poste multiple : Conseiller (maxOccupants = 5)
                conseillerPost = Post.builder()
                                .id(2L)
                                .association(association)
                                .name("Conseiller")
                                .maxOccupants(5)
                                .isExecutive(false)
                                .build();
        }

        // ==================== Tests: Poste Unique (maxOccupants = 1)
        // ====================

        @Test
        @DisplayName("✅ Assign post unique - Poste vide - Devrait réussir")
        void assignPost_UniquePost_WhenEmpty_ShouldSucceed() {
                // Given
                when(postRepository.findById(presidentPost.getId())).thenReturn(Optional.of(presidentPost));
                when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
                when(transitionPolicy.closeUserMandate(user1.getId(), presidentPost.getId()))
                                .thenReturn(Optional.empty());
                when(mandateRepository.save(any(Mandate.class))).thenAnswer(inv -> {
                        Mandate m = inv.getArgument(0);
                        m.setId(1L);
                        return m;
                });

                // When
                Mandate result = mandateService.assignPost(
                                user1.getId(),
                                presidentPost.getId(),
                                LocalDate.now(),
                                null,
                                1L,
                                false,
                                null,
                                "Test");

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getUser()).isEqualTo(user1);
                assertThat(result.getPost()).isEqualTo(presidentPost);
                verify(validationPolicy).validate(any(AssignPostRequest.class));
                verify(mandateRepository).save(any(Mandate.class));
        }

        @Test
        @DisplayName("❌ Assign post unique - Poste plein - Devrait lever exception")
        void assignPost_UniquePost_WhenFull_ShouldThrowException() {
                // Given
                when(postRepository.findById(presidentPost.getId())).thenReturn(Optional.of(presidentPost));
                when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
                when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
                when(transitionPolicy.closeUserMandate(user2.getId(), presidentPost.getId()))
                                .thenReturn(Optional.empty());
                doThrow(new IllegalStateException("Le poste 'Président' est complet: 1/1 occupant(s)"))
                                .when(validationPolicy).validate(any(AssignPostRequest.class));

                // When / Then
                assertThatThrownBy(() -> mandateService.assignPost(
                                user2.getId(),
                                presidentPost.getId(),
                                LocalDate.now(),
                                null,
                                1L,
                                false,
                                null,
                                "Test"))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessageContaining("complet");

                verify(mandateRepository, never()).save(any(Mandate.class));
        }

        // ==================== Tests: Poste Multiple (maxOccupants > 1)
        // ====================

        @Test
        @DisplayName("✅ Assign post multiple - Pas plein - Devrait réussir")
        void assignPost_MultiplePost_WhenNotFull_ShouldSucceed() {
                // Given
                when(postRepository.findById(conseillerPost.getId())).thenReturn(Optional.of(conseillerPost));
                when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
                when(transitionPolicy.closeUserMandate(user1.getId(), conseillerPost.getId()))
                                .thenReturn(Optional.empty());
                when(mandateRepository.save(any(Mandate.class))).thenAnswer(inv -> {
                        Mandate m = inv.getArgument(0);
                        m.setId(2L);
                        return m;
                });

                // When
                Mandate result = mandateService.assignPost(
                                user1.getId(),
                                conseillerPost.getId(),
                                LocalDate.now(),
                                null,
                                1L,
                                false,
                                null,
                                "Test");

                // Then
                assertThat(result).isNotNull();
                verify(validationPolicy).validate(any(AssignPostRequest.class));
                verify(mandateRepository).save(any(Mandate.class));
        }

        @Test
        @DisplayName("❌ Assign post multiple - Plein (5/5) - Devrait lever exception")
        void assignPost_MultiplePost_WhenFull_ShouldThrowException() {
                // Given
                when(postRepository.findById(conseillerPost.getId())).thenReturn(Optional.of(conseillerPost));
                when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
                when(transitionPolicy.closeUserMandate(user1.getId(), conseillerPost.getId()))
                                .thenReturn(Optional.empty());
                doThrow(new IllegalStateException("Le poste 'Conseiller' est complet: 5/5 occupant(s)"))
                                .when(validationPolicy).validate(any(AssignPostRequest.class));

                // When / Then
                assertThatThrownBy(() -> mandateService.assignPost(
                                user1.getId(),
                                conseillerPost.getId(),
                                LocalDate.now(),
                                null,
                                1L,
                                false,
                                null,
                                "Test"))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessageContaining("complet");
        }

        // ==================== Tests: Renouvellement ====================

        @Test
        @DisplayName("✅ Renouvellement - Même user - Devrait fermer ancien mandat et créer nouveau")
        void assignPost_SameUserRenewal_ShouldCloseOldMandate() {
                // Given
                Mandate oldMandate = Mandate.builder()
                                .id(1L)
                                .user(user1)
                                .post(presidentPost)
                                .active(true)
                                .startDate(LocalDate.now().minusYears(1))
                                .build();

                when(postRepository.findById(presidentPost.getId())).thenReturn(Optional.of(presidentPost));
                when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
                when(transitionPolicy.closeUserMandate(user1.getId(), presidentPost.getId()))
                                .thenReturn(Optional.of(oldMandate));
                when(mandateRepository.save(any(Mandate.class))).thenAnswer(inv -> {
                        Mandate m = inv.getArgument(0);
                        m.setId(2L);
                        return m;
                });

                // When
                Mandate result = mandateService.assignPost(
                                user1.getId(),
                                presidentPost.getId(),
                                LocalDate.now(),
                                null,
                                1L,
                                false,
                                null,
                                "Renouvellement");

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(2L); // Nouveau mandat
                verify(transitionPolicy).closeUserMandate(user1.getId(), presidentPost.getId());
                verify(mandateRepository).save(any(Mandate.class));
        }

        // ==================== Tests: Révocation avec Rôle ====================

        @Test
        @DisplayName("✅ Revoke mandate - Avec rôle assigné - Devrait révoquer le rôle")
        void revokeMandate_WithAssignedRole_ShouldRevokeRole() {
                // Given
                Long roleId = 10L;
                Mandate mandate = Mandate.builder()
                                .id(1L)
                                .association(association)
                                .user(user1)
                                .post(presidentPost)
                                .active(true)
                                .assignRole(true)
                                .assignedRoleId(roleId)
                                .build();

                RevokeMandateRequest request = new RevokeMandateRequest();
                request.setReason("Démission");

                when(mandateRepository.findById(1L)).thenReturn(Optional.of(mandate));
                when(mandateRepository.save(any(Mandate.class))).thenReturn(mandate);
                doNothing().when(roleService).revokeRoleFromUser(user1.getId(), roleId, null);

                // When
                Mandate result = mandateService.revokeMandate(1L, request);

                // Then
                assertThat(result.isActive()).isFalse();
                verify(roleService).revokeRoleFromUser(user1.getId(), roleId, null);
                verify(auditService).log(eq("REVOKE_MANDATE"), anyString(), anyLong(), (Long) isNull(), anyMap());
        }

        @Test
        @DisplayName("✅ Revoke mandate - Sans rôle assigné - Ne devrait pas révoquer de rôle")
        void revokeMandate_WithoutAssignedRole_ShouldNotRevokeRole() {
                // Given
                Mandate mandate = Mandate.builder()
                                .id(1L)
                                .association(association)
                                .user(user1)
                                .post(presidentPost)
                                .active(true)
                                .assignRole(false)
                                .assignedRoleId(null)
                                .build();

                RevokeMandateRequest request = new RevokeMandateRequest();

                when(mandateRepository.findById(1L)).thenReturn(Optional.of(mandate));
                when(mandateRepository.save(any(Mandate.class))).thenReturn(mandate);

                // When
                Mandate result = mandateService.revokeMandate(1L, request);

                // Then
                assertThat(result.isActive()).isFalse();
                verify(roleService, never()).revokeRoleFromUser(anyLong(), anyLong());
        }

        // ==================== Tests: Attribution avec Rôle ====================

        @Test
        @DisplayName("✅ Assign post - Avec assignRole=true - Devrait attribuer et tracer le rôle")
        void assignPost_WithAssignRole_ShouldAssignAndTrackRole() {
                // Given
                Long roleId = 10L;
                Role role = Role.builder()
                                .id(roleId)
                                .association(association)
                                .name("Président Role")
                                .slug("president-role")
                                .build();
                when(postRepository.findById(presidentPost.getId())).thenReturn(Optional.of(presidentPost));
                when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
                when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
                when(transitionPolicy.closeUserMandate(user1.getId(), presidentPost.getId()))
                                .thenReturn(Optional.empty());

                Mandate savedMandate = Mandate.builder()
                                .id(1L)
                                .user(user1)
                                .post(presidentPost)
                                .assignRole(true)
                                .build();

                when(mandateRepository.save(any(Mandate.class))).thenReturn(savedMandate);
                when(roleService.assignRoleToUser(anyLong(), anyLong(), anyLong(), any(), any()))
                                .thenReturn(new UserRole());

                // When
                Mandate result = mandateService.assignPost(
                                user1.getId(),
                                presidentPost.getId(),
                                LocalDate.now(),
                                null,
                                1L,
                                true, // assignRole = true
                                roleId,
                                "Test");

                // Then
                verify(roleService).assignRoleToUser(
                                eq(user1.getId()),
                                eq(roleId),
                                eq(1L),
                                any(LocalDate.class),
                                isNull());
                verify(mandateRepository, atLeast(2)).save(any(Mandate.class)); // 1 fois initial + 1 fois pour
                                                                                // assignedRoleId
        }
}
