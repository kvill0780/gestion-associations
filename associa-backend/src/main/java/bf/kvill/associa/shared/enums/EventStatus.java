package bf.kvill.associa.shared.enums;

public enum EventStatus {
    DRAFT("draft", "Brouillon"),
    PUBLISHED("published", "Publié"),
    CANCELLED("cancelled", "Annulé"),
    COMPLETED("completed", "Terminé");

    private final String value;
    private final String label;

    EventStatus(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }
}
