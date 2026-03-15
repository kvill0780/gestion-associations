package bf.kvill.associa.documents.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DocumentResponse {
    private Long id;
    private String title;
    private String category;
    private String fileType;
    private Long fileSize;
    private String filePath;
    private LocalDateTime createdAt;
}
