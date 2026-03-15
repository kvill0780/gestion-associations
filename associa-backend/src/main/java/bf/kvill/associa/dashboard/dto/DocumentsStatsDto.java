package bf.kvill.associa.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DocumentsStatsDto {
    private Long total;
    private List<DocumentSummaryDto> recent;

    public DocumentsStatsDto() {
    }

    public DocumentsStatsDto(Long total, List<DocumentSummaryDto> recent) {
        this.total = total;
        this.recent = recent;
    }

    // ==================== Manual Getters/Setters (Lombok fallback)
    // ====================

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<DocumentSummaryDto> getRecent() {
        return recent;
    }

    public void setRecent(List<DocumentSummaryDto> recent) {
        this.recent = recent;
    }

    // ==================== Manual Builder (Lombok fallback) ====================

    public static DocumentsStatsDtoBuilder builder() {
        return new DocumentsStatsDtoBuilder();
    }

    public static class DocumentsStatsDtoBuilder {
        private Long total;
        private List<DocumentSummaryDto> recent;

        DocumentsStatsDtoBuilder() {
        }

        public DocumentsStatsDtoBuilder total(Long total) {
            this.total = total;
            return this;
        }

        public DocumentsStatsDtoBuilder recent(List<DocumentSummaryDto> recent) {
            this.recent = recent;
            return this;
        }

        public DocumentsStatsDto build() {
            DocumentsStatsDto dto = new DocumentsStatsDto();
            dto.setTotal(total);
            dto.setRecent(recent);
            return dto;
        }
    }
}
