package bimobile.service;

public class DuplicateCustomerException extends RuntimeException {
    public DuplicateCustomerException(String email) {
        super("Kunde mit E-Mail '" + email + "' existiert bereits");
    }
}
