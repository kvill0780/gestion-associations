package bf.kvill.associa.documents;

import bf.kvill.associa.documents.dto.DocumentResponse;
import bf.kvill.associa.security.userdetails.CustomUserPrincipal;
import bf.kvill.associa.system.audit.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Gestion des documents")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocumentService documentService;
    private final AuditService auditService;

    @Operation(summary = "Lister les documents")
    @GetMapping
    @PreAuthorize("hasPermission(null, 'documents.view')")
    public ResponseEntity<List<DocumentResponse>> getDocuments(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        Long associationId = principal.getAssociationId();
        List<DocumentResponse> documents = documentService.findByAssociation(associationId)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(documents);
    }

    @Operation(summary = "Téléverser un document")
    @PostMapping("/upload")
    @PreAuthorize("hasPermission(null, 'documents.upload')")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") @NotBlank String title,
            @RequestParam("category") @NotBlank String category) {
        Document document = documentService.upload(
                principal.getAssociationId(),
                principal.getId(),
                title,
                category,
                file);
        return ResponseEntity.ok(toResponse(document));
    }

    @Operation(summary = "Supprimer un document")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'documents.delete')")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        documentService.delete(id, principal.getAssociationId(), principal.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Télécharger un document")
    @GetMapping("/{id}/download")
    @PreAuthorize("hasPermission(null, 'documents.view')")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        Document document = documentService.findByIdAndAssociation(id, principal.getAssociationId());
        Resource resource = documentService.loadAsResource(id, principal.getAssociationId());

        auditService.log(
                "DOWNLOAD_DOCUMENT",
                "Document",
                id,
                principal.getId(),
                Map.of("title", document.getTitle()));

        String filename = document.getOriginalFileName() != null ? document.getOriginalFileName() : "document";

        return ResponseEntity.ok()
                .contentType(document.getFileType() != null
                        ? MediaType.parseMediaType(document.getFileType())
                        : MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    private DocumentResponse toResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .category(document.getCategory())
                .fileType(document.getFileType())
                .fileSize(document.getFileSize())
                .filePath(document.getFilePath())
                .createdAt(document.getCreatedAt())
                .build();
    }
}
