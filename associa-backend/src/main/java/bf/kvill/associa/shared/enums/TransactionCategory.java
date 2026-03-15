package bf.kvill.associa.shared.enums;

public enum TransactionCategory {
    MEMBERSHIP_FEE("membership_fee", "Cotisation"),
    DONATION("donation", "Don"),
    EVENT_REVENUE("event_revenue", "Recette événement"),
    OFFICE_SUPPLIES("office_supplies", "Fournitures"),
    EQUIPMENT("equipment", "Équipement"),
    SERVICES("services", "Services"),
    OTHER("other", "Autre");

    private final String value;
    private final String label;

    TransactionCategory(String value, String label) {
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
