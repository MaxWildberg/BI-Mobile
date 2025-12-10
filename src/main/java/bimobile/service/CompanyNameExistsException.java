package bimobile.service;

public class CompanyNameExistsException extends RuntimeException{
    public CompanyNameExistsException(String name){
        super("Firma mit Name: '" + name + "'existiert bereits");
    }
}
