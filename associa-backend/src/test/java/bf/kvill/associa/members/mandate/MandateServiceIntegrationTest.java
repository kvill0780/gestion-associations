package bf.kvill.associa.members.mandate;

import bf.kvill.associa.members.post.Post;
import bf.kvill.associa.members.post.PostRepository;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.shared.enums.AssociationStatus;
import bf.kvill.associa.shared.enums.AssociationType;
import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.system.association.AssociationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import bf.kvill.associa.shared.email.EmailService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MandateServiceIntegrationTest {

    @Autowired
    private MandateService mandateService;

    @Autowired
    private MandateRepository mandateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private AssociationRepository associationRepository;

    @MockBean
    private EmailService emailService;

    private User testUser;
    private Post testPost;
    private Association testAssociation;

    @BeforeEach
    void setUp() {
        testAssociation = Association.builder()
                .name("Integration Association")
                .slug("integ-asso")
                .type(AssociationType.STUDENT)
                .status(AssociationStatus.ACTIVE)
                .build();
        testAssociation = associationRepository.save(testAssociation);

        testUser = userRepository.save(User.builder()
                .email("mandate.user@test.com")
                .firstName("Alice")
                .lastName("Smith")
                .password("Secr3t123!")
                .association(testAssociation)
                .membershipStatus(bf.kvill.associa.shared.enums.MembershipStatus.ACTIVE)
                .build());

        testPost = postRepository.save(Post.builder()
                .name("Secrétaire")
                .association(testAssociation)
                .isExecutive(true)
                .maxOccupants(1)
                .build());
    }

    @AfterEach
    void tearDown() {
        mandateRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
        associationRepository.deleteAll();
    }

    @Test
    @DisplayName("assignPost - L'intégration complète doit créer et sauvegarder un Mandate")
    void assignPost_Integration_ShouldCreateMandate() {
        // When
        Mandate newMandate = mandateService.assignPost(
                testUser.getId(),
                testPost.getId(),
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                testUser.getId(),
                false,
                null,
                "Integration Test Mandate");

        // Then
        assertThat(newMandate).isNotNull();
        assertThat(newMandate.getId()).isNotNull();
        assertThat(newMandate.getActive()).isTrue();
        assertThat(newMandate.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(newMandate.getPost().getId()).isEqualTo(testPost.getId());

        // Verify state is persisted
        assertThat(mandateRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("assignPost - L'assignation d'un poste plein lève une exception InvalidDataAccessApiUsageException via validation")
    void assignPost_Integration_WhenPostIsFull_ShouldThrow() {
        // Given - assign the post once (since maxOccupants = 1)
        mandateService.assignPost(
                testUser.getId(),
                testPost.getId(),
                LocalDate.now(),
                null,
                testUser.getId(),
                false,
                null,
                "First assigment");

        User secondUser = userRepository.save(User.builder()
                .email("second.user@test.com")
                .firstName("Bob")
                .lastName("Jones")
                .password("Secr3t123!")
                .association(testAssociation)
                .membershipStatus(bf.kvill.associa.shared.enums.MembershipStatus.ACTIVE)
                .build());

        // When/Then - try assigning again to someone else
        assertThatThrownBy(() -> mandateService.assignPost(
                secondUser.getId(),
                testPost.getId(),
                LocalDate.now(),
                null,
                testUser.getId(),
                false,
                null,
                "Second assignment")).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("complet");
    }

    @Test
    @DisplayName("assignPost - Renouveler un mandat sur le même poste ferme l'ancien et garde un seul mandat courant")
    void assignPost_Integration_WhenRenewingSameUserAndPost_ShouldClosePreviousMandate() {
        Mandate firstMandate = mandateService.assignPost(
                testUser.getId(),
                testPost.getId(),
                LocalDate.now().minusMonths(2),
                LocalDate.now().plusMonths(2),
                testUser.getId(),
                false,
                null,
                "Premier mandat");

        Mandate renewedMandate = mandateService.assignPost(
                testUser.getId(),
                testPost.getId(),
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                testUser.getId(),
                false,
                null,
                "Renouvellement");

        Mandate firstReloaded = mandateRepository.findById(firstMandate.getId()).orElseThrow();
        Mandate secondReloaded = mandateRepository.findById(renewedMandate.getId()).orElseThrow();

        assertThat(secondReloaded.getId()).isNotEqualTo(firstReloaded.getId());
        assertThat(firstReloaded.getActive()).isFalse();
        assertThat(secondReloaded.getActive()).isTrue();

        assertThat(mandateRepository.countCurrentActiveByPostId(testPost.getId(), LocalDate.now())).isEqualTo(1);
        assertThat(mandateRepository.findByUserIdAndPostIdAndActiveTrue(testUser.getId(), testPost.getId()))
                .isPresent()
                .get()
                .extracting(Mandate::getId)
                .isEqualTo(secondReloaded.getId());
    }
}
