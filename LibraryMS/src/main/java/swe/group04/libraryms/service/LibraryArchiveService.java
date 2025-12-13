/**
 * @file LibraryArchiveService.java
 * @brief Servizio applicativo per l'accesso e la persistenza dell'archivio della biblioteca.
 *
 * Questo servizio funge da facciata rispetto all'oggetto di dominio LibraryArchive
 * e ai meccanismi di persistenza su file.
 *
 * - mantiene un riferimento coerente all'istanza corrente di LibraryArchive;
 * - carica/salva l'archivio tramite ArchiveFileService;
 * - fornisce metodi di accesso alle collezioni aggregate.
 *
 * @note Questo servizio NON applica regole di business sui dati:
 *       tali responsabilità appartengono ai service specifici.
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
 * Incapsula l'istanza di LibraryArchive e delega ad ArchiveFileService
 * le operazioni di lettura/scrittura su file.
 */
public class LibraryArchiveService {
    
    //  Archivio effettivamente gestito dal servizio
    private LibraryArchive libraryArchive; //<  Archivio effettivo

    //  Componente di persistenza incaricato della lettura/scrittura su file
    private ArchiveFileService archiveFileService; //<  Gestione I/O

    /**
     * @brief Crea un nuovo LibraryArchiveService configurato con un servizio di persistenza.
     *
     * Questo costruttore permette di iniettare esplicitamente il componente
     * ArchiveFileService (utile per test e configurazioni alternative).
     *
     * @param archiveFileService Servizio di persistenza da utilizzare (non null).
     *
     * @pre  archiveFileService != null
     * @post this.archiveFileService == archiveFileService
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
     * Accetta un'istanza di LibraryArchive già creata (potenzialmente condivisa
     * tra i vari service applicativi) e configura internamente un ArchiveFileService
     * di default per la persistenza su file.
     *
     * @param libraryArchive Istanza di LibraryArchive da gestire (non null).
     *
     * @pre  libraryArchive != null
     * @post this.libraryArchive == libraryArchive
     * @post this.archiveFileService != null
     *
     * @throws IllegalArgumentException se libraryArchive è null.
     */
    public LibraryArchiveService(LibraryArchive libraryArchive) {
        if (libraryArchive == null) {
            throw new IllegalArgumentException("libraryArchive non può essere nullo");
        }
        this.libraryArchive = libraryArchive;

        //  Configurazione di default del servizio di persistenza
        this.archiveFileService = new ArchiveFileService(
                "library-archive.dat",
                new FileService()
        );
    }

    /**
     * @brief Garantisce che l'archivio interno sia inizializzato.
     *
     * Se libraryArchive è null, viene creato un nuovo archivio vuoto.
     *
     * @post libraryArchive != null
     *
     * @note Questo metodo implementa un'inizializzazione "lazy":
     *       l'archivio viene creato solo quando necessario.
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
     * @pre  true
     * @post libraryArchive != null
     *
     * @return Istanza di LibraryArchive (mai null).
     */
    public LibraryArchive getLibraryArchive() {
        ensureArchiveInitialized();
        return libraryArchive;
    }

    /**
     * @brief Carica l'archivio da file e lo imposta come archivio corrente.
     *
     * - tenta la lettura tramite archiveFileService.loadArchive();
     * - se il file non esiste, crea un archivio vuoto;
     * - se l'oggetto caricato è null, crea un archivio vuoto;
     * - per altre IOException rilancia l'eccezione.
     *
     * @pre  archiveFileService != null
     *
     * @post libraryArchive != null
     * @post Se il file è leggibile e contiene un LibraryArchive valido:
     *       libraryArchive == archivio caricato da file
     * @post Se il file non esiste o il caricamento restituisce null:
     *       libraryArchive è un nuovo archivio vuoto
     *
     * @return L'archivio caricato (o creato vuoto).
     *
     * @throws IOException Se si verifica un errore di I/O diverso da FileNotFoundException.
     *
     * @note Questo metodo aggiorna lo stato interno del servizio, rendendo
     *       disponibile l'archivio ai service applicativi che lo interrogano.
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
     * - aggiorna il riferimento interno this.libraryArchive per coerenza;
     * - delega la scrittura a archiveFileService.saveArchive(...).
     *
     * @param archive Archivio da salvare su file.
     *
     * @pre  archive != null
     * @pre  archiveFileService != null
     *
     * @post this.libraryArchive == archive
     * @post L'archivio viene serializzato sul file configurato
     *
     * @throws IOException Se si verifica un errore durante la scrittura.
     * @throws IllegalArgumentException Se archive è nullo.
     *
     * @note Il metodo non “merge-a” dati: sostituisce il riferimento interno
     *       con l'oggetto passato.
     */
    public void saveArchive(LibraryArchive archive) throws IOException {
        if (archive == null) {
            throw new IllegalArgumentException("archive non può essere nullo");
        }

        //  Aggiornamento del riferimento interno per mantenere coerenza
        this.libraryArchive = archive;

        //  Delega ad ArchiveFileService della scrittura effettiva su disco
        archiveFileService.saveArchive(archive);
    }
    
    /**
     * @brief Restituisce la lista completa dei libri presenti in archivio.
     *
     * @pre  true
     * @post libraryArchive != null
     *
     * @return Lista di tutti i libri registrati (può essere vuota).
     *
     * @note Il contenuto dipende dallo stato corrente dell'archivio:
     *       se non è stato caricato da file, potrebbe essere vuoto.
     */
    public List<Book> getAllBooks() {
        ensureArchiveInitialized();
        return libraryArchive.getBooks();
    }
    
    /**
     * @brief Restituisce la lista completa degli utenti registrati.
     *
     * @pre  true
     * @post libraryArchive != null
     *
     * @return Lista di tutti gli utenti registrati (può essere vuota).
     */
    public List<User> getAllUsers() {
        ensureArchiveInitialized();
        return libraryArchive.getUsers();
    }
    
    /**
     * @brief Restituisce la lista completa dei prestiti registrati.
     *
     * @pre  true
     * @post libraryArchive != null
     *
     * @return Lista di tutti i prestiti (può essere vuota).
     */
    public List<Loan> getAllLoans() {
        ensureArchiveInitialized();
        return libraryArchive.getLoans();
    }
}