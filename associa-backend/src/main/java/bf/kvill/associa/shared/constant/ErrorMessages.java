
package bf.kvill.associa.shared.constant;

public class ErrorMessages {

    public static final String ASSOCIATION_NOT_FOUND = "Association non trouvée";
    public static final String USER_NOT_FOUND = "Utilisateur non trouvé";
    public static final String ROLE_NOT_FOUND = "Rôle non trouvé";
    public static final String POST_NOT_FOUND = "Poste non trouvé";
    public static final String MANDATE_NOT_FOUND = "Mandat non trouvé";

    public static final String EMAIL_ALREADY_EXISTS = "Cet email existe déjà";
    public static final String SLUG_ALREADY_EXISTS = "Ce slug existe déjà";

    public static final String UNAUTHORIZED = "Non autorisé";
    public static final String FORBIDDEN = "Accès refusé";

    public static final String INVALID_CREDENTIALS = "Identifiants invalides";
    public static final String ACCOUNT_DISABLED = "Compte désactivé";

    private ErrorMessages() {
        // Utility class
    }
}