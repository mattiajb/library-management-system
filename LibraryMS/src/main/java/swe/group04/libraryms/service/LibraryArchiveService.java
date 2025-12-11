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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import swe.group04.libraryms.models.Book;
import swe.group04.libraryms.models.LibraryArchive;
import swe.group04.libraryms.models.Loan;
import swe.group04.libraryms.models.User;
import swe.group04.libraryms.persistence.ArchiveFileService;
import swe.group04.libraryms.persistence.FileService;

/**
 * @brief Servizio di alto livello per la gestione dell'archivio della biblioteca.
 *
 * Incapsula l'istanza di LibraryArchive e delega a FileService
 * le operazioni di lettura/scrittura su file.
 */
public class LibraryArchiveService {
    
    private LibraryArchive libraryArchive; /// Archivio effettivo
    private ArchiveFileService archiveFileService; // Gestione I/O

    /**
     * @brief Crea un nuovo LibraryArchiveService.
     *
     * @param archiveFileService Servizio di persistenza da utilizzare (non null).
     *
     * @throws IllegalArgumentException se archiveFileService è null.
     */
    public LibraryArchiveService(ArchiveFileService archiveFileService) {
        if (archiveFileService == null) {
            throw new IllegalArgumentException("archiveFileService non può essere nullo");
        }
        this.archiveFileService = archiveFileService;
    }

    /**
     * @brief Costruttore pensato per l'uso con ServiceLocator.
     *
     * Accetta un archivio già creato (condiviso tra i vari service) e
     * inizializza internamente un ArchiveFileService di default
     * per la persistenza su file.
     *
     * @param libraryArchive Istanza di LibraryArchive da gestire (non null).
     *
     * @throws IllegalArgumentException se libraryArchive è null.
     */
    public LibraryArchiveService(LibraryArchive libraryArchive) {
        if (libraryArchive == null) {
            throw new IllegalArgumentException("libraryArchive non può essere nullo");
        }
        this.libraryArchive = libraryArchive;

        // Configurazione di default del servizio di persistenza.
        // Puoi eventualmente estrarre il percorso in una costante.
        this.archiveFileService = new ArchiveFileService(
                "library-archive.dat",
                new FileService()
        );
    }

    /**
     * @brief Garantisce che libraryArchive sia inizializzato.
     *
     * Se è null, crea un nuovo archivio vuoto.
     */
    private void ensureArchiveInitialized() {
        if (this.libraryArchive == null) {
            this.libraryArchive = new LibraryArchive();
        }
    }

    /**
     * @brief Restituisce l'archivio attualmente gestito dal servizio.
     *
     * Se nessun archivio è stato ancora caricato o creato, viene creato
     * un nuovo archivio vuoto.
     *
     * @return Istanza di LibraryArchive mai null.
     */
    public LibraryArchive getLibraryArchive() {
        ensureArchiveInitialized();
        return libraryArchive;
    }

    /**
     * @brief Carica l'archivio da file e lo imposta come archivio corrente.
     *
     * @pre  L'archivio è stato configurato validamente
     *
     * @post Se il file esiste, libraryArchive == archivio caricato da file
     *
     * @return L'archivio caricato
     *
     */
    public LibraryArchive loadArchive() throws IOException {
        try {
            LibraryArchive loaded = archiveFileService.loadArchive();

            // Se viene restituito null, creazione archivio vuoto
            if (loaded == null) {
                this.libraryArchive = new LibraryArchive();
            } else {
                this.libraryArchive = loaded;
            }
        } catch (IOException e) {
            if (e instanceof FileNotFoundException) {
                this.libraryArchive = new LibraryArchive();
            } else {
                throw e;
            }
        }

        return this.libraryArchive;
    }

    /**
     * @brief Salva l'archivio corrente su file.
     *
     * @pre  archive != null
     * @pre  L'ArchiveFileService è correttamente configurato.
     *
     * @post L'archivio viene serializzato sul file configurato.
     *
     * @param archive Archivio da salvare su file.
     *
     * @throws IOException Se si verifica un errore durante la scrittura.
     */
    public void saveArchive(LibraryArchive archive) throws IOException {
        if (archive == null) {
            throw new IllegalArgumentException("archive non può essere nullo");
        }

        // Aggiorniamo il riferimento interno per mantenere coerenza
        this.libraryArchive = archive;

        // Deleghiamo ad ArchiveFileService la scrittura effettiva su disco
        archiveFileService.saveArchive(archive);
    }
    
    /**
     * @brief Restituisce la lista completa dei libri presenti in archivio.
     *
     * @return Lista di tutti i libri registrati (può essere vuota)
     */
    public List<Book> getAllBooks() {
        ensureArchiveInitialized();
        return libraryArchive.getBooks();
    }
    
    /**
     * @brief Restituisce la lista completa degli utenti registrati.
     *
     * @return Lista di tutti gli utenti registrati (può essere vuota).
     *
     */
    public List<User> getAllUsers() {
        ensureArchiveInitialized();
        return libraryArchive.getUsers();
    }
    
    /**
     * @brief Restituisce la lista completa dei prestiti registrati.
     *
     * @return Lista di tutti i prestiti (può essere vuota).
     */
    public List<Loan> getAllLoans() {
        ensureArchiveInitialized();
        return libraryArchive.getLoans();
    }
}
