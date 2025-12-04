package bimobile.model;

public enum VehicleStatus {
    AVAILABLE ("Verfügbar"),
    RENTED ("Verliehen"),
    IN_MAINTENANCE ("In Wartung"),
    SCRAPPED ("Ausgemustert"),
    SOLD ("Verkauft");

    private final String displayName;

    VehicleStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
