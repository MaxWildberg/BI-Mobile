package bimobile.dao;

import bimobile.model.Rental;
import bimobile.enums.RentalStatus;
import bimobile.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Repository für den Zugriff auf Ausleihen (Ausleihe).
 *
 * Stellt die CRUD-Operationen bereit und es wird eine Methode zur Prüfung,
 * ob ein Fahrzeug schon ausgeliehen ist mit der Hilfe des Status eines Fahrzeugs.
 *
 * Es wird eine Methode bereitgestellt die das Auflisten aller Ausleihen zu einem Fahrzeug
 * ermöglicht.
 *
 * Das Interface nutzt Spring Data JPA.
 * @author Ben Berlin
 */
@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

	/**
	 * Liefert alle Ausleihen mit einem bestimmten Status.
	 *
	 * @param status Status, nach dem gefiltert werden soll
	 * @return Liste der Ausleihen mit diesem Status
	 */
	List<Rental> findByStatus(RentalStatus status);

	/**
	 * Prüft, ob es für ein bestimmtes Fahrzeug eine Ausleihe mit einem der
	 * angegebenen Status gibt.
	 *
	 * Diese Methode stellt sicher, dass ein Fahrzeug nur einmal zu einem Zeitpunkt
	 * ausgeliehen werden kann.
	 *
	 * @param vehicle Fahrzeug, das geprüft werden soll
	 * @param statuses Menge von Status, die als "aktiv" betrachtet werden
	 * @return true, falls mindestens eine Ausleihe mit einem dieser Status existiert
	 */
	boolean existsByVehicleAndStatusIn(Vehicle vehicle, Collection<RentalStatus> statuses);

	/**
	 * Liefert alle Ausleihen zu einem bestimmten Fahrzeug.
	 *
	 * @param vehicle Fahrzeug
	 * @return Liste der Ausleihen dieses Fahrzeugs
	 */
	List<Rental> findByVehicle(Vehicle vehicle);


	/**
	 * Holt alle Rentals und lädt Customer, Vehicle und Facility direkt mit
	 * (Fetch Join), um LazyInitialization-Probleme im UI zu vermeiden.
	 */
	@Query("""
           SELECT DISTINCT r
           FROM Rental r
           JOIN FETCH r.customer
           JOIN FETCH r.vehicle
           LEFT JOIN FETCH r.facility
           """)
	List<Rental> findAllWithCustomerVehicleFacility();

	/**
	 * Holt ein einzelnes Rental inkl. aller benötigten Beziehungen
	 * (Customer, Vehicle, Facility). Wird u.a. für Rückgabe / Detailansicht genutzt.
	 */
	@Query("""
           SELECT r
           FROM Rental r
           JOIN FETCH r.customer
           JOIN FETCH r.vehicle
           LEFT JOIN FETCH r.facility
           WHERE r.id = :id
           """)
	Rental findByIdWithAllAttributes(@Param("id") Long id);

    /**
     * @return Gibt eine Liste aller Ausleihen passend zum Kunden und Fahrzeug zurück
     * @author Max Wildberg
     */
    @Query("SELECT r FROM Rental r " +
            "JOIN FETCH r.customer c " +
            "JOIN FETCH r.vehicle v")
    List<Rental> findAllWithCustomerAndVehicle();
}