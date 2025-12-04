package swe.group04.libraryms.exceptions;

public class MandatoryFieldException extends RuntimeException{
    
    public MandatoryFieldException(String message) {
        super(message);
    }
}
