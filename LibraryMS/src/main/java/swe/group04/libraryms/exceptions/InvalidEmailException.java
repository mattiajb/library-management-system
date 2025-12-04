package swe.group04.libraryms.exceptions;

public class InvalidEmailException extends RuntimeException{
    
    public InvalidEmailException(String message) {
        super(message);
    }
}
