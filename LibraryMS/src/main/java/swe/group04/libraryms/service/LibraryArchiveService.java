package swe.group04.libraryms.service;

import java.util.List;
import swe.group04.libraryms.models.Book;
import swe.group04.libraryms.models.LibraryArchive;
import swe.group04.libraryms.models.Loan;
import swe.group04.libraryms.models.User;
import swe.group04.libraryms.persistence.FileService;

public class LibraryArchiveService {
    
    private LibraryArchive archive; //Archivio effettivo
    private FileService fileService; //Dipendenza per la persistenza su file
    private String archivePath; //Contiene il percorso del file che contiente l'archivio
    
    public LibraryArchive loadArchive(){
        return null;
    }
    
    public void saveArchive(LibraryArchive archive) {
        return;
    }
    
    public List<Book> getAllBooks() {
        return null;
    }
    
    public List<User> getAllUsers() {
        return null;
    }
    
    public List<Loan> getAllLoans() {
        return null;
    }
}
