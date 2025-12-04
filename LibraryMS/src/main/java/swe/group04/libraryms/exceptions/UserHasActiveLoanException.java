package swe.group04.libraryms.exceptions;

public class UserHasActiveLoanException extends RuntimeException{
    
    public UserHasActiveLoanException(String message) {
        super(message);
    }
}
