package bf.kvill.associa.integration;

import bf.kvill.associa.events.Event;
import bf.kvill.associa.events.EventRepository;
import bf.kvill.associa.events.enums.EventStatus;
import bf.kvill.associa.events.enums.EventType;
import bf.kvill.associa.members.mandate.Mandate;
import bf.kvill.associa.members.mandate.MandateRepository;
import bf.kvill.associa.members.post.Post;
import bf.kvill.associa.members.post.PostRepository;
import bf.kvill.associa.members.role.Role;
import bf.kvill.associa.members.role.RoleRepository;
import bf.kvill.associa.members.role.UserRole;
import bf.kvill.associa.members.role.UserRoleRepository;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.security.userdetails.CustomUserPrincipal;
import bf.kvill.associa.shared.enums.AssociationStatus;
import bf.kvill.associa.shared.enums.AssociationType;
import bf.kvill.associa.shared.enums.MembershipStatus;
import bf.kvill.associa.shared.enums.RoleType;
import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.system.association.AssociationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiContractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AssociationRepository associationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MandateRepository mandateRepository;

    @Autowired
    private EventRepository eventRepository;

    private Association association;
    private User president;
    private User member;
    private Role presidentRole;
    private Post post;
    private Mandate mandate;
    private Event event;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        association = associationRepository.save(Association.builder()
                .name("Association API " + suffix)
                .slug("api-contract-" + suffix)
                .type(AssociationType.STUDENT)
                .status(AssociationStatus.ACTIVE)
                .build());

        presidentRole = roleRepository.save(Role.builder()
                .association(association)
                .name("President")
                .slug("president-" + suffix)
                .type(RoleType.LEADERSHIP)
                .permissions(Map.of(
                        "members.view", true,
                        "members.update", true,
                        "events.view", true,
                        "events.manage", true,
                        "posts.manage", true,
                        "roles.manage", true))
                .displayOrder(1)
                .build());

        Role memberRole = roleRepository.save(Role.builder()
                .association(association)
                .name("Member")
                .slug("member-" + suffix)
                .type(RoleType.MEMBER)
                .permissions(Map.of(
                        "members.view", true,
                        "events.view", true))
                .displayOrder(99)
                .build());

        president = userRepository.save(User.builder()
                .association(association)
                .email("president." + suffix + "@associa.test")
                .password(passwordEncoder.encode("password"))
                .firstName("President")
                .lastName("Test")
                .membershipStatus(MembershipStatus.ACTIVE)
                .membershipDate(LocalDate.now())
                .isSuperAdmin(false)
                .build());

        member = userRepository.save(User.builder()
                .association(association)
                .email("member." + suffix + "@associa.test")
                .password(passwordEncoder.encode("password"))
                .firstName("Member")
                .lastName("Test")
                .membershipStatus(MembershipStatus.ACTIVE)
                .membershipDate(LocalDate.now())
                .isSuperAdmin(false)
                .build());

        userRoleRepository.save(UserRole.builder()
                .userId(president.getId())
                .roleId(presidentRole.getId())
                .isActive(true)
                .build());

        userRoleRepository.save(UserRole.builder()
                .userId(member.getId())
                .roleId(memberRole.getId())
                .isActive(true)
                .build());

        post = postRepository.save(Post.builder()
                .association(association)
                .name("President")
                .isExecutive(true)
                .maxOccupants(1)
                .displayOrder(1)
                .isActive(true)
                .build());

        mandate = mandateRepository.save(Mandate.builder()
                .association(association)
                .user(president)
                .post(post)
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusMonths(6))
                .active(true)
                .notes("Mandate API contract")
                .build());

        event = eventRepository.save(Event.builder()
                .association(association)
                .title("Réunion hebdo " + suffix)
                .description("Suivi des actions du bureau")
                .type(EventType.MEETING)
                .status(EventStatus.PUBLISHED)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Salle A")
                .isOnline(false)
                .maxParticipants(50)
                .createdBy(president)
                .build());
    }

    @Test
    @DisplayName("API contract - /api/auth/me returns user profile with permissions and mandates")
    void authMe_ShouldReturnProfileWithoutServerError() throws Exception {
        mockMvc.perform(get("/api/auth/me").with(authAs(president, "ROLE_PRESIDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(president.getId()))
                .andExpect(jsonPath("$.data.associationId").value(association.getId()))
                .andExpect(jsonPath("$.data.permissions").isArray())
                .andExpect(jsonPath("$.data.currentMandates[0].id").value(mandate.getId()));
    }

    @Test
    @DisplayName("API contract - core members endpoints return 200 for valid principal")
    void membersEndpoints_ShouldReturn200WithoutServerError() throws Exception {
        RequestPostProcessor auth = authAs(president, "ROLE_PRESIDENT");

        mockMvc.perform(get("/api/members/users").with(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/members/posts").with(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/members/mandates/association/{associationId}/current", association.getId())
                        .with(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("API contract - activate membership endpoint returns 200 and ACTIVE status")
    void activateMembership_ShouldReturn200WithoutServerError() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User pendingUser = userRepository.save(User.builder()
                .association(association)
                .email("pending." + suffix + "@associa.test")
                .password(passwordEncoder.encode("password"))
                .firstName("Pending")
                .lastName("User")
                .membershipStatus(MembershipStatus.PENDING)
                .isSuperAdmin(false)
                .build());

        RequestPostProcessor auth = authAs(president, "ROLE_PRESIDENT");

        mockMvc.perform(put("/api/members/users/{id}/activate", pendingUser.getId()).with(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(pendingUser.getId()))
                .andExpect(jsonPath("$.data.membershipStatus").value("ACTIVE"));
    }

    @Test
    @DisplayName("API contract - member cannot suspend another member")
    void memberCannotSuspendAnotherMember_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(put("/api/members/users/{id}/suspend", president.getId())
                        .with(authAs(member, "ROLE_MEMBER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("API contract - details endpoints used by frontend return 200")
    void detailsEndpoints_ShouldReturn200WithoutServerError() throws Exception {
        RequestPostProcessor auth = authAs(president, "ROLE_PRESIDENT");

        mockMvc.perform(get("/api/members/users/{id}", president.getId()).with(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(president.getId()));

        mockMvc.perform(get("/api/members/posts/{id}", post.getId()).with(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId()));

        mockMvc.perform(get("/api/members/mandates/{id}", mandate.getId()).with(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mandate.getId()));

        mockMvc.perform(get("/api/members/roles/{id}", presidentRole.getId()).with(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(presidentRole.getId()));
    }

    @Test
    @DisplayName("API contract - settings permissions endpoints return 200")
    void settingsPermissionsEndpoints_ShouldReturn200WithoutServerError() throws Exception {
        RequestPostProcessor auth = authAs(president, "ROLE_PRESIDENT");

        mockMvc.perform(get("/api/core/permissions/grouped").with(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/core/permissions/all").with(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("API contract - /api/auth/me/password changes password with current password")
    void changePassword_ShouldSucceedWithCurrentPassword() throws Exception {
        Map<String, String> payload = Map.of(
                "currentPassword", "password",
                "newPassword", "newPassword123!");

        mockMvc.perform(put("/api/auth/me/password")
                        .with(authAs(president, "ROLE_PRESIDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("API contract - event participation workflow endpoints return 200 with consistent payload")
    void eventParticipationWorkflow_ShouldReturn200WithoutServerError() throws Exception {
        RequestPostProcessor auth = authAs(president, "ROLE_PRESIDENT");

        mockMvc.perform(get("/api/events/{id}/participants", event.getId()).with(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(post("/api/events/{id}/participants/register", event.getId())
                        .with(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "userId", president.getId(),
                                "notes", "Inscrit via test contrat"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(event.getId()))
                .andExpect(jsonPath("$.userId").value(president.getId()))
                .andExpect(jsonPath("$.status").value("REGISTERED"));

        mockMvc.perform(get("/api/events/{id}/attendance-summary", event.getId()).with(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(event.getId()))
                .andExpect(jsonPath("$.registeredCount").value(1));

        mockMvc.perform(patch("/api/events/{id}/participants/{userId}/status", event.getId(), president.getId())
                        .with(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "status", "ATTENDED",
                                "notes", "Présence confirmée"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATTENDED"));

        mockMvc.perform(post("/api/events/{id}/check-in", event.getId()).with(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATTENDED"));

        mockMvc.perform(get("/api/events/{id}/attendance-summary", event.getId()).with(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendedCount").value(1))
                .andExpect(jsonPath("$.totalParticipants").value(1));
    }

    private RequestPostProcessor authAs(User user, String roleAuthority) {
        CustomUserPrincipal principal = CustomUserPrincipal.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(roleAuthority)))
                .active(true)
                .suspended(false)
                .associationId(user.getAssociation().getId())
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities());

        return SecurityMockMvcRequestPostProcessors.authentication(authentication);
    }
}
