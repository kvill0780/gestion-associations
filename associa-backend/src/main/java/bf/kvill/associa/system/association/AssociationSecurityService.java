package bf.kvill.associa.system.association;

import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.members.mandate.MandateRepository;
import bf.kvill.associa.members.post.Post;
import bf.kvill.associa.members.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service de sécurité pour les associations
 *
 * Vérifie les autorisations pour les actions sensibles
 */
@Service("associationSecurityService")
@RequiredArgsConstructor
public class AssociationSecurityService {

    private static final Logger log = LoggerFactory.getLogger(AssociationSecurityService.class);

    private final UserRepository userRepository;
    private final MandateRepository mandateRepository;
    private final PostRepository postRepository;

    /**
     * Vérifie si un utilisateur peut gérer une association
     *
     * Conditions :
     * - L'utilisateur est super admin
     * - OU l'utilisateur est président de cette association
     */
    public boolean canManage(Long associationId, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        // Super admin peut tout gérer
        if (user.isSuperAdmin()) {
            log.debug("User {} est super admin", userId);
            return true;
        }

        // Vérifier si l'utilisateur est président de cette association
        boolean isPresident = isPresidentOf(userId, associationId);

        if (isPresident) {
            log.debug("User {} est président de l'association {}", userId, associationId);
        } else {
            log.debug("User {} n'est pas président de l'association {}", userId, associationId);
        }

        return isPresident;
    }

    /**
     * Vérifie si un utilisateur est président d'une association
     */
    public boolean isPresidentOf(Long userId, Long associationId) {
        // Trouver le poste "Président" de cette association
        Post presidentPost = postRepository
                .findByNameAndAssociationId("Président", associationId)
                .orElse(null);

        if (presidentPost == null) {
            log.debug("Poste 'Président' non trouvé pour l'association {}", associationId);
            return false;
        }

        // Vérifier si l'utilisateur a un mandat actif sur ce poste
        return mandateRepository
                .existsByUserIdAndPostIdAndActiveTrue(userId, presidentPost.getId());
    }

    /**
     * Vérifie si un utilisateur appartient à une association
     */
    public boolean isMemberOf(Long userId, Long associationId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        return user.getAssociation() != null &&
                user.getAssociation().getId().equals(associationId);
    }

    /**
     * Vérifie si un utilisateur peut voir une association
     *
     * Conditions :
     * - L'association est active
     * - OU l'utilisateur en est membre
     * - OU l'utilisateur est super admin
     */
    public boolean canView(Long associationId, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        // Super admin peut tout voir
        if (user.isSuperAdmin()) {
            return true;
        }

        // Membre de l'association peut toujours voir
        if (isMemberOf(userId, associationId)) {
            return true;
        }

        // Sinon, vérifier que l'association est active (visible publiquement)
        // Cette logique peut être ajustée selon les besoins
        return false;
    }
}
