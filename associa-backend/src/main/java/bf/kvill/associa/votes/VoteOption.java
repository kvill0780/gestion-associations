package bf.kvill.associa.votes;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vote_options", indexes = {
        @Index(name = "idx_vote_options_vote_id", columnList = "vote_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote;

    @Column(name = "option_text", nullable = false, length = 255)
    private String optionText;
}
