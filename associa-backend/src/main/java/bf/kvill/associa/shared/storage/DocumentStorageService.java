package bf.kvill.associa.shared.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DocumentStorageService {

    private final Path rootLocation;

    public DocumentStorageService(@Value("${app.storage.documents:uploads/documents}") String root) {
        this.rootLocation = Paths.get(root).toAbsolutePath().normalize();
    }

    public StoredDocument store(MultipartFile file, Long associationId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Fichier manquant");
        }

        String originalName = file.getOriginalFilename();
        String safeName = sanitizeFileName(originalName != null ? originalName : "document");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String storedName = timestamp + "_" + safeName;

        Path associationDir = rootLocation.resolve(String.valueOf(associationId));
        Files.createDirectories(associationDir);

        Path target = associationDir.resolve(storedName).normalize();
        if (!target.startsWith(associationDir)) {
            throw new IOException("Chemin de stockage invalide");
        }

        Files.copy(file.getInputStream(), target);

        String relativePath = rootLocation.relativize(target).toString();
        return new StoredDocument(
                originalName,
                storedName,
                relativePath,
                file.getContentType(),
                file.getSize());
    }

    public Path resolve(String storedRelativePath) {
        return rootLocation.resolve(storedRelativePath).normalize();
    }

    private String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public record StoredDocument(
            String originalFileName,
            String storedFileName,
            String relativePath,
            String contentType,
            long size) {
    }
}
