package bf.kvill.associa.security.auth;

import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.security.auth.dto.ForgotPasswordRequest;
import bf.kvill.associa.security.auth.dto.LoginRequest;
import bf.kvill.associa.security.auth.dto.LoginResponse;
import bf.kvill.associa.security.jwt.JwtService;
import bf.kvill.associa.shared.email.EmailService;
import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.system.association.AssociationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssociationRepository associationRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private EmailService emailService;

    private User testUser;
    private Association testAssociation;
    private final String RAW_PASSWORD = "Password123!";

    @BeforeEach
    void setUp() {
        testAssociation = Association.builder()
                .name("Test Association")
                .slug("test-asso")
                .type(bf.kvill.associa.shared.enums.AssociationType.STUDENT)
                .status(bf.kvill.associa.shared.enums.AssociationStatus.ACTIVE)
                .build();
        testAssociation = associationRepository.save(testAssociation);

        testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .association(testAssociation)
                .membershipStatus(bf.kvill.associa.shared.enums.MembershipStatus.ACTIVE)
                .build();
        testUser = userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        userRepository.deleteAll();
        associationRepository.deleteAll();
    }

    @Test
    @DisplayName("Login - Succès avec identifiants valides")
    void login_Success() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail(testUser.getEmail());
        request.setPassword(RAW_PASSWORD);

        MockHttpServletRequest httpRequest = new MockHttpServletRequest();

        // When
        LoginResponse response = authService.login(request, httpRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
        assertThat(response.getUser().getEmail()).isEqualTo(testUser.getEmail());

        // Validate token payload
        Long extractedUserId = jwtService.extractUserId(response.getAccessToken());
        assertThat(extractedUserId).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Login - Échec avec mauvais mot de passe")
    void login_Failure_WrongPassword() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail(testUser.getEmail());
        request.setPassword("WrongPassword!!!");

        MockHttpServletRequest httpRequest = new MockHttpServletRequest();

        // When / Then
        assertThatThrownBy(() -> authService.login(request, httpRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    @DisplayName("Reset Password Flow - Doit générer un token et envoyer un email")
    void forgotPassword_ShouldGenerateTokenAndSendEmail() {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail(testUser.getEmail());
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();

        // When
        authService.forgotPassword(request, httpRequest);

        // Then
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findLatestByUserId(testUser.getId());
        assertThat(tokenOpt).isPresent();

        PasswordResetToken token = tokenOpt.get();
        assertThat(token.getToken()).isNotBlank();
        assertThat(token.getExpiresAt()).isAfter(java.time.LocalDateTime.now());

        // Verify email was sent
        verify(emailService).sendPasswordResetEmail(eq(testUser.getEmail()), anyString());
    }
}
