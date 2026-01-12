package bimobile.service;
import bimobile.enums.RentalStatus;
import bimobile.model.customer.Customer;
import bimobile.model.EventType;
import bimobile.model.Facility;
import bimobile.model.Rental;
import bimobile.model.Vehicle;
import bimobile.model.VehicleHistoryEntry;
import bimobile.model.VehicleStatus;
import bimobile.dao.VehicleHistoryRepository;
import bimobile.dao.RentalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

/**
 * Geschäftslogik zur Verwaltung des Ausleihprozesses.
 *<p>
 * Der Service bündelt alle Schritte, die notwendig sind, um einen vollständigen
 * Lebenszyklus einer Ausleihe abzubilden. Dadurch lässt sich in Übungen und Projekten
 * nachvollziehen, welche Prüfungen in welcher Reihenfolge greifen müssen, bevor ein
 * Fahrzeug tatsächlich ausgeliehen oder zurückgegeben wird.
 *
 * Verantwortlichkeiten:
 * <ul>
 * <li>Validierung von Ausleihanfragen (Datenvollständigkeit, Datumslogik)</li>
 * <li>Prüfung der Fahrzeugverfügbarkeit</li>
 * <li>Berechnung des Gesamtpreises</li>
 * <li>Anlegen und Speichern von Ausleihen</li>
 * <li>Aktualisierung des Fahrzeugstatus</li>
 * <li>Abschluss von Ausleihen</li>
 * </ul>
 * @author Ben Berlin
 */
@Service
public class RentalService {

    /**
     * Status, die als "aktiv" gelten und eine parallele Ausleihe verhindern.
     */
    private static final Set<RentalStatus> ACTIVE_STATES =
            Set.of(RentalStatus.CREATED, RentalStatus.ACTIVE);

    private final RentalRepository rentalRepository;
    private final VehicleService vehicleService;
    private final InvoiceService invoiceService;
    private final VehicleHistoryRepository historyRepository;

    /**
     * Konstruktor-Injektion der Abhängigkeiten.
     *
     * @param rentalRepository Repository für Ausleihen
     * @param vehicleService   Service für Fahrzeugzugriffe
     */
    public RentalService(RentalRepository rentalRepository,
                         VehicleService vehicleService,
                         InvoiceService invoiceService,
                         VehicleHistoryRepository historyRepository) {
        this.rentalRepository = rentalRepository;
        this.vehicleService = vehicleService;
        this.invoiceService = invoiceService;
        this.historyRepository = historyRepository;
    }

    /**
     * Erstellt und speichert eine neue Ausleihe.
     * <p>
     * Ablauf:
     * 1. Validierung der Eingabedaten
     * 2. Prüfung der HU-/Wartungsregeln
     * 3. Prüfung, ob das Fahrzeug bereits eine aktive Ausleihe hat
     * 4. Berechnung von Mietdauer und Gesamtpreis
     * 5. Setzen des Ausleihstatus auf ACTIVE
     * 6. Markieren des Fahrzeugs als nicht verfügbar
     * 7. Speichern von Ausleihe und Fahrzeug
     *
     * @param customer  Kunde, der das Fahrzeug ausleiht
     * @param vehicle   Fahrzeug, das ausgeliehen wird
     * @param facility  Standort, an dem die Ausleihe durchgeführt wird
     * @param startDate Startdatum der Ausleihe
     * @param endDate   Enddatum der Ausleihe
     * @return gespeicherte Ausleihe
     */
    @Transactional
    public Rental createRental(Customer customer,
                               Vehicle vehicle,
                               Facility facility,
                               LocalDate startDate,
                               LocalDate endDate) {

        // Null prüfungen
        if (customer == null) {
            throw new IllegalArgumentException("Kunde darf nicht null sein.");
        }
        if (vehicle == null) {
            throw new IllegalArgumentException("Fahrzeug darf nicht null sein.");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start- und Enddatum dürfen nicht null sein.");
        }

        // Datumslogik
        // Check auf Fahrzeugstatus
        // verhindert, dass verkaufte oder ausgemusterte Autos verliehen werden
        // (wichtige Regel für die tägliche Disposition)
        if (vehicle.getStatus() == VehicleStatus.SOLD || vehicle.getStatus() == VehicleStatus.SCRAPPED) {
            throw new IllegalStateException(
                    "Das Fahrzeug ist verkauft oder ausgemustert und kann nicht mehr verliehen werden.");
        }

        // Datumslogik
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Enddatum muss am selben Tag oder nach dem Startdatum liegen.");
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) {
            days = 1; // Mindestdauer: 1 Tag
        }
        // Logik, ob das Fahrzeug verfügbar ist.
        // Der Aufruf kapselt alle Wartungs- und Statusprüfungen, damit createRental übersichtlich bleibt
        validateVehicleAvailability(vehicle, startDate, endDate);

