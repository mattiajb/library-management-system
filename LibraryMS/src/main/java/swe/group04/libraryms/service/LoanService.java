package swe.group04.libraryms.service;

import java.time.LocalDate;
import java.util.List;
import swe.group04.libraryms.exceptions.*;
import swe.group04.libraryms.models.Book;
import swe.group04.libraryms.models.LibraryArchive;
import swe.group04.libraryms.models.Loan;
import swe.group04.libraryms.models.User;

public class LoanService {
    
    private LibraryArchive libraryArchive;
    private LibraryArchiveService libraryArchiveService;
    
    // Registrazioni nuovo prestito
    public Loan registerLoan(User user, Book book, LocalDate dueDate) throws NoAvailableCopiesException, MaxLoansReachedException, MandatoryFieldException{
        return null;
    }
    
    public void registerReturn(Loan loan) {
        
    }
    
    public List<Loan> getActiveLoan() {
        return null;
    }
    
    public List<Loan> sortActiveLoan() {
        return null;
    }
    
    public List<Loan> getActiveLoansByUser(User user){
        return null;
    }
    
    public List<Loan> getActiveLoanByBook(Book book) {
        return null;
    }
    
    public boolean isLate(Loan loan){
        return false;
    }
}
