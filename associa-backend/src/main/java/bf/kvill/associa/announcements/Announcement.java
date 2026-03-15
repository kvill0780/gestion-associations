package bf.kvill.associa.announcements;

import bf.kvill.associa.members.user.User;
import bf.kvill.associa.system.association.Association;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "announcements", indexes = {
        @Index(name = "idx_announcements_association_id", columnList = "association_id"),
        @Index(name = "idx_announcements_created_by", columnList = "created_by_id"),
        @Index(name = "idx_announcements_priority", columnList = "priority"),
        @Index(name = "idx_announcements_type", columnList = "type"),
        @Index(name = "idx_announcements_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@SQLDelete(sql = "UPDATE announcements SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "association_id", nullable = false)
    private Association association;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @Column(nullable = false, length = 200)
    @ToString.Include
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnnouncementPriority priority = AnnouncementPriority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnnouncementType type = AnnouncementType.ANNOUNCEMENT;

    @Column(name = "poll_question", length = 255)
    private String pollQuestion;

    @Column(name = "allow_multiple_votes")
    private Boolean allowMultipleVotes = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "announcement", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AnnouncementPollOption> pollOptions = new ArrayList<>();

    @OneToMany(mappedBy = "announcement", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AnnouncementReaction> reactions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (priority == null) {
            priority = AnnouncementPriority.NORMAL;
        }
        if (type == null) {
            type = AnnouncementType.ANNOUNCEMENT;
        }
        if (allowMultipleVotes == null) {
            allowMultipleVotes = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
