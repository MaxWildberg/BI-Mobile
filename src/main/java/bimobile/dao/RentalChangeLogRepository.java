package bimobile.dao;

import bimobile.model.Rental;
import bimobile.model.RentalChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentalChangeLogRepository extends JpaRepository<RentalChangeLog, Long> {
	/**
	 * Repository für das persistente Änderungsprotokoll von Ausleihvorgängen.
	 * <p>
	 * Die Abfragen sind bewusst simpel gehalten, damit sie leicht nachvollzogen werden können:
	 * <ul>
	 *     <li>{@link #findAllByOrderByTimestampDesc()} liefert alle Einträge für eine chronologische
	 *         Historie im UI.</li>
	 *     <li>{@link #findByRental(Rental)} sucht gezielt die Protokolle einer konkreten Ausleihe, um
	 *         sie vor dem Löschen zu entkoppeln oder auszuwerten.</li>
	 * </ul>
	 */

    List<RentalChangeLog> findAllByOrderByTimestampDesc();

    List<RentalChangeLog> findByRental(Rental rental);
}
