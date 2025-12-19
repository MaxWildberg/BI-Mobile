package bimobile.model;

/**
 * Enum für die verschiedenen Zustände eines Fahrzeugs.
 * @author Halil Sentürk
 */

public enum VehicleStatus {
    AVAILABLE,        // verfügbar
    RENTED,           // verliehen
    IN_MAINTENANCE,   // in Wartung / HU
    SCRAPPED,         // ausgemustert (Endzustand)
    SOLD              // verkauft
    ;

    public String getDisplayName() {
        return this.name();
    }
}