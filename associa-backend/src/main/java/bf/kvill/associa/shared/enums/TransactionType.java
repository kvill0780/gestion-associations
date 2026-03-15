package bf.kvill.associa.shared.enums;

public enum TransactionType {
    INCOME("income", "Recette"),
    EXPENSE("expense", "Dépense");

    private final String value;
    private final String label;

    TransactionType(String value, String label) {
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
