package bimobile.service.customer;

/**
 * Exception Klasse
 * Wird ausgelöst, wenn Daten mit dem das Kunden-Objekt erstellt werden soll unvollständig sind.
 *
 * @author Max Wildberg
 */
public class InvalidCustomerDataException extends RuntimeException {
    public InvalidCustomerDataException(String message) {
        super(message);
    }
}
