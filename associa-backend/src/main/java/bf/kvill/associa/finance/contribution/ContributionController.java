package bf.kvill.associa.finance.contribution;

import bf.kvill.associa.finance.contribution.dto.ContributionPaymentResponse;
import bf.kvill.associa.finance.contribution.dto.ContributionResponse;
import bf.kvill.associa.finance.contribution.dto.ContributionStatsResponse;
import bf.kvill.associa.finance.contribution.dto.CreateContributionRequest;
import bf.kvill.associa.finance.contribution.dto.GenerateContributionsRequest;
import bf.kvill.associa.finance.contribution.dto.RecordContributionPaymentRequest;
import bf.kvill.associa.finance.transaction.Transaction;
import bf.kvill.associa.security.userdetails.CustomUserPrincipal;
import bf.kvill.associa.shared.enums.ContributionStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance/contributions")
@RequiredArgsConstructor
@Tag(name = "Contributions", description = "Gestion des cotisations")
@SecurityRequirement(name = "bearerAuth")
public class ContributionController {

    private final ContributionService contributionService;

    @Operation(summary = "Lister les cotisations")
    @GetMapping
    @PreAuthorize("hasPermission(null, 'finances.view')")
    public ResponseEntity<List<ContributionResponse>> getContributions(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        if (month != null && year == null) {
            throw new IllegalArgumentException("L'annee est obligatoire si le mois est fourni");
        }
        List<Contribution> contributions = contributionService.findByPeriod(principal.getAssociationId(), year, month);
        return ResponseEntity.ok(contributions.stream().map(this::toResponse).toList());
    }

    @Operation(summary = "Afficher une cotisation")
    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'finances.view')")
    public ResponseEntity<ContributionResponse> getContribution(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        Contribution contribution = contributionService.findByIdAndAssociation(id, principal.getAssociationId());
        return ResponseEntity.ok(toResponse(contribution));
    }

    @Operation(summary = "Creer une cotisation manuelle")
    @PostMapping
    @PreAuthorize("hasPermission(null, 'finances.create')")
    public ResponseEntity<ContributionResponse> createContribution(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CreateContributionRequest request) {
        Contribution contribution = contributionService.createContribution(
                principal.getAssociationId(),
                principal.getId(),
                request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(contribution));
    }

    @Operation(summary = "Generer les cotisations du mois")
    @PostMapping("/generate")
    @PreAuthorize("hasPermission(null, 'finances.create')")
    public ResponseEntity<List<ContributionResponse>> generateContributions(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody GenerateContributionsRequest request) {
        List<Contribution> contributions = contributionService.generateContributions(
                principal.getAssociationId(),
                principal.getId(),
                request);
        return ResponseEntity.ok(contributions.stream().map(this::toResponse).toList());
    }

    @Operation(summary = "Enregistrer un paiement de cotisation")
    @PostMapping("/{id}/payments")
    @PreAuthorize("hasPermission(null, 'finances.create')")
    public ResponseEntity<ContributionResponse> recordPayment(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody RecordContributionPaymentRequest request) {
        Contribution contribution = contributionService.recordPayment(
                principal.getAssociationId(),
                id,
                principal.getId(),
                request);
        return ResponseEntity.ok(toResponse(contribution));
    }

    @Operation(summary = "Statistiques des cotisations")
    @GetMapping("/stats")
    @PreAuthorize("hasPermission(null, 'finances.view')")
    public ResponseEntity<ContributionStatsResponse> getStats(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam Integer year,
            @RequestParam(required = false) Integer month) {
        List<Contribution> contributions = contributionService.findByPeriod(principal.getAssociationId(), year, month);
        return ResponseEntity.ok(buildStats(contributions));
    }

    private ContributionResponse toResponse(Contribution contribution) {
        List<ContributionPaymentResponse> payments = contribution.getPayments() == null
                ? List.of()
                : contribution.getPayments().stream().map(this::toPaymentResponse).toList();

        return ContributionResponse.builder()
                .id(contribution.getId())
                .associationId(contribution.getAssociation() != null ? contribution.getAssociation().getId() : null)
                .memberId(contribution.getMember() != null ? contribution.getMember().getId() : null)
                .memberName(contribution.getMember() != null ? contribution.getMember().getFullName() : null)
                .year(contribution.getYear())
                .month(contribution.getMonth())
                .periodLabel(contribution.getPeriodLabel())
                .type(contribution.getType())
                .expectedAmount(contribution.getExpectedAmount())
                .paidAmount(contribution.getPaidAmount())
                .remainingAmount(contribution.getRemainingAmount())
                .status(contribution.getStatus())
                .dueDate(contribution.getDueDate())
                .firstPaymentDate(contribution.getFirstPaymentDate())
                .lastPaymentDate(contribution.getLastPaymentDate())
                .waived(contribution.getWaived())
                .waivedReason(contribution.getWaivedReason())
                .notes(contribution.getNotes())
                .payments(payments)
                .createdAt(contribution.getCreatedAt())
                .updatedAt(contribution.getUpdatedAt())
                .build();
    }

    private ContributionPaymentResponse toPaymentResponse(Transaction payment) {
        return ContributionPaymentResponse.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .transactionDate(payment.getTransactionDate())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .notes(payment.getNotes())
                .build();
    }

    private ContributionStatsResponse buildStats(List<Contribution> contributions) {
        long total = contributions.size();
        long paid = contributions.stream().filter(Contribution::isPaid).count();
        long late = contributions.stream().filter(Contribution::isLate).count();
        long partial = contributions.stream()
                .filter(c -> c.getStatus() == ContributionStatus.PARTIAL
                        || c.getStatus() == ContributionStatus.LATE_PARTIAL)
                .count();

        BigDecimal totalExpected = contributions.stream()
                .map(Contribution::getExpectedAmount)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCollected = contributions.stream()
                .map(Contribution::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double collectionRate = total == 0 ? 0d : (double) paid / (double) total * 100d;

        return ContributionStatsResponse.builder()
                .totalMembers(total)
                .upToDate(paid)
                .late(late)
                .partial(partial)
                .totalExpected(totalExpected)
                .totalCollected(totalCollected)
                .collectionRate(collectionRate)
                .build();
    }
}
