package bf.kvill.associa.members.role.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AssignRoleRequest {

    @NotNull(message = "L'utilisateur est obligatoire")
    private Long userId;

    @NotNull(message = "Le rôle est obligatoire")
    private Long roleId;

    private Long assignedById;

    private LocalDate termStart;

    private LocalDate termEnd;

    private String notes;
}
