package bf.kvill.associa.members.mandate.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RevokeMandateRequest {
    private LocalDate endDate;
    private String reason;

    public RevokeMandateRequest() {
    }

    public RevokeMandateRequest(LocalDate endDate, String reason) {
        this.endDate = endDate;
        this.reason = reason;
    }

    // ==================== Manual Getters/Setters (Lombok fallback)
    // ====================

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}