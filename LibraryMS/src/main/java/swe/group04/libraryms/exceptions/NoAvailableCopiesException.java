package swe.group04.libraryms.exceptions;

public class NoAvailableCopiesException extends RuntimeException{
    
    public NoAvailableCopiesException(String message) {
        super(message);
    }
}
