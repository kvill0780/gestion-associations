package bf.kvill.associa.shared.enums;

import lombok.Getter;

@Getter
public enum AssociationType {

    COMMUNITY("community", "Communautaire"),

    /**
     * Association étudiante
     * Exemples : Bureau Des Étudiants (BDE), Association de filière
     */
    STUDENT("student", "Étudiante"),

    /**
     * Association professionnelle
     * Exemples : Ordre des médecins, Association d'anciens élèves
     */
    PROFESSIONAL("professional", "Professionnelle"),

    /**
     * Association culturelle
     * Exemples : Troupe de théâtre, Chorale, Club de lecture
     */
    CULTURAL("cultural", "Culturelle"),

    /**
     * Association sportive
     * Exemples : Club de football, Association de randonnée
     */
    SPORTS("sports", "Sportive"),

    /**
     * Association caritative/humanitaire
     * Exemples : ONG, Association d'aide aux démunis
     */
    CHARITY("charity", "Caritative"),

    /**
     * Association religieuse
     * Exemples : Paroisse, Association cultuelle
     */
    RELIGIOUS("religious", "Religieuse"),

    /**
     * Association de loisirs
     * Exemples : Club d'échecs, Association de jeux de société
     */
    LEISURE("leisure", "Loisirs"),

    /**
     * Association de défense de droits
     * Exemples : Association de consommateurs, Syndicat
     */
    ADVOCACY("advocacy", "Défense de droits"),

    /**
     * Autre type
     */
    OTHER("other", "Autre"),

    /**
     * Association système — réservée à l'usage interne de la plateforme
     * Ne jamais afficher dans les listes publiques
     */
    SYSTEM("system", "Système");

    private final String value;
    private final String label;

    AssociationType(String value, String label) {
        this.value = value;
        this.label = label;
    }

}
