package bimobile.service.customer;

/**
 * Exception Klasse
 * Wird ausgelöst, wenn Kunde mit der E-Mail Adresse schon in der Datenbank existiert.
 *
 * @author Max Wildberg
 */
public class DuplicateCustomerException extends RuntimeException {
    public DuplicateCustomerException(String email) {
        super("Kunde mit E-Mail '" + email + "' existiert bereits");
    }
}