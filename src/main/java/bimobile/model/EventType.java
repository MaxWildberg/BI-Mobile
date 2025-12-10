package bimobile.model;

public enum EventType {

    CREATED,          // Fahrzeug angelegt
    UPDATED,          // Fahrzeugdaten geändert
    STATUS_CHANGED,   // Status geändert
    MAINTENANCE,      // Wartung / HU
    RENTAL_START,     // Ausleihe starten
    RENTAL_END,       // Ausleihe beenden
    SOLD,             // Verkauf
    SCRAPPED          // Ausmusterung

}