package bimobile.model;

/**
 * Definiert die verfügbaren Benutzerrollen und Berechtigungsstufen.
 * Dient als Basis für die Zugriffssteuerung (Authorization) im gesamten System.
 *
 * @author Jan Lasse Stegmann
 */
public enum RoleType {

    // Geschäftsführer: Hat globalen Vollzugriff und ist keinem festen Standort zugeordnet
    MANAGING_DIRECTOR,

    // Standortleiter: Verwaltet exklusiv seinen zugewiesenen Standort und dessen Mitarbeiter
    GENERAL_MANAGER,

    // Regulärer Mitarbeiter: Basiszuggiff ohne administrative Rechte
    EMPLOYEE
}