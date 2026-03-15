package bf.kvill.associa.members.mandate.policy;

import bf.kvill.associa.members.mandate.MandateRepository;
import bf.kvill.associa.members.mandate.dto.AssignPostRequest;
import bf.kvill.associa.members.post.Post;
import bf.kvill.associa.members.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
// @Slf4j - Removed due to Lombok issues
@RequiredArgsConstructor
public class MandateValidationPolicy {

    private static final Logger log = LoggerFactory.getLogger(MandateValidationPolicy.class);

    private final MandateRepository mandateRepository;
    private final PostRepository postRepository;

    /**
     * Valide une demande d'attribution de poste
     */
    public void validate(AssignPostRequest request) {
        validateMandateLimit(request.getUserId());
        validateDates(request);
        // ✅ NOUVELLE VALIDATION : Vérifier disponibilité du poste
        validatePostAvailability(request.getPostId());
    }

    /**
     * ✅ VALIDATION CLÉ : Vérifie si le poste peut accueillir un nouveau mandataire
     * 
     * Lève une exception si le poste a atteint sa limite maxOccupants
     *
     * @param postId ID du poste
     * @throws IllegalStateException si le poste est plein
     */
    public void validatePostAvailability(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Poste non trouvé: " + postId));

        if (post.getMaxOccupants() == null || post.getMaxOccupants() <= 0) {
            return;
        }

        long activeCount = mandateRepository.countCurrentActiveByPostId(postId, LocalDate.now());

        if (activeCount >= post.getMaxOccupants()) {
            throw new IllegalStateException(String.format(
                    "Le poste '%s' est complet: %d/%d occupant(s). " +
                            "Veuillez révoquer un mandat existant avant d'en créer un nouveau.",
                    post.getName(), activeCount, post.getMaxOccupants()));
        }

        log.debug("✅ Post available: {}/{} occupants for postId={}",
                activeCount, post.getMaxOccupants(), postId);
    }

    /**
     * Vérifie qu'un utilisateur ne dépasse pas la limite de postes
     * Limite : 3 postes simultanés maximum
     */
    private void validateMandateLimit(Long userId) {
        long activeCount = mandateRepository.countCurrentActiveByUserId(userId, LocalDate.now());

        if (activeCount >= 3) {
            throw new IllegalStateException(
                    "Un utilisateur ne peut pas avoir plus de 3 postes simultanés. " +
                            "Postes actifs actuels : " + activeCount);
        }
    }

    /**
     * Valide la cohérence des dates
     */
    private void validateDates(AssignPostRequest request) {
        if (request.getEndDate() != null) {
            if (request.getEndDate().isBefore(request.getStartDate())) {
                throw new IllegalArgumentException(
                        "La date de fin ne peut pas être antérieure à la date de début");
            }
        }
    }
}
