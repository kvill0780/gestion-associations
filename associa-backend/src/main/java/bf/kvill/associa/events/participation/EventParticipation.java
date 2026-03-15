package bf.kvill.associa.events.participation;

import bf.kvill.associa.events.Event;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.shared.enums.EventParticipationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_participations", indexes = {
        @Index(name = "idx_event_participations_event_id", columnList = "event_id"),
        @Index(name = "idx_event_participations_user_id", columnList = "user_id"),
        @Index(name = "idx_event_participations_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventParticipationStatus status = EventParticipationStatus.REGISTERED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