        // Preisberechnung
        double dailyRate = vehicle.getDailyRate();
        double totalPrice = dailyRate * days;

        // Anlegen der Ausleihe
        Rental rental = new Rental(
                customer,
                vehicle,
                facility,
                startDate,
                endDate,
                totalPrice,
                RentalStatus.ACTIVE
        );

        // Fahrzeug auf nicht verfügbar setzen.
        // Der Statuswechsel ist bewusst vor dem Speichern platziert,
        // damit Concurrent Requests das Fahrzeug nicht doppelt blocken können.
        vehicle.setStatus(VehicleStatus.RENTED);
        vehicleService.save(vehicle);

        // History Eintrag für Ausleihe
        VehicleHistoryEntry entry = new VehicleHistoryEntry(
                vehicle,
                LocalDate.now(),
                EventType.RENTAL_START,
                "Ausleihe gestartet. Kunde: " + customer.getFullName() +
                        " (" + startDate + " bis " + endDate + ")"
        );
        historyRepository.save(entry);

        //Speichern der Ausleihe
        return rentalRepository.save(rental);
    }

    /**
     * Schließt eine bestehende Ausleihe ab und gibt das Fahrzeug wieder frei.
     *
     * @param rental        Ausleihe, die abgeschlossen werden soll
     * @param actualEndDate tatsächliches Rückgabedatum
     * @return aktualisierte Ausleihe
     */
    @Transactional
    public Rental completeRental(Rental rental, LocalDate actualEndDate) {
        if (rental == null) {
            throw new IllegalArgumentException("Ausleihe darf nicht null sein.");
        }

        if (actualEndDate != null && actualEndDate.isAfter(rental.getStartDate())) {
            rental.setEndDate(actualEndDate);
        }

        rental.setStatus(RentalStatus.COMPLETED);

        Vehicle vehicle = rental.getVehicle();
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicleService.save(vehicle);

        return rentalRepository.save(rental);
    }

    /**
     * Liefert alle Ausleihen zurück.
     *
     * @return Liste aller Ausleihen
     */
    @Transactional(readOnly = true)
    public List<Rental> findAll() {
        return rentalRepository.findAll();
    }

    /**
     * Prüft, ob Fahrzeug im angegebenen Zeitraum aufgrund von HU oder Wartung gesperrt ist.
     * @author Halil Sentürk
     */
    private boolean isVehicleBlockedByMaintenance(Vehicle vehicle,
                                                  LocalDate rentalStart,
                                                  LocalDate rentalEnd) {

        LocalDate nextInspection = vehicle.getNextInspectionDate();
        LocalDate nextService = vehicle.getNextServiceDate();

        // Überfällige HU/Wartung vor Mietbeginn = gesperrt
        if (nextInspection != null && nextInspection.isBefore(rentalStart)) {
            return true;
        }
        if (nextService != null && nextService.isBefore(rentalStart)) {
            return true;
        }

        // HU/Wartung fällt in den Mietzeitraum = gesperrt
        if (nextInspection != null &&
                (!nextInspection.isBefore(rentalStart) && !nextInspection.isAfter(rentalEnd))) {
            return true;
        }
        if (nextService != null &&
                (!nextService.isBefore(rentalStart) && !nextService.isAfter(rentalEnd))) {
            return true;
        }

        return false;
    }

    /**
     * Prüft, ob ausgewähltes Fahrzeug für den gewünschten Zeitraum ausgeliehen werden darf.
     * @author Halil Sentürk
     */
    private void validateVehicleAvailability(Vehicle vehicle, LocalDate rentalStart, LocalDate rentalEnd) {
        if (vehicle.getStatus() == VehicleStatus.SCRAPPED || vehicle.getStatus() == VehicleStatus.SOLD) {
            throw new IllegalStateException("Das Fahrzeug ist aus dem Bestand entfernt und kann nicht ausgeliehen werden.");
        }

        if (vehicle.getStatus() == VehicleStatus.IN_MAINTENANCE || vehicle.isMaintenanceActive()) {
            throw new IllegalStateException("Das Fahrzeug befindet sich aktuell in der Wartung und steht nicht zur Verfügung.");
        }

        if (vehicle.getStatus() == VehicleStatus.RENTED) {
            throw new IllegalStateException("Das Fahrzeug ist momentan nicht verfügbar oder bereits vermietet.");
        }

        if (!vehicle.isAvailable() && vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new IllegalStateException("Das Fahrzeug ist aktuell blockiert und nicht als verfügbar markiert.");
        }

	    if (isVehicleBlockedByMaintenance(vehicle, rentalStart, rentalEnd)) {
		    throw new IllegalStateException(
				    "GESPERRT wegen HU/Wartung. " +
						    "Mietzeitraum=" + rentalStart + " bis " + rentalEnd +
						    ", nextInspection=" + vehicle.getNextInspectionDate() +
						    ", nextService=" + vehicle.getNextServiceDate() +
						    ", maintenanceActive=" + vehicle.isMaintenanceActive() +
						    ", status=" + vehicle.getStatus()
		    );
	    }

        if (rentalRepository.existsByVehicleAndStatusIn(vehicle, ACTIVE_STATES)) {
            throw new IllegalStateException("Für dieses Fahrzeug existiert bereits eine aktive Ausleihe im System.");
        }
    }


    public void deleteRental(Rental rental) {
        if (rental == null || rental.getId() == null) {
            throw new IllegalArgumentException("Ungültige Ausleihe.");
        }
        Vehicle v = rental.getVehicle();
        v.setStatus(VehicleStatus.AVAILABLE);
        vehicleService.save(v);

        rentalRepository.delete(rental);

    }

    /**
     * Gibt die ausgewählte Ausleihe zurück
     * <p>
     * und übergibt sie der Rechnungs-Methode
     *
     * @param rental Speichert die zu bearbeitende Rental in saved ab
     * @return Rentalobjekt für welches die Rechnung erstellt wird
     * @author Leonard Köchling
     */
    public Rental returnRental(Rental rental) {
        Rental loaded = rentalRepository.findByIdWithAllAttributes(rental.getId());
        return returnRental(loaded, loaded.getVehicle().getMileage());
    }

    public Rental returnRental(Rental rental, int endMileage) {
	    return returnRental(rental, endMileage, null, false);
    }

	public Rental returnRental(Rental rental,
	                           int endMileage,
	                           String maintenanceNote,
	                           boolean maintenanceRequired) {

        Rental loaded = rentalRepository.findByIdWithAllAttributes(rental.getId());

        // Fahrzeug wieder freigeben
        Vehicle v = loaded.getVehicle();

        // 1. Validierung: Neuer KM Stand darf nicht kleiner sein als der alte
        if (endMileage < v.getMileage()) {
            throw new IllegalArgumentException("Neuer Kilometerstand darf nicht kleiner sein als der alte.");
        }

        // Alten Stand merken für die Rechnung
        int startMileage = v.getMileage();

        loaded.setStatus(RentalStatus.COMPLETED);

        // 2. Fahrzeug Status & Kilometer updaten
		if (maintenanceRequired) {
			v.setStatus(VehicleStatus.IN_MAINTENANCE);
			v.setMaintenanceActive(true);
		} else {
			v.setStatus(VehicleStatus.AVAILABLE);
			v.setMaintenanceActive(false);
		}
        v.setMileage(endMileage); // Kilometer setzen
        vehicleService.save(v);

        // 3. History Eintrag mit Kilometer Info
        VehicleHistoryEntry entry = new VehicleHistoryEntry(
                v,
                LocalDate.now(),
                EventType.RENTAL_END,
                "Fahrzeug zurückgegeben. Kunde: " + loaded.getCustomer().getFullName() +
                        ". Gefahren: " + (endMileage - startMileage) + " km. (Stand: " + endMileage + ")"
        );
        historyRepository.save(entry);
		if (maintenanceRequired) {
			String noteText = maintenanceNote != null && !maintenanceNote.isBlank()
					? maintenanceNote
					: "Wartung/Schadensmeldung bei Rückgabe.";
			VehicleHistoryEntry maintenanceEntry = new VehicleHistoryEntry(
					v,
					LocalDate.now(),
					EventType.MAINTENANCE,
					"Fahrzeug bei Rückgabe gesperrt. Notiz: " + noteText
			);
			historyRepository.save(maintenanceEntry);
		}
        Rental saved = rentalRepository.save(loaded);

        // Rechnung erzeugen
        invoiceService.createInvoiceForRental(saved, startMileage, endMileage);

        return saved;
    }


    /**
     * Aktualisiert eine bestehende Ausleihe und berechnet den Gesamtpreis neu
     *
     * @param rental   zu aktualisierende Ausleihe
     * @param facility neuer Standort
     * @param start    neues Startdatum
     * @param end      neues Enddatum
     * @return aktualisierte Ausleihe
     */
    @Transactional
    public Rental updateRental(Rental rental,
                               Facility facility,
                               LocalDate start,
                               LocalDate end) {
        if (rental == null){
            throw new IllegalArgumentException("Ausleihe darf nicht null sein.");
        }
        if (start == null || end == null) {
            throw new IllegalArgumentException("Bitte Start- und Enddatum angeben.");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Enddatum muss am selben Tag oder nach dem Startdatum liegen.");
        }

        Rental managedRental = rentalRepository.findByIdWithAllAttributes(rental.getId());
        if (managedRental == null) {
            throw new IllegalArgumentException("Ausleihe konnte nicht gefunden werden.");
        }

        //HU Prüfung nachträglich setzten
        Vehicle vehicle = managedRental.getVehicle();
        validateVehicleAvailabilityWhileRented(vehicle, start, end);

        managedRental.setFacility(facility);
        managedRental.setStartDate(start);
        managedRental.setEndDate(end);

        double totalPrice = calculateTotalPrice(managedRental.getVehicle(), start, end);
        managedRental.setTotalPrice(totalPrice);

        return rentalRepository.save(managedRental);
    }

    /**
     * Berechnet den Gesamtpreis für eine Ausleihe,
     * Min. ein Tag wird berechnet.
     *
     * @param vehicle Fahrzeug mit Tagespreis
     * @param start   Startdatum
     * @param end     Enddatum
     * @return Gesamtpreis
     */
    private double calculateTotalPrice(Vehicle vehicle, LocalDate start, LocalDate end) {
        long days = ChronoUnit.DAYS.between(start, end);
        if (days <= 0) {
            days = 1;
        }
        return days * vehicle.getDailyRate();
    }

    public List<Rental> findAllWithCustomerVehicleFacility() {
        return rentalRepository.findAllWithCustomerVehicleFacility();
    }

    // Findet Ausleihen für einen bestimmten Standort
    public List<Rental> findRentalsByFacility(Long facilityId) {
        return rentalRepository.findByFacilityId(facilityId);
    }

    /**
     * Validiert Verfügbarkeit eines Fahrzeugs während Ausleihe.
     * @author Halil Sentürk
     */
    private void validateVehicleAvailabilityWhileRented(Vehicle vehicle, LocalDate rentalStart, LocalDate rentalEnd) {
        if (vehicle.getStatus() == VehicleStatus.SCRAPPED || vehicle.getStatus() == VehicleStatus.SOLD) {
            throw new IllegalStateException("Das Fahrzeug ist aus dem Bestand entfernt und kann nicht ausgeliehen werden.");
        }

        if (vehicle.getStatus() == VehicleStatus.IN_MAINTENANCE || vehicle.isMaintenanceActive()) {
            throw new IllegalStateException("Das Fahrzeug befindet sich aktuell in der Wartung und steht nicht zur Verfügung.");
        }

        if (isVehicleBlockedByMaintenance(vehicle, rentalStart, rentalEnd)) {
            throw new IllegalStateException(
                    "Fahrzeug ist aufgrund fälliger oder in den Zeitraum fallender HU/Wartung gesperrt."
            );
        }
    }
}