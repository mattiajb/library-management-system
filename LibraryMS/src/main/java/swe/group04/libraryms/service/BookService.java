package swe.group04.libraryms.service;

import java.util.List;
import swe.group04.libraryms.exceptions.*;
import swe.group04.libraryms.models.Book;
import swe.group04.libraryms.models.LibraryArchive;

public class BookService {
    
    private LibraryArchive libraryArchive;
    private LibraryArchiveService libraryArchiveService;
    
    // Metodi di gestione CRUD dei libri
    
    public void addBook(Book book) throws MandatoryFieldException, InvalidIsbnException{
        
    }
    
    public void updateBook(Book book) throws MandatoryFieldException, InvalidIsbnException{
        
    }
    
    public void removeBook(Book book) throws UserHasActiveLoanException{
        
    }
    
    // Metodi di ricerca e ordinamento dei libri
    
    public List<Book> getBooksSortedByTitle() {
        return null;
    }
    
    public List<Book> searchBooks(String query) {
        return null;
    }
}
