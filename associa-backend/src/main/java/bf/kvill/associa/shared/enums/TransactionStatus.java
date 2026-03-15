package bf.kvill.associa.shared.enums;

public enum TransactionStatus {
    PENDING("pending", "En attente"),
    APPROVED("approved", "Approuvé"),
    REJECTED("rejected", "Rejeté");

    private final String value;
    private final String label;

    TransactionStatus(String value, String label) {
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
