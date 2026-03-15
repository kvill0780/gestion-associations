package bf.kvill.associa.shared.enums;

public enum VoteStatus {
    DRAFT("draft", "Brouillon"),
    ACTIVE("active", "Actif"),
    CLOSED("closed", "Clôturé");

    private final String value;
    private final String label;

    VoteStatus(String value, String label) {
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
