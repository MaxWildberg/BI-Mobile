package bimobile.service.customer;

/**
 * Exception Klasse
 * Wird ausgelöst, wenn Firma mit der ID schon in der Datenbank existiert.
 *
 * @author Max Wildberg
 */
public class DuplicateCompanyException extends RuntimeException{
    public DuplicateCompanyException(String name){
        super("Firma mit Name: '" + name + "'existiert bereits");
    }
}