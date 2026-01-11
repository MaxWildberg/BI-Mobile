package bimobile.controller;

import bimobile.model.Facility;
import bimobile.service.FacilityService;
import bimobile.dao.FacilityDAO;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Verwaltet die Standorte (anlegen, bearbeiten, löschen).
 *
 * Telefonnummer wird konsequent als String behandelt:
 * - führende Nullen bleiben erhalten
 * - +49 / Leerzeichen möglich
 *
 * @author Jannick Braun
 */
@Controller
public class FacilityController {

	private final FacilityService facilityService;
	private final FacilityDAO facilityDAO;

	public FacilityController(FacilityService facilityService, FacilityDAO facilityDAO) {
		this.facilityService = facilityService;
		this.facilityDAO = facilityDAO;
	}

	public String standortAnlegen(String address, String mail, String telephoneNr) {
		// Adresse prüfen
		if (address == null || address.trim().isEmpty()) {
			return "Fehler: Adresse ist ein Pflichtfeld";
		}
		address = address.trim();
		if (address.length() > 200) {
			return "Fehler: Adresse ist zu lang (maximal 200 Zeichen)";
		}

		// Mail prüfen
		if (mail == null || mail.trim().isEmpty()) {
			return "Fehler: E-Mail ist ein Pflichtfeld";
		}
		mail = mail.trim();
		String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
		if (!mail.matches(emailRegex)) {
			return "Fehler: Ungültiges E-Mail Format";
		}

		// Telefon prüfen (String!)
		if (telephoneNr == null || telephoneNr.trim().isEmpty()) {
			return "Fehler: Telefonnummer ist ein Pflichtfeld";
		}
		telephoneNr = telephoneNr.trim();

		// erlaubt: Ziffern, Leerzeichen, + (z.B. +49 521 123456)
		if (!telephoneNr.matches("^[0-9+ ]+$")) {
			return "Fehler: Telefonnummer darf nur Ziffern, Leerzeichen und + enthalten";
		}
		if (telephoneNr.length() > 15) {
			return "Fehler: Telefonnummer ist zu lang (maximal 15 Zeichen)";
		}

		try {
			Facility facility = new Facility(address, mail, telephoneNr);
			facilityService.addFacility(facility);
			return "Erfolg: Standort '" + address + "' wurde erfolgreich angelegt";
		} catch (Exception e) {
			return "Fehler: Standort konnte nicht gespeichert werden - " + e.getMessage();
		}
	}

	public String standortBearbeiten(Long id, String address, String mail, String telephoneNr) {
		// ID prüfen
		if (id == null || id <= 0) {
			return "Fehler: Ungültige Standort-ID";
		}

		Facility facility = facilityDAO.getFacilityById(id);
		if (facility == null) {
			return "Fehler: Standort mit ID " + id + " wurde nicht gefunden";
		}

		// Adresse prüfen
		if (address == null || address.trim().isEmpty()) {
			return "Fehler: Adresse ist ein Pflichtfeld";
		}
		address = address.trim();
		if (address.length() > 200) {
			return "Fehler: Adresse ist zu lang (maximal 200 Zeichen)";
		}

		// Mail prüfen
		if (mail == null || mail.trim().isEmpty()) {
			return "Fehler: E-Mail ist ein Pflichtfeld";
		}
		mail = mail.trim();
		String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
		if (!mail.matches(emailRegex)) {
			return "Fehler: Ungültiges E-Mail Format";
		}

		// Telefon prüfen (String!)
		if (telephoneNr == null || telephoneNr.trim().isEmpty()) {
			return "Fehler: Telefonnummer ist ein Pflichtfeld";
		}
		telephoneNr = telephoneNr.trim();

		if (!telephoneNr.matches("^[0-9+ ]+$")) {
			return "Fehler: Telefonnummer darf nur Ziffern, Leerzeichen und + enthalten";
		}
		if (telephoneNr.length() > 15) {
			return "Fehler: Telefonnummer ist zu lang (maximal 15 Zeichen)";
		}

		try {
			facility.setAddress(address);
			facility.setMail(mail);
			facility.setTelephoneNr(telephoneNr);
			facilityDAO.updateFacility(facility);
			return "Erfolg: Standort wurde aktualisiert";
		} catch (Exception e) {
			return "Fehler: Standort konnte nicht aktualisiert werden - " + e.getMessage();
		}
	}

	public String standortDeaktivieren(Long id) {
		if (id == null || id <= 0) {
			return "Fehler: Ungültige Standort ID";
		}

		Facility facility = facilityDAO.getFacilityById(id);
		if (facility == null) {
			return "Fehler: Standort mit ID " + id + " wurde nicht gefunden";
		}

		try {
			facilityDAO.deleteFacility(id);
			return "Erfolg: Standort wurde gelöscht";
		} catch (Exception e) {
			return "Fehler: Standort konnte nicht gelöscht werden - " + e.getMessage();
		}
	}

	public List<Facility> getAllFacilities() {
		try {
			return facilityService.getAllFacilities();
		} catch (Exception e) {
			System.err.println("Fehler beim Abrufen der Standorte: " + e.getMessage());
			return List.of();
		}
	}
}
