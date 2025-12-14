package bimobile.service;

public class CompanyNotFoundException extends RuntimeException {
    public CompanyNotFoundException(Long companyId) {
        super("Firma mit ID " + companyId + " wurde nicht gefunden");
    }
}
