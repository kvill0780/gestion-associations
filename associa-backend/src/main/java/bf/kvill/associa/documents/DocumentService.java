package bf.kvill.associa.documents;

import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.shared.exception.ResourceNotFoundException;
import bf.kvill.associa.shared.storage.DocumentStorageService;
import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.system.association.AssociationRepository;
import bf.kvill.associa.system.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final AssociationRepository associationRepository;
    private final UserRepository userRepository;
    private final DocumentStorageService storageService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<Document> findByAssociation(Long associationId) {
        return documentRepository.findByAssociationIdOrderByCreatedAtDesc(associationId);
    }

    @Transactional(readOnly = true)
    public Document findByIdAndAssociation(Long documentId, Long associationId) {
        return documentRepository.findByIdAndAssociationId(documentId, associationId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));
    }

    @Transactional
    public Document upload(Long associationId, Long userId, String title, String category, MultipartFile file) {
        Association association = associationRepository.findById(associationId)
                .orElseThrow(() -> new ResourceNotFoundException("Association", associationId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getAssociation() == null || !associationId.equals(user.getAssociation().getId())) {
            throw new AccessDeniedException("Action interdite hors de votre association");
        }

        try {
            DocumentStorageService.StoredDocument stored = storageService.store(file, associationId);

            Document document = Document.builder()
                    .association(association)
                    .createdBy(user)
                    .title(title)
                    .category(category)
                    .originalFileName(stored.originalFileName())
                    .storedFileName(stored.storedFileName())
                    .filePath(stored.relativePath())
                    .fileType(stored.contentType())
                    .fileSize(stored.size())
                    .build();

            Document saved = documentRepository.save(document);

            auditService.log(
                    "UPLOAD_DOCUMENT",
                    "Document",
                    saved.getId(),
                    user,
                    Map.of("title", saved.getTitle(), "category", saved.getCategory()));

            return saved;
        } catch (Exception e) {
            throw new RuntimeException("Impossible d'enregistrer le document: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void delete(Long documentId, Long associationId, Long userId) {
        Document document = findByIdAndAssociation(documentId, associationId);
        documentRepository.delete(document);

        auditService.log(
                "DELETE_DOCUMENT",
                "Document",
                documentId,
                userId,
                Map.of("title", document.getTitle()));
    }

    @Transactional(readOnly = true)
    public Resource loadAsResource(Long documentId, Long associationId) {
        Document document = findByIdAndAssociation(documentId, associationId);
        Path filePath = storageService.resolve(document.getFilePath());

        try {
            if (!Files.exists(filePath)) {
                throw new ResourceNotFoundException("Document", documentId);
            }
            return new UrlResource(filePath.toUri());
        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger le document", e);
        }
    }
}
