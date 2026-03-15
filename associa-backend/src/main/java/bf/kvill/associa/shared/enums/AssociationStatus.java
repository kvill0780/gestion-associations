package bf.kvill.associa.shared.enums;

import lombok.Getter;

@Getter
public enum AssociationStatus {
    /**
     * Association active et opérationnelle
     * Les membres peuvent se connecter et utiliser toutes les fonctionnalités
     */
    ACTIVE("active", "Active"),

    /**
     * Association inactive temporairement
     * Les membres peuvent se connecter mais les fonctionnalités sont limitées
     */
    INACTIVE("inactive", "Inactive"),

    /**
     * Association suspendue
     * Les membres ne peuvent plus se connecter
     * Toutes les opérations sont bloquées
     * Utilisé en cas de problème (non-paiement, violation de règles, etc.)
     */
    SUSPENDED("pending", "Suspendue"),

    /**
     * Association archivée
     * Les données sont conservées en lecture seule
     * Utilisé pour les associations dissoutes ou terminées
     */
    ARCHIVED("archived", "Archivée");

    private final String value;
    private final String label;

    AssociationStatus(String value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * Vérifie si l'association peut être utilisée normalement
     */
    public boolean isOperational() {
        return this == ACTIVE;
    }

    /**
     * Vérifie si l'association est en lecture seule
     */
    public boolean isReadOnly() {
        return this == ARCHIVED;
    }

    /**
     * Vérifie si l'association est complètement bloquée
     */
    public boolean isBlocked() {
        return this == SUSPENDED;
    }

}
