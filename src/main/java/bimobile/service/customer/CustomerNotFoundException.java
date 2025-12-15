package bimobile.service.customer;


/**
 * Exception Klasse
 * Wird ausgelöst, wenn Kunde mit der ID nicht in Datenbank gefunden werden kann
 *
 * @author Max Wildberg
 */
public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(Long id) {
        super("Kunde mit ID " + id + " wurde nicht gefunden");
    }
}