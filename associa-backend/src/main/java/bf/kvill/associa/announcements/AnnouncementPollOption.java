package bf.kvill.associa.announcements;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "announcement_poll_options", indexes = {
        @Index(name = "idx_announcement_poll_options_announcement_id", columnList = "announcement_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnouncementPollOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announcement_id", nullable = false)
    private Announcement announcement;

    @Column(name = "option_text", nullable = false, length = 255)
    private String optionText;
}
