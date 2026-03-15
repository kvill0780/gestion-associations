package bf.kvill.associa.finance.contribution;

import bf.kvill.associa.finance.transaction.Transaction;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.shared.enums.ContributionStatus;
import bf.kvill.associa.shared.enums.ContributionType;
import bf.kvill.associa.shared.enums.TransactionStatus;
import bf.kvill.associa.system.association.Association;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "contributions",
        indexes = {
                @Index(name = "idx_contributions_association_id", columnList = "association_id"),
                @Index(name = "idx_contributions_member_id", columnList = "member_id"),
                @Index(name = "idx_contributions_year", columnList = "year"),
                @Index(name = "idx_contributions_month", columnList = "month"),
                @Index(name = "idx_contributions_due_date", columnList = "due_date"),
                @Index(name = "idx_contributions_member_period", columnList = "association_id, member_id, year, month")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_contributions_period", columnNames = {"association_id", "member_id", "year", "month"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Contribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "association_id", nullable = false)
    private Association association;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @Column(nullable = false)
    private Integer year;

    @Column
    private Integer month;

    @Column(name = "period_label", nullable = false, length = 50)
    private String periodLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContributionType type = ContributionType.MONTHLY;

    @Column(name = "expected_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal expectedAmount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "first_payment_date")
    private LocalDate firstPaymentDate;

    @Column(name = "last_payment_date")
    private LocalDate lastPaymentDate;

    @Column(name = "waived", nullable = false)
    private Boolean waived = false;

    @Column(name = "waived_reason", length = 500)
    private String waivedReason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "contribution", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Transaction> payments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (waived == null) {
            waived = false;
        }
        generatePeriodLabel();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        generatePeriodLabel();
    }

    @Transient
    public BigDecimal getPaidAmount() {
        if (payments == null || payments.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return payments.stream()
                .filter(payment -> payment.getStatus() == TransactionStatus.APPROVED)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transient
    public BigDecimal getRemainingAmount() {
        if (expectedAmount == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal remaining = expectedAmount.subtract(getPaidAmount());
        return remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining;
    }

    @Transient
    public ContributionStatus getStatus() {
        if (Boolean.TRUE.equals(waived)) {
            return ContributionStatus.WAIVED;
        }
        BigDecimal paid = getPaidAmount();
        if (expectedAmount != null && paid.compareTo(expectedAmount) >= 0) {
            return ContributionStatus.PAID;
        }
        boolean isLate = dueDate != null && LocalDate.now().isAfter(dueDate);
        if (paid.compareTo(BigDecimal.ZERO) > 0) {
            return isLate ? ContributionStatus.LATE_PARTIAL : ContributionStatus.PARTIAL;
        }
        return isLate ? ContributionStatus.LATE : ContributionStatus.PENDING;
    }

    @Transient
    public boolean isPaid() {
        return getStatus() == ContributionStatus.PAID;
    }

    @Transient
    public boolean isLate() {
        ContributionStatus status = getStatus();
        return status == ContributionStatus.LATE || status == ContributionStatus.LATE_PARTIAL;
    }

    public void addPayment(Transaction transaction) {
        if (transaction == null) {
            return;
        }
        if (payments == null) {
            payments = new ArrayList<>();
        }
        payments.add(transaction);
        transaction.setContribution(this);
        LocalDate today = LocalDate.now();
        if (firstPaymentDate == null) {
            firstPaymentDate = today;
        }
        lastPaymentDate = today;
    }

    private void generatePeriodLabel() {
        if (year == null) {
            return;
        }
        if (month != null) {
            periodLabel = monthName(month) + " " + year;
        } else {
            periodLabel = "Annee " + year;
        }
    }

    private String monthName(int monthValue) {
        String[] months = {
                "Janvier", "Fevrier", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Aout", "Septembre", "Octobre", "Novembre", "Decembre"
        };
        if (monthValue < 1 || monthValue > 12) {
            return "Mois " + monthValue;
        }
        return months[monthValue - 1];
    }
}
