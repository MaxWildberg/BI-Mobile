package bimobile.service.customer;

/**
 * Exception Klasse
 * Wird ausgelöst, wenn Firma mit der ID nicht in Datenbank gefunden werden kann
 *
 * @author Max Wildberg
 */
public class CompanyNotFoundException extends RuntimeException {
    public CompanyNotFoundException(Long companyId) {
        super("Firma mit ID " + companyId + " wurde nicht gefunden");
    }
}
