package swe.group04.libraryms.exceptions;

public class MaxLoansReachedException extends RuntimeException{
    
    public MaxLoansReachedException(String message) {
        super(message);
    }
}
