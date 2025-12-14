package bimobile.service;

public class DuplicateCompanyException extends RuntimeException{
    public DuplicateCompanyException(String name){
        super("Firma mit Name: '" + name + "'existiert bereits");
    }
}