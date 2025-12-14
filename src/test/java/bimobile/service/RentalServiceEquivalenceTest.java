package bimobile.service;

import bimobile.dao.RentalRepository;
import bimobile.dao.VehicleHistoryRepository;
import bimobile.enums.RentalStatus;
import bimobile.model.Facility;
import bimobile.model.Rental;
import bimobile.model.Vehicle;
import bimobile.model.VehicleHistoryEntry;
import bimobile.model.VehicleStatus;
import bimobile.model.customer.Customer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Äquivalenzklassentests + zugehörige JUnit-Tests
 * für die {@link bimobile.service.RentalService#createRental} Methode.
 * <p>
 * Die Tests dokumentieren die wichtigsten validierungsrelevanten Randfälle, sodass
 * nachvollziehbar bleibt, welche Eingaben akzeptiert oder abgelehnt werden. Sie dienen
 * damit gleichzeitig als lebendige Spezifikation für die Studienarbeit.
 *
 * @author Ben Berlin
 */

@ExtendWith(MockitoExtension.class)
public class RentalServiceEquivalenceTest {

	@Mock
	private RentalRepository rentalRepository;

	@Mock
	private VehicleService vehicleService;

	@Mock
	private InvoiceService invoiceService;

	@Mock
	private VehicleHistoryRepository historyRepository;

	@InjectMocks
	private RentalService rentalService;

	// ÄQUIVALENZKLASSE 1: Enddatum < Startdatum → ungültig
	// Erwartung: IllegalArgumentException
	@Test
	void ek_invalidDateRange_throwsIllegalArgumentException() {
		Customer customer = mock(Customer.class);
		Vehicle vehicle = mock(Vehicle.class);
		Facility facility = new Facility();

		when(vehicle.getStatus()).thenReturn(VehicleStatus.AVAILABLE);

		LocalDate start = LocalDate.of(2025, 1, 10);
		LocalDate end = LocalDate.of(2025, 1, 5); // ungültige Klasse

		assertThrows(IllegalArgumentException.class, () ->
				rentalService.createRental(customer, vehicle, facility, start, end)
		);
	}


	// ÄQUIVALENZKLASSE 2: Fahrzeug bereits aktiv ausgeliehen → ungültig
	// Erwartung: IllegalStateException
	@Test
	void ek_vehicleAlreadyActive_throwsIllegalStateException() {
		Customer customer = mock(Customer.class);
		Vehicle vehicle = mock(Vehicle.class);
		Facility facility = new Facility();

		when(vehicle.getStatus()).thenReturn(VehicleStatus.AVAILABLE);
		when(vehicle.isAvailable()).thenReturn(true);
		when(vehicle.isMaintenanceActive()).thenReturn(false);
		when(vehicle.getNextInspectionDate()).thenReturn(null);
		when(vehicle.getNextServiceDate()).thenReturn(null);

		LocalDate start = LocalDate.of(2025, 2, 1);
		LocalDate end = LocalDate.of(2025, 2, 3);

		// Fahrzeug ist laut Repository bereits in einer aktiven Ausleihe
		when(rentalRepository.existsByVehicleAndStatusIn(eq(vehicle), anySet()))
				.thenReturn(true);

		assertThrows(IllegalStateException.class, () ->
				rentalService.createRental(customer, vehicle, facility, start, end)
		);
	}


	// JUNIT-TEST 1: Gültige Ausleihe erzeugt korrektes Rental-Objekt
	// (Repräsentiert die gültige Äquivalenzklasse)
	@Test
	void validRental_createsRentalSuccessfully() {
		Customer customer = mock(Customer.class);

		when(customer.getFullName()).thenReturn("Max Mustermann");
		Vehicle vehicle = mock(Vehicle.class);
		Facility facility = new Facility();

		LocalDate start = LocalDate.of(2025, 3, 1);
		LocalDate end = LocalDate.of(2025, 3, 4);

		when(vehicle.getStatus()).thenReturn(VehicleStatus.AVAILABLE);
		when(vehicle.isAvailable()).thenReturn(true);
		when(vehicle.isMaintenanceActive()).thenReturn(false);
		when(vehicle.getNextInspectionDate()).thenReturn(null);
		when(vehicle.getNextServiceDate()).thenReturn(null);
		when(vehicle.getDailyRate()).thenReturn(50.0);

		// keine aktive Ausleihe
		when(rentalRepository.existsByVehicleAndStatusIn(eq(vehicle), anySet()))
				.thenReturn(false);

		// Repository speichert das Rental
		when(rentalRepository.save(any(Rental.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		when(historyRepository.save(any(VehicleHistoryEntry.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		Rental rental = rentalService.createRental(customer, vehicle, facility, start, end);

		assertNotNull(rental);
		assertEquals(RentalStatus.ACTIVE, rental.getStatus());
		assertEquals(3 * 50.0, rental.getTotalPrice()); // 3 Tage
		verify(vehicle).setStatus(VehicleStatus.RENTED);
		verify(vehicleService).save(vehicle);
		verify(historyRepository).save(any(VehicleHistoryEntry.class));
	}


	// JUNIT-TEST 2: Fahrzeug gesperrt wegen Inspektion/HU
	// → IllegalStateException
	// (Weitere ungültige Äquivalenzklasse)
	@Test
	void vehicleBlockedDueToInspection_throwsIllegalStateException() {
		Customer customer = mock(Customer.class);
		Vehicle vehicle = mock(Vehicle.class);
		Facility facility = new Facility();

		LocalDate start = LocalDate.of(2025, 4, 1);
		LocalDate end = LocalDate.of(2025, 4, 5);

		when(vehicle.getStatus()).thenReturn(VehicleStatus.AVAILABLE);
		when(vehicle.isAvailable()).thenReturn(true);
		when(vehicle.isMaintenanceActive()).thenReturn(false);

		// HU steht genau am Startdatum an → Fahrzeug darf nicht verliehen werden
		when(vehicle.getNextInspectionDate()).thenReturn(LocalDate.of(2025, 4, 1));

		assertThrows(IllegalStateException.class, () ->
				rentalService.createRental(customer, vehicle, facility, start, end)
		);
	}
}