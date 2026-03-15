package bf.kvill.associa.members.mandate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MandateResponseDto {
    private Long id;
    private Long userId;
    private String userFullName;
    private Long postId;
    private String postName;
    private Long associationId;
    private String associationName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
    private String notes;
    private Long assignedById;
    private LocalDateTime createdAt;

    // ==================== Manual Getters/Setters (Lombok fallback)
    // ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getPostName() {
        return postName;
    }

    public void setPostName(String postName) {
        this.postName = postName;
    }

    public Long getAssociationId() {
        return associationId;
    }

    public void setAssociationId(Long associationId) {
        this.associationId = associationId;
    }

    public String getAssociationName() {
        return associationName;
    }

    public void setAssociationName(String associationName) {
        this.associationName = associationName;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getAssignedById() {
        return assignedById;
    }

    public void setAssignedById(Long assignedById) {
        this.assignedById = assignedById;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ==================== Manual Builder (Lombok fallback) ====================

    public static MandateResponseDtoBuilder builder() {
        return new MandateResponseDtoBuilder();
    }

    public static class MandateResponseDtoBuilder {
        private Long id;
        private Long userId;
        private String userFullName;
        private Long postId;
        private String postName;
        private Long associationId;
        private String associationName;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean active;
        private String notes;
        private Long assignedById;
        private LocalDateTime createdAt;

        MandateResponseDtoBuilder() {
        }

        public MandateResponseDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public MandateResponseDtoBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public MandateResponseDtoBuilder userFullName(String userFullName) {
            this.userFullName = userFullName;
            return this;
        }

        public MandateResponseDtoBuilder postId(Long postId) {
            this.postId = postId;
            return this;
        }

        public MandateResponseDtoBuilder postName(String postName) {
            this.postName = postName;
            return this;
        }

        public MandateResponseDtoBuilder associationId(Long associationId) {
            this.associationId = associationId;
            return this;
        }

        public MandateResponseDtoBuilder associationName(String associationName) {
            this.associationName = associationName;
            return this;
        }

        public MandateResponseDtoBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public MandateResponseDtoBuilder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public MandateResponseDtoBuilder active(Boolean active) {
            this.active = active;
            return this;
        }

        public MandateResponseDtoBuilder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public MandateResponseDtoBuilder assignedById(Long assignedById) {
            this.assignedById = assignedById;
            return this;
        }

        public MandateResponseDtoBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public MandateResponseDto build() {
            return new MandateResponseDto(id, userId, userFullName, postId, postName, associationId, associationName,
                    startDate, endDate, active, notes, assignedById, createdAt);
        }
    }
}
