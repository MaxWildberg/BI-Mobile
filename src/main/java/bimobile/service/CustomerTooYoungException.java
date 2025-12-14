package bimobile.service;

public class CustomerTooYoungException extends RuntimeException {
    public CustomerTooYoungException(String message) {
        super(message);
    }
}
