package bf.kvill.associa.members.user.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ApproveMemberRequest {
    private LocalDate membershipDate;
    private String notes;
}