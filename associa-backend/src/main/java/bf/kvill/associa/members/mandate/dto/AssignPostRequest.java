package bf.kvill.associa.members.mandate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO pour l'endpoint assign-post
 * L'endpoint le plus important du système !
 */
@Data
public class AssignPostRequest {

    @NotNull(message = "L'utilisateur est obligatoire")
    private Long userId;

    @NotNull(message = "Le poste est obligatoire")
    private Long postId;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate startDate;

    private LocalDate endDate;

    /**
     * Si true, attribue automatiquement le rôle lié au poste
     */
    private Boolean assignRole = true;

    /**
     * Override du rôle suggéré (optionnel)
     */
    private Long roleOverrideId;

    private String notes;

    public AssignPostRequest() {
    }

    public AssignPostRequest(Long userId, Long postId, LocalDate startDate, LocalDate endDate, Boolean assignRole,
            Long roleOverrideId, String notes) {
        this.userId = userId;
        this.postId = postId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.assignRole = assignRole;
        this.roleOverrideId = roleOverrideId;
        this.notes = notes;
    }

    // ==================== Manual Getters/Setters (Lombok fallback)
    // ====================

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Boolean getAssignRole() {
        return assignRole;
    }

    public void setAssignRole(Boolean assignRole) {
        this.assignRole = assignRole;
    }

    public Long getRoleOverrideId() {
        return roleOverrideId;
    }

    public void setRoleOverrideId(Long roleOverrideId) {
        this.roleOverrideId = roleOverrideId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}