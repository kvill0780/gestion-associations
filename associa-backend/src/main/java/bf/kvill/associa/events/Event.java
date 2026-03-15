package bf.kvill.associa.events;

import bf.kvill.associa.events.enums.EventStatus;
import bf.kvill.associa.events.enums.EventType;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.system.association.Association;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Entité représentant un Événement organisé par une association.
 * Permet de gérer les réunions, formations, soirées, etc.
 */
@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_events_association_id", columnList = "association_id"),
        @Index(name = "idx_events_status", columnList = "status"),
        @Index(name = "idx_events_start_date", columnList = "start_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@SQLDelete(sql = "UPDATE events SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "association_id", nullable = false)
    @ToString.Exclude
    private Association association;

    @Column(nullable = false, length = 150)
    @ToString.Include
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @ToString.Include
    private EventType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @ToString.Include
    private EventStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(length = 255)
    private String location;

    @Column(name = "is_online", nullable = false)
    private boolean isOnline;

    @Column(name = "meeting_link", length = 500)
    private String meetingLink;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    // Métadonnées d'audit
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    @ToString.Exclude
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = EventStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
