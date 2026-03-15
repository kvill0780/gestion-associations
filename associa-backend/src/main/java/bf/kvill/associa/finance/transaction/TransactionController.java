package bf.kvill.associa.finance.transaction;

import bf.kvill.associa.finance.transaction.dto.ApproveTransactionRequest;
import bf.kvill.associa.finance.transaction.dto.CreateTransactionRequest;
import bf.kvill.associa.finance.transaction.dto.RejectTransactionRequest;
import bf.kvill.associa.finance.transaction.dto.TransactionResponse;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.security.userdetails.CustomUserPrincipal;
import bf.kvill.associa.shared.enums.TransactionStatus;
import bf.kvill.associa.shared.exception.ResourceNotFoundException;
import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.system.association.AssociationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Gestion des transactions financières")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;
    private final AssociationRepository associationRepository;
    private final UserRepository userRepository;

    @Operation(summary = "Lister les transactions")
    @GetMapping
    @PreAuthorize("hasPermission(null, 'finances.view')")
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) TransactionStatus status) {
        Long associationId = principal.getAssociationId();
        if (associationId == null) {
            throw new AccessDeniedException("Association introuvable pour ce compte");
        }

        List<Transaction> transactions = status == null
                ? transactionService.findByAssociation(associationId)
                : transactionService.findByStatus(associationId, status);

        return ResponseEntity.ok(
                transactions.stream()
                        .map(this::toResponse)
                        .toList());
    }

    @Operation(summary = "Créer une transaction")
    @PostMapping
    @PreAuthorize("hasPermission(null, 'finances.create')")
    public ResponseEntity<TransactionResponse> createTransaction(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CreateTransactionRequest request) {
        Long associationId = principal.getAssociationId();
        if (associationId == null) {
            throw new AccessDeniedException("Association introuvable pour ce compte");
        }

        Association association = associationRepository.findById(associationId)
                .orElseThrow(() -> new ResourceNotFoundException("Association", associationId));

        Transaction transaction = new Transaction();
        transaction.setAssociation(association);
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());
        transaction.setTitle(request.getTitle());
        transaction.setDescription(request.getDescription());
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setAcademicYear(request.getAcademicYear());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setNotes(request.getNotes());
        transaction.setRecordedById(principal.getId());

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));
            Long userAssociationId = user.getAssociation() != null ? user.getAssociation().getId() : null;
            if (userAssociationId == null || !userAssociationId.equals(associationId)) {
                throw new AccessDeniedException("Utilisateur hors de votre association");
            }
            transaction.setUser(user);
        }

        Transaction created = transactionService.createTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @Operation(summary = "Approuver une transaction")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasPermission(null, 'finances.approve')")
    public ResponseEntity<TransactionResponse> approveTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody(required = false) ApproveTransactionRequest request) {
        assertTransactionInScope(id, principal.getAssociationId());

        String notes = request != null ? request.getNotes() : null;
        Transaction approved = transactionService.approveTransaction(id, principal.getId(), notes);
        return ResponseEntity.ok(toResponse(approved));
    }

    @Operation(summary = "Rejeter une transaction")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasPermission(null, 'finances.approve')")
    public ResponseEntity<TransactionResponse> rejectTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody RejectTransactionRequest request) {
        assertTransactionInScope(id, principal.getAssociationId());

        Transaction rejected = transactionService.rejectTransaction(id, principal.getId(), request.getReason());
        return ResponseEntity.ok(toResponse(rejected));
    }

    private void assertTransactionInScope(Long transactionId, Long associationId) {
        if (associationId == null) {
            throw new AccessDeniedException("Association introuvable pour ce compte");
        }

        transactionService.findByIdAndAssociationId(transactionId, associationId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", transactionId));
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .associationId(transaction.getAssociation() != null ? transaction.getAssociation().getId() : null)
                .userId(transaction.getUser() != null ? transaction.getUser().getId() : null)
                .contributionId(transaction.getContribution() != null ? transaction.getContribution().getId() : null)
                .recordedById(transaction.getRecordedById())
                .validatedById(transaction.getValidatedById())
                .type(transaction.getType())
                .category(transaction.getCategory())
                .title(transaction.getTitle())
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .transactionDate(transaction.getTransactionDate())
                .academicYear(transaction.getAcademicYear())
                .paymentMethod(transaction.getPaymentMethod())
                .status(transaction.getStatus())
                .notes(transaction.getNotes())
                .validatedAt(transaction.getValidatedAt())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
