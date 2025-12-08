package bimobile.sevice;

import bimobile.dao.RentalRepository;
import bimobile.enums.RentalStatus;
import bimobile.model.customer.Customer;
import bimobile.model.Facility;
import bimobile.model.Rental;
import bimobile.model.Vehicle;

import bimobile.model.customer.PrivateCustomer;
import bimobile.service.RentalService;
import bimobile.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Äquivalenzklassentests + zugehörige JUnit-Tests
 * für die RentalService.createRental() Methode.
 * @author Ben Berlin
 */

@ExtendWith(MockitoExtension.class)
public class RentalServiceEquivalenceTest {

	@Mock
	private RentalRepository rentalRepository;

	@Mock
	private VehicleService vehicleService;

	@InjectMocks
	private RentalService rentalService;

	// ÄQUIVALENZKLASSE 1: Enddatum < Startdatum → ungültig
	// Erwartung: IllegalArgumentException
	@Test
	void ek_invalidDateRange_throwsIllegalArgumentException() {
		Customer customer = new PrivateCustomer();
		Vehicle vehicle = mock(Vehicle.class);
		Facility facility = new Facility();

		LocalDate start = LocalDate.of(2025, 1, 10);
		LocalDate end   = LocalDate.of(2025, 1, 5); // ungültige Klasse

		assertThrows(IllegalArgumentException.class, () ->
				rentalService.createRental(customer, vehicle, facility, start, end)
		);
	}


	// ÄQUIVALENZKLASSE 2: Fahrzeug bereits aktiv ausgeliehen → ungültig
	// Erwartung: IllegalStateException
	@Test
	void ek_vehicleAlreadyActive_throwsIllegalStateException() {
		Customer customer = new PrivateCustomer();
		Vehicle vehicle = mock(Vehicle.class);
		Facility facility = new Facility();

		LocalDate start = LocalDate.of(2025, 2, 1);
		LocalDate end   = LocalDate.of(2025, 2, 3);

		// Fahrzeug ist laut Repository bereits in einer aktiven Ausleihe
		when(rentalRepository.existsByVehicleAndStatusIn(eq(vehicle), any(Set.class)))
				.thenReturn(true);

		assertThrows(IllegalStateException.class, () ->
				rentalService.createRental(customer, vehicle, facility, start, end)
		);
	}


	// JUNIT-TEST 1: Gültige Ausleihe erzeugt korrektes Rental-Objekt
	// (Repräsentiert die gültige Äquivalenzklasse)
	@Test
	void validRental_createsRentalSuccessfully() {
		Customer customer = new PrivateCustomer();
		Vehicle vehicle = mock(Vehicle.class);
		Facility facility = new Facility();

		LocalDate start = LocalDate.of(2025, 3, 1);
		LocalDate end   = LocalDate.of(2025, 3, 4);

		when(vehicle.getNextInspectionDate()).thenReturn(null);
		when(vehicle.getNextServiceDate()).thenReturn(null);
		when(vehicle.getDailyRate()).thenReturn(50.0);

		// keine aktive Ausleihe
		when(rentalRepository.existsByVehicleAndStatusIn(eq(vehicle), any(Set.class)))
				.thenReturn(false);

		// Repository speichert das Rental
		when(rentalRepository.save(any(Rental.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		Rental rental = rentalService.createRental(customer, vehicle, facility, start, end);

		assertNotNull(rental);
		assertEquals(RentalStatus.ACTIVE, rental.getStatus());
		assertEquals(3 * 50.0, rental.getTotalPrice()); // 3 Tage
		verify(vehicle).setAvailable(false);
		verify(vehicleService).save(vehicle);
	}


	// JUNIT-TEST 2: Fahrzeug gesperrt wegen Inspektion/HU
	// → IllegalStateException
	// (Weitere ungültige Äquivalenzklasse)
	@Test
	void vehicleBlockedDueToInspection_throwsIllegalStateException() {
		Customer customer = new PrivateCustomer();
		Vehicle vehicle = mock(Vehicle.class);
		Facility facility = new Facility();

		LocalDate start = LocalDate.of(2025, 4, 1);
		LocalDate end   = LocalDate.of(2025, 4, 5);

		// HU steht genau am Startdatum an → Fahrzeug darf nicht verliehen werden
		when(vehicle.getNextInspectionDate()).thenReturn(LocalDate.of(2025, 4, 1));

		assertThrows(IllegalStateException.class, () ->
				rentalService.createRental(customer, vehicle, facility, start, end)
		);
	}
}
