package bimobile.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Persistentes Änderungsprotokoll für Ausleihvorgänge.
 * <p>
 * Jeder Eintrag hält fest, welcher Benutzer welche Aktion zu welchem Zeitpunkt ausgelöst hat.
 * Zusätzlich wird eine textuelle Beschreibung gespeichert, damit spätere Analysen auch ohne
 * Domänenwissen nachvollziehbar bleiben. Im Gegensatz zu einem flüchtigen Audit loggen wir
 * hier bewusst die {@code rentalIdSnapshot}, damit ein Zusammenhang selbst nach dem Löschen
 * einer Ausleihe sichtbar bleibt.
 *
 * @author Ben Berlin
 */
@Entity
@Table(name = "rental_change_log")
public class RentalChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rental_id", nullable = true) // Null erlaubt
    private Rental rental;

    @Column(name= "rental_id_snapshot")
    private Long rentalIdSnapshot;

    @Column(nullable = false)
    private String userIdentifier;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false, length = 1000)
    private String details;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    protected RentalChangeLog() {
	    // für JPA. Der geschützte Konstruktor verhindert versehentliche Nutzung im Code.
    }

    public RentalChangeLog(Rental rental, String userIdentifier, String action, String details) {
        this.rental = rental;
        this.userIdentifier = userIdentifier;
        this.action = action;
        this.details = details;
        this.timestamp = LocalDateTime.now();

	    // Snapshot der Rental-ID speichern, falls rental beim Löschen NULL wird.
	    // So bleibt auch nach Cascade-Löschungen eine referenzierbare Spur erhalten.
        if(rental != null){
            this.rentalIdSnapshot = rental.getId();
        }
    }

    public Long getId() {
        return id;
    }

    public Rental getRental() {
        return rental;
    }

    public Long getRentalIdSnapshot() {
        return rentalIdSnapshot;
    }


    public String getUserIdentifier() {
        return userIdentifier;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    /**
     * Löst die Relation zur Ausleihe, wenn diese gelöscht wird, behält aber
     * eine textuelle Referenz bei, damit das Änderungsprotokoll erhalten bleibt.
     */
    public void detachRental() {
        if (this.rental != null && this.rentalIdSnapshot == null) {
            this.rentalIdSnapshot = this.rental.getId(); // Merken der ID
        }
        this.rental = null;
    }
}
