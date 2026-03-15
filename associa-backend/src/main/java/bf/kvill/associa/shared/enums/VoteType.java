package bf.kvill.associa.shared.enums;

public enum VoteType {
    SIMPLE("simple", "Vote simple"),
    MULTIPLE("multiple", "Vote multiple");

    private final String value;
    private final String label;

    VoteType(String value, String label) {
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
