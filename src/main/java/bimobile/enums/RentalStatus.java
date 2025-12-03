package bimobile.enums;

/**
 * Repräsentiert den Status einer Ausleihe im BI-Mobile-System.

 * CREATED - Ausleihe wurde angelegt, aber noch nicht aktiv.
 * ACTIVE - Fahrzeug ist aktuell an den Kunden vermietet.
 * COMPLETED - Ausleihe wurde vollständig abgeschlossen und abgerechnet.
 * CANCELLED - Ausleihe wurde vor der Fahrzeugübergabe storniert.
 */
public enum RentalStatus {
    CREATED,
    ACTIVE,
    COMPLETED,
    CANCELLED
}
