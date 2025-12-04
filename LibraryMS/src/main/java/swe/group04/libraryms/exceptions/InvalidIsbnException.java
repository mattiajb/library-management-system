package swe.group04.libraryms.exceptions;

public class InvalidIsbnException extends RuntimeException{
    
    public InvalidIsbnException(String message) {
        super(message);
    }
}
