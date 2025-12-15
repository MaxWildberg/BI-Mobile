package bimobile.service.customer;

public class CustomerTooYoungException extends RuntimeException {
    public CustomerTooYoungException(String message) {
        super(message);
    }
}
