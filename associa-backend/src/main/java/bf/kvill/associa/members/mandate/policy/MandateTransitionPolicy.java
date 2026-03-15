package bf.kvill.associa.members.mandate.policy;

import bf.kvill.associa.members.mandate.Mandate;
import bf.kvill.associa.members.mandate.MandateRepository;
import bf.kvill.associa.members.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Politique de transition des mandats
 * Gère la succession entre anciens et nouveaux mandataires
 */
@Component
@RequiredArgsConstructor
public class MandateTransitionPolicy {

    private static final Logger log = LoggerFactory.getLogger(MandateTransitionPolicy.class);

    private final MandateRepository mandateRepository;

    /**
     * Ferme le mandat actif pour un poste donné
     * 
     * @deprecated Utiliser {@link #closeUserMandate(Long, Long)} pour plus de
     *             précision
     * @param postId ID du poste
     * @return Le premier mandat fermé (si existe)
     */
    @Deprecated
    public Optional<Mandate> closeActiveMandate(Long postId) {
        List<Mandate> activeMandates = mandateRepository.findByPostIdAndActiveTrue(postId);

        if (!activeMandates.isEmpty()) {
            Mandate mandate = activeMandates.get(0);
            mandate.setActive(false);
            mandate.setEndDate(LocalDate.now());

            mandateRepository.save(mandate);

            log.warn("⚠️ Méthode dépréciée utilisée - Mandat {} fermé pour transition (poste: {})",
                    mandate.getId(), postId);

            return Optional.of(mandate);
        }

        log.debug("Aucun mandat actif à fermer pour le poste {}", postId);
        return Optional.empty();
    }

    /**
     * ✅ MÉTHODE RECOMMANDÉE : Ferme le mandat actif d'un utilisateur pour un poste
     * 
     * Cette méthode est précise et sûre car elle cible exactement quel mandat
     * fermer.
     * Utilisée pour les renouvellements ou démissions ciblées.
     *
     * @param userId ID de l'utilisateur
     * @param postId ID du poste
     * @return Le mandat fermé (si existe)
     */
    public Optional<Mandate> closeUserMandate(Long userId, Long postId) {
        log.debug("🔒 Closing mandate: userId={}, postId={}", userId, postId);

        Optional<Mandate> mandateOpt = mandateRepository
                .findByUserIdAndPostIdAndActiveTrue(userId, postId);

        if (mandateOpt.isPresent()) {
            Mandate mandate = mandateOpt.get();
            mandate.setActive(false);
            mandate.setEndDate(LocalDate.now());

            Mandate saved = mandateRepository.save(mandate);
            log.info("✅ Mandate closed: userId={}, postId={}, mandateId={}",
                    userId, postId, saved.getId());
            return Optional.of(saved);
        }

        log.debug("ℹ️ No active mandate to close for userId={}, postId={}", userId, postId);
        return Optional.empty();
    }

    /**
     * Ferme tous les mandats actifs d'un poste
     * ⚠️ Utiliser avec précaution (ex: suppression de poste, réorganisation)
     *
     * @param postId ID du poste
     * @return Liste des mandats fermés
     */
    public List<Mandate> closeAllActiveMandates(Long postId) {
        log.warn("⚠️ Closing ALL active mandates for postId={}", postId);

        List<Mandate> activeMandates = mandateRepository.findByPostIdAndActiveTrue(postId);

        activeMandates.forEach(mandate -> {
            mandate.setActive(false);
            mandate.setEndDate(LocalDate.now());
        });

        List<Mandate> saved = mandateRepository.saveAll(activeMandates);
        log.info("✅ Closed {} mandates for postId={}", saved.size(), postId);

        return saved;
    }

    /**
     * Vérifie s'il y a un mandat actif pour un poste
     */
    public boolean hasActiveMandate(Long postId) {
        return mandateRepository.existsByPostIdAndActiveTrue(postId);
    }

    /**
     * Récupère le mandataire actuel d'un poste
     */
    public List<Mandate> getCurrentMandataires(Long postId) {
        return mandateRepository.findCurrentByPostId(postId, LocalDate.now());
    }

    /**
     * Récupère les utilisateurs occupant actuellement un poste
     *
     * @param postId ID du poste
     * @return Liste des utilisateurs avec mandat actif
     */
    public List<User> getCurrentHolders(Long postId) {
        List<Mandate> activeMandates = mandateRepository.findCurrentByPostId(postId, LocalDate.now());

        return activeMandates.stream()
                .map(Mandate::getUser)
                .toList();
    }
}
