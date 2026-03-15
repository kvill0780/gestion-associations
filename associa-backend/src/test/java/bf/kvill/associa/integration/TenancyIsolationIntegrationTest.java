package bf.kvill.associa.integration;

import bf.kvill.associa.events.EventService;
import bf.kvill.associa.events.dto.EventRequest;
import bf.kvill.associa.events.enums.EventStatus;
import bf.kvill.associa.events.enums.EventType;
import bf.kvill.associa.finance.transaction.Transaction;
import bf.kvill.associa.finance.transaction.TransactionService;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.shared.enums.AssociationStatus;
import bf.kvill.associa.shared.enums.AssociationType;
import bf.kvill.associa.shared.enums.MembershipStatus;
import bf.kvill.associa.shared.enums.TransactionCategory;
import bf.kvill.associa.shared.enums.TransactionType;
import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.system.association.AssociationRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
class TenancyIsolationIntegrationTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssociationRepository associationRepository;

    @Test
    @DisplayName("Events - création refusée si l'acteur n'appartient pas à l'association ciblée")
    void createEvent_WhenActorAssociationDiffers_ShouldDeny() {
        Association associationA = createAssociation("Association A", "association-a");
        Association associationB = createAssociation("Association B", "association-b");

        User outsider = createUser(associationB, "outsider@test.com");

        EventRequest request = EventRequest.builder()
                .title("Réunion confidentielle")
                .description("Tentative hors périmètre")
                .type(EventType.MEETING)
                .status(EventStatus.DRAFT)
                .startDate(LocalDateTime.now().plusDays(2))
                .endDate(LocalDateTime.now().plusDays(2).plusHours(2))
                .location("Salle A")
                .isOnline(false)
                .build();

        assertThatThrownBy(() -> eventService.createEvent(request, associationA.getId(), outsider.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("hors de votre association");
    }

    @Test
    @DisplayName("Transactions - la lecture par ID est strictement bornée à l'association")
    void findTransactionByIdAndAssociation_ShouldRespectAssociationScope() {
        Association associationA = createAssociation("Association TX A", "association-tx-a");
        Association associationB = createAssociation("Association TX B", "association-tx-b");
        User recorder = createUser(associationA, "recorder@test.com");

        Transaction tx = new Transaction();
        tx.setAssociation(associationA);
        tx.setRecordedById(recorder.getId());
        tx.setType(TransactionType.INCOME);
        tx.setCategory(TransactionCategory.MEMBERSHIP_FEE);
        tx.setTitle("Cotisation test");
        tx.setAmount(BigDecimal.valueOf(15000));
        tx.setTransactionDate(LocalDate.now());

        Transaction saved = transactionService.createTransaction(tx);

        assertThat(transactionService.findByIdAndAssociationId(saved.getId(), associationA.getId())).isPresent();
        assertThat(transactionService.findByIdAndAssociationId(saved.getId(), associationB.getId())).isEmpty();
    }

    private Association createAssociation(String name, String slug) {
        return associationRepository.save(Association.builder()
                .name(name)
                .slug(slug)
                .type(AssociationType.STUDENT)
                .status(AssociationStatus.ACTIVE)
                .build());
    }

    private User createUser(Association association, String email) {
        return userRepository.save(User.builder()
                .email(email)
                .password("encoded")
                .firstName("User")
                .lastName("Test")
                .association(association)
                .membershipStatus(MembershipStatus.ACTIVE)
                .isSuperAdmin(false)
                .build());
    }
}
