/**
 * @file LibraryArchiveService.java
 * @brief Servizio applicativo per l'accesso e la persistenza dell'archivio della biblioteca.
 *
 * Questo servizio fa da “facciata” rispetto all'oggetto LibraryArchive
 * e ai meccanismi di persistenza su file. Fornisce metodi di utilità per:
 * - caricare e salvare l'archivio,
 * - ottenere liste complete di libri, utenti e prestiti.
 */
package swe.group04.libraryms.service;

import java.util.List;
import swe.group04.libraryms.models.Book;
import swe.group04.libraryms.models.LibraryArchive;
import swe.group04.libraryms.models.Loan;
import swe.group04.libraryms.models.User;
import swe.group04.libraryms.persistence.FileService;

/**
 * @brief Servizio di alto livello per la gestione dell'archivio della biblioteca.
 *
 * Incapsula l'istanza di LibraryArchive e delega a FileService
 * le operazioni di lettura/scrittura su file.
 */
public class LibraryArchiveService {
    
    private LibraryArchive archive; ///<Archivio effettivo
    private FileService fileService; ///<Dipendenza per la persistenza su file
    private String archivePath; ///<Contiene il percorso del file che contiente l'archivio
    
    /**
     * @brief Carica l'archivio da file e lo imposta come archivio corrente.
     *
     * @pre  fileService != null
     * @pre  archivePath != null
     *
     * @post archive == result  (se il caricamento ha successo)
     *
     * @return L'archivio caricato, oppure null finché il metodo
     *         non viene effettivamente implementato.
     *
     * @note Metodo attualmente non implementato: restituisce null.
     */
    public LibraryArchive loadArchive(){
        return null;
    }
    
    /**
     * @brief Salva l'archivio corrente su file.
     *
     * @pre  archive != null
     * @pre  fileService != null
     * @pre  archivePath != null
     *
     * @post true   // l'effetto è esterno: scrittura su file
     *
     * @param archive Archivio da salvare su file.
     *
     * @note Metodo attualmente non implementato: il corpo è vuoto.
     */
    public void saveArchive(LibraryArchive archive) {
        
    }
    
    /**
     * @brief Restituisce la lista completa dei libri presenti in archivio.
     *
     * @return Lista di tutti i libri registrati (può essere vuota),
     *         oppure null finché il metodo non viene implementato.
     */
    public List<Book> getAllBooks() {
        return null;
    }
    
    /**
     * @brief Restituisce la lista completa degli utenti registrati.
     *
     * @return Lista di tutti gli utenti registrati (può essere vuota),
     *         oppure null finché il metodo non viene implementato.
     */
    public List<User> getAllUsers() {
        return null;
    }
    
    /**
     * @brief Restituisce la lista completa dei prestiti registrati.
     *
     * @return Lista di tutti i prestiti (può essere vuota),
     *         oppure null finché il metodo non viene implementato.
     */
    public List<Loan> getAllLoans() {
        return null;
    }
}
