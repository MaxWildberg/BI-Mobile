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
 *  Stellt die CRUD-Operationen Bereit und es wird eine Methode zur Prüfung,
 *  ob ein Fahrzeug schon ausgeliehen ist mit der Hilfe des Status eines Fahrzeugs.
 *
 *  Es wird eine Methode bereitgestellt die das Áuflisten aller Ausleihen zu einem Fahrzeug
 *  ermöglicht.
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
     * damit permanent beim Zugriff der Klassen untereinander die Injection aktualisiert wird
     * @author Leonard Köchling
     */
    // Holt alle Rentals und lädt den Customer,Facility und Fahrzeug gleich mit
    @Query("SELECT r FROM Rental r JOIN FETCH r.customer JOIN FETCH r.vehicle JOIN FETCH r.facility")
    List<Rental> findAllWithCustomerVehicleFacility();

    //für das zurückgeben der Rental braucht man hier das jeweilige Rental-Objekt
    @Query("SELECT r FROM Rental r " +
            "JOIN FETCH r.customer " +
            "JOIN FETCH r.vehicle " +
            "JOIN FETCH r.facility " +
            "WHERE r.id = :id")
    Rental findByIdWithAllAttributes(@Param("id") Long id);
}
