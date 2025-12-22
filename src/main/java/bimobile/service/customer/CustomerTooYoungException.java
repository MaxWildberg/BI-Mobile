package bimobile.service.customer;

/**
 * Exception Klasse
 * Wird ausgelöst, wenn Gebrutsdatum bzw. Alter des Kunden < 18 ist.
 *
 * @author Max Wildberg
 */
public class CustomerTooYoungException extends RuntimeException {
    public CustomerTooYoungException(String message) {
        super(message);
    }
}
