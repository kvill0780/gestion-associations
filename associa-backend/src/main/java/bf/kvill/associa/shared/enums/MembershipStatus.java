package bf.kvill.associa.shared.enums;

import lombok.Getter;

@Getter
public enum MembershipStatus {
    PENDING("pending", "En attente"),
    ACTIVE("active", "Actif"),
    EXPIRED("expired", "Expiré"),
    INACTIVE("inactive", "Inactif"),
    SUSPENDED("suspended", "Suspendu"),
    LEFT("left", "Parti");

    private final String value;
    private final String label;

    MembershipStatus(String value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * Vérifie si le membre peut se connecter
     */
    public boolean canLogin() {
        return this == ACTIVE;
    }

    /**
     * Vérifie si le membre est considéré comme actif
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

}
