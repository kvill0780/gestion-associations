package bf.kvill.associa.shared.enums;

public enum PaymentMethod {
    CASH("cash", "Espèces"),
    CHECK("check", "Chèque"),
    TRANSFER("transfer", "Virement"),
    MOBILE_MONEY("mobile_money", "Mobile Money");

    private final String value;
    private final String label;

    PaymentMethod(String value, String label) {
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
