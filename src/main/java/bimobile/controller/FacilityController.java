package bimobile.controller;

import bimobile.model.Facility;
import bimobile.service.FacilityService;
import bimobile.dao.FacilityDAO;
import org.springframework.stereotype.Controller;
import java.util.List;

@Controller
public class FacilityController {

    private FacilityService facilityService;
    private FacilityDAO facilityDAO;

    public FacilityController(FacilityService facilityService, FacilityDAO facilityDAO) {
        this.facilityService = facilityService;
        this.facilityDAO = facilityDAO;
    }

    public String standortAnlegen(String address, String mail, String telephoneNr) {
        if (address == null || address.trim().isEmpty()) {
            return "Fehler: Adresse ist ein Pflichtfeld";
        }

        if (address.length() > 200) {
            return "Fehler: Adresse ist zu lang (maximal 200 Zeichen)";
        }

        if (mail == null || mail.trim().isEmpty()) {
            return "Fehler: E-Mail ist ein Pflichtfeld";
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!mail.matches(emailRegex)) {
            return "Fehler: Ungültiges E-Mail-Format";
        }

	    if (telephoneNr == null || telephoneNr.isBlank()) {
		    return "Fehler: Telefonnummer darf nicht leer sein.";
	    }

	    if (!telephoneNr.matches("^[0-9+ ]{6,20}$")) {
		    return "Fehler: Telefonnummer muss gültig sein (nur Ziffern, + und Leerzeichen).";
	    }


	    if (String.valueOf(telephoneNr).length() > 15) {
            return "Fehler: Telefonnummer ist zu lang";
        }

        try {
            Facility facility = new Facility(address.trim(), mail.trim(), telephoneNr);
            facilityService.addFacility(facility);
            return "Erfolg: Standort '" + address + "' wurde erfolgreich angelegt";
        } catch (Exception e) {
            return "Fehler: Standort konnte nicht gespeichert werden - " + e.getMessage();
        }
    }

    public String standortBearbeiten(Long id, String address, String mail, String telephoneNr) {
        if (id == null || id <= 0) {
            return "Fehler: Ungültige Standort-ID";
        }

        Facility facility = facilityDAO.getFacilityById(id);
        if (facility == null) {
            return "Fehler: Standort mit ID " + id + " wurde nicht gefunden";
        }

        if (address == null || address.trim().isEmpty()) {
            return "Fehler: Adresse ist ein Pflichtfeld";
        }

        if (address.length() > 200) {
            return "Fehler: Adresse ist zu lang (maximal 200 Zeichen)";
        }

        if (mail == null || mail.trim().isEmpty()) {
            return "Fehler: E-Mail ist ein Pflichtfeld";
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!mail.matches(emailRegex)) {
            return "Fehler: Ungültiges E-Mail-Format";
        }

        try {
            facility.setAddress(address.trim());
            facility.setMail(mail.trim());
            facility.setTelephoneNr(telephoneNr);
            facilityDAO.updateFacility(facility);
            return "Erfolg: Standort wurde aktualisiert";
        } catch (Exception e) {
            return "Fehler: Standort konnte nicht aktualisiert werden - " + e.getMessage();
        }
    }

    public String standortDeaktivieren(Long id) {
        if (id == null || id <= 0) {
            return "Fehler: Ungültige Standort-ID";
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