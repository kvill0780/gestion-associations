package bf.kvill.associa.votes.dto;

import bf.kvill.associa.votes.VoteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateVoteRequest {
    @NotBlank(message = "Titre requis")
    private String title;

    @NotBlank(message = "Description requise")
    private String description;

    @NotNull(message = "Quorum requis")
    private Integer quorum;

    @NotNull(message = "Majorité requise")
    private Integer majority;

    @NotNull(message = "Date de début requise")
    private LocalDateTime startDate;

    @NotNull(message = "Date de fin requise")
    private LocalDateTime endDate;

    @NotNull(message = "Type requis")
    private VoteType type;

    @Size(min = 2, message = "Au moins 2 options")
    private List<String> options;
}
