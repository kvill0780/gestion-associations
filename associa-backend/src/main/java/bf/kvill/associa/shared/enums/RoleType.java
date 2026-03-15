package bf.kvill.associa.shared.enums;

import lombok.Getter;

@Getter
public enum RoleType {
    /**
     * Rôle de direction/leadership
     * Exemples : Président, Vice-Président, Trésorier
     */
    LEADERSHIP("leadership", "Direction"),

    /**
     * Rôle de commission/comité
     * Exemples : Responsable Communication, Responsable Événements
     */
    COMMITTEE("committee", "Commission"),

    /**
     * Rôle de membre simple
     * Permissions de base pour tous les membres
     */
    MEMBER("member", "Membre"),

    /**
     * Rôle personnalisé
     */
    CUSTOM("custom", "Personnalisé");
    private final String value;
    private final String label;

    RoleType(String value, String label) {
        this.value = value;
        this.label = label;
    }

}
