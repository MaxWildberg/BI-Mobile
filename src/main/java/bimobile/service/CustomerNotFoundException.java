package bimobile.service;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(Long id) {
        super("Kunde mit ID " + id + " wurde nicht gefunden");
    }
}

