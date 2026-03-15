package bf.kvill.associa.security.auth;

import bf.kvill.associa.core.security.permission.PermissionService;
import bf.kvill.associa.members.mandate.MandateService;
import bf.kvill.associa.members.mandate.mapper.MandateMapper;
import bf.kvill.associa.members.role.Role;
import bf.kvill.associa.members.role.UserRole;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserService;
import bf.kvill.associa.security.auth.dto.UserInfoDto;
import bf.kvill.associa.security.userdetails.CustomUserPrincipal;
import bf.kvill.associa.shared.dto.ApiResponse;
import bf.kvill.associa.shared.enums.MembershipStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController - Legacy Tests")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @Mock
    private PermissionService permissionService;

    @Mock
    private MandateService mandateService;

    @Mock
    private MandateMapper mandateMapper;

    @InjectMocks
    private AuthController authController;

    private User user;
    private CustomUserPrincipal principal;

    @BeforeEach
    void setUp() {
        Role role = Role.builder()
                .name("Member")
                .slug("member")
                .build();
        UserRole userRole = UserRole.builder()
                .userId(1L)
                .roleId(1L)
                .role(role)
                .isActive(true)
                .build();

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .membershipStatus(MembershipStatus.ACTIVE)
                .build();
        user.setUserRoles(Set.of(userRole));

        principal = CustomUserPrincipal.builder()
                .id(1L)
                .email("test@example.com")
                .build();
    }

    @Test
    @DisplayName("✅ Should return full user info for /me endpoint")
    void getCurrentUser_ShouldReturnFullInfo() {
        // Given
        when(userService.findByIdWithUserRoles(1L)).thenReturn(user);
        when(permissionService.getUserPermissions(1L)).thenReturn(Set.of("members.view"));
        when(mandateService.findActiveUserMandates(1L)).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<ApiResponse<UserInfoDto>> response = authController.getCurrentUser(principal);

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getEmail()).isEqualTo("test@example.com");
        assertThat(response.getBody().getData().getRoles()).contains("Member");
        assertThat(response.getBody().getData().getPermissions()).contains("members.view");
    }
}
