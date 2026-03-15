package bf.kvill.associa.shared.enums;

public enum EventParticipationStatus {
    REGISTERED("registered", "Inscrit"),
    ATTENDED("attended", "Présent"),
    ABSENT("absent", "Absent"),
    CANCELLED("cancelled", "Annulé");

    private final String value;
    private final String label;

    EventParticipationStatus(String value, String label) {
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
