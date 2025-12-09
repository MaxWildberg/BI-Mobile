package bimobile.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Persistentes Änderungsprotokoll für Ausleihvorgänge.
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
        // for JPA
    }

    public RentalChangeLog(Rental rental, String userIdentifier, String action, String details) {
        this.rental = rental;
        this.userIdentifier = userIdentifier;
        this.action = action;
        this.details = details;
        this.timestamp = LocalDateTime.now();

        //Snapshot der Rental-ID speichern, falls rental beim Löschen NULL wird
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
