package bf.kvill.associa.members.user.policy;

import bf.kvill.associa.members.user.User;
import bf.kvill.associa.shared.enums.MembershipStatus;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Politique d'approbation des membres
 */
@Component
public class MemberApprovalPolicy {

    private static final Logger log = LoggerFactory.getLogger(MemberApprovalPolicy.class);

    /**
     * Valide qu'un membre peut être approuvé
     */
    public void validateApproval(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Utilisateur non trouvé");
        }
        if (user.getMembershipStatus() == MembershipStatus.ACTIVE) {
            throw new IllegalStateException("Le membre est déjà actif");
        }
        if (user.getMembershipStatus() == MembershipStatus.SUSPENDED) {
            throw new IllegalStateException("Le membre est suspendu, impossible de l'approuver");
        }
        if (!user.isEmailVerified()) {
            log.warn("Approbation d'un membre sans email vérifié: " + user.getEmail());
        }
    }

    /**
     * Valide qu'un membre peut être suspendu
     */
    public void validateSuspension(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Utilisateur non trouvé");
        }

        if (user.isSuperAdmin()) {
            throw new IllegalStateException("Impossible de suspendre un super admin");
        }

        if (user.getMembershipStatus() == MembershipStatus.SUSPENDED) {
            throw new IllegalStateException("Le membre est déjà suspendu");
        }
    }
}