package bf.kvill.associa.system.activity;

import bf.kvill.associa.members.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service pour le logging des activités utilisateur
 * Reproduit ActivityService et ActivityLogger de Laravel
 * 
 * Différence avec AuditLog :
 * - AuditLog : Actions critiques sur entités (create, update, delete)
 * - Activity : Actions utilisateur (login, view, download, etc.)
 */
@Service
@RequiredArgsConstructor
public class ActivityService {

    private static final Logger log = LoggerFactory.getLogger(ActivityService.class);

    // TODO: Créer ActivityRepository quand entité Activity sera créée
    // private final ActivityRepository activityRepository;

    /**
     * Enregistre une activité utilisateur
     * 
     * @param action      Action effectuée (ex: "login", "view_document",
     *                    "download_file")
     * @param user        Utilisateur qui a effectué l'action
     * @param description Description de l'action
     * @param metadata    Métadonnées additionnelles (optionnel)
     */
    @Transactional
    public void log(String action, User user, String description, Map<String, Object> metadata) {
        // TODO: Créer entité Activity et implémenter
        log.info("Activity: {} by user {} - {}", action, user != null ? user.getId() : "system", description);
    }

    /**
     * Enregistre une activité sans métadonnées
     */
    @Transactional
    public void log(String action, User user, String description) {
        log(action, user, description, null);
    }

    /**
     * Enregistre une connexion utilisateur
     */
    @Transactional
    public void logLogin(User user, String ipAddress) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("ip_address", ipAddress);
        metadata.put("user_agent", ""); // TODO: Récupérer depuis request

        log("login", user, "User logged in", metadata);
    }

    /**
     * Enregistre une déconnexion utilisateur
     */
    @Transactional
    public void logLogout(User user) {
        log("logout", user, "User logged out");
    }

    /**
     * Enregistre une consultation de document
     */
    @Transactional
    public void logDocumentView(User user, Long documentId, String documentName) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("document_id", documentId);
        metadata.put("document_name", documentName);

        log("view_document", user, "Viewed document: " + documentName, metadata);
    }

    /**
     * Enregistre un téléchargement de fichier
     */
    @Transactional
    public void logFileDownload(User user, Long fileId, String fileName) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("file_id", fileId);
        metadata.put("file_name", fileName);

        log("download_file", user, "Downloaded file: " + fileName, metadata);
    }

    /**
     * Récupère les activités récentes d'un utilisateur
     * 
     * @param userId ID de l'utilisateur
     * @param limit  Nombre maximum d'activités
     * @return Liste des activités
     */
    public List<Object> getRecentActivities(Long userId, int limit) {
        // TODO: Implémenter avec ActivityRepository
        // return activityRepository.findByUserIdOrderByCreatedAtDesc(userId,
        // PageRequest.of(0, limit));
        return List.of();
    }

    /**
     * Récupère les activités récentes d'une association
     * 
     * @param associationId ID de l'association
     * @param limit         Nombre maximum d'activités
     * @return Liste des activités
     */
    public List<Object> getAssociationActivities(Long associationId, int limit) {
        // TODO: Implémenter avec ActivityRepository
        // return
        // activityRepository.findByAssociationIdOrderByCreatedAtDesc(associationId,
        // PageRequest.of(0, limit));
        return List.of();
    }

    /**
     * Récupère les activités par type
     * 
     * @param action    Type d'action
     * @param startDate Date de début
     * @param endDate   Date de fin
     * @return Liste des activités
     */
    public List<Object> getActivitiesByAction(String action, LocalDateTime startDate, LocalDateTime endDate) {
        // TODO: Implémenter avec ActivityRepository
        return List.of();
    }

    /**
     * Nettoie les anciennes activités (plus de X jours)
     * 
     * @param daysToKeep Nombre de jours à conserver
     * @return Nombre d'activités supprimées
     */
    @Transactional
    public long cleanOldActivities(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);

        // TODO: Implémenter avec ActivityRepository
        // return activityRepository.deleteByCreatedAtBefore(cutoffDate);

        log.info("Cleaned activities older than {} days (cutoff: {})", daysToKeep, cutoffDate);
        return 0L;
    }
}
