package bf.kvill.associa.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DocumentSummaryDto {
    private Long id;
    private String title;
    private String category;
    private String fileType;
    private Long fileSize;
    private LocalDateTime createdAt;
}
