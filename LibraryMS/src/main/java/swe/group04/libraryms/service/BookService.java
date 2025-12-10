/**
 * @file BookService.java
 * @brief Servizio applicativo per la gestione dei libri.
 *
 * Questa classe incapsula la logica di business relativa al catalogo libri:
 * - operazioni CRUD
 * - controlli di validazione sugli input
 * - coordinamento con LibraryArchive e LibraryArchiveService per la persistenza.
 */
package swe.group04.libraryms.service;

import java.io.IOException;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import swe.group04.libraryms.exceptions.*;
import swe.group04.libraryms.models.*;

/**
 * @brief Implementa la logica di alto livello per la gestione del catalogo libri.
 *
 * Utilizza l'archivio centrale della biblioteca e il servizio di persistenza
 * per applicare le regole di business sulle operazioni relative ai libri.
 */
public class BookService {
    
    private LibraryArchive libraryArchive; // Archivio in memoria
    private LibraryArchiveService libraryArchiveService; // Servizio per la persistenza dell'archivio

    // Comparatore Lower-Case
    private static final Comparator<Book> BY_TITLE_COMPARATOR =
            Comparator.comparing(
                    b -> b.getTitle() == null
                            ? ""
                            : b.getTitle().toLowerCase()
            );

    /**
     * @brief Crea un nuovo BookService.
     *
     * @param libraryArchive        Archivio (non null).
     * @param libraryArchiveService Servizio per la gestione/persistenza dell'archivio (non null).
     */
    public BookService(LibraryArchive libraryArchive, LibraryArchiveService libraryArchiveService) {
        if (libraryArchive == null) {
            throw new IllegalArgumentException("libraryArchive non può essere nullo");
        }
        if (libraryArchiveService == null) {
            throw new IllegalArgumentException("libraryArchiveService non può essere nullo");
        }

        this.libraryArchive = libraryArchive;
        this.libraryArchiveService = libraryArchiveService;
    }

    /**
     * @brief Registra un nuovo libro nel catalogo.
     *
     * @pre  book != null
     * @pre  I campi obbligatori del libro sono valorizzati
     *       (titolo, autori, ISBN, numero di copie).
     * @pre  libraryArchive != null
     *
     * @post Il libro risulta presente nell'archivio.
     *
     * @param book Libro da aggiungere al catalogo.
     *
     * @throws MandatoryFieldException Se uno o più campi obbligatori non sono validi.
     * @throws InvalidIsbnException    Se l'ISBN non rispetta il formato atteso
     *                                 o è già presente nell'archivio.
     *
     * @note Metodo ancora da implementare: il corpo è vuoto.
     */
    public void addBook(Book book) throws MandatoryFieldException, InvalidIsbnException{
        validateBookMandatoryFields(book);
        validateIsbnFormat(book.getIsbn());
        validateIsbnUniquenessOnAdd(book.getIsbn());

        libraryArchive.addBook(book); // Aggiunge il libro all'archivio

        persistChanges(); // Persistenza delle modifiche
    }
    
    /**
     * @brief Aggiorna i dati di un libro esistente.
     *
     * @pre  book != null
     * @pre  Il libro esiste già nell'archivio.
     * @pre  libraryArchive != null
     *
     * @post I dati del libro nell'archivio riflettono quelli dell'oggetto passato.
     *
     * @param book Libro con i dati aggiornati.
     *
     * @throws MandatoryFieldException Se i nuovi dati violano vincoli di obbligatorietà.
     * @throws InvalidIsbnException    Se l'ISBN aggiornato non è valido o crea duplicati.
     *
     * @note Metodo ancora da implementare: il corpo è vuoto.
     */
    public void updateBook(Book book) throws MandatoryFieldException, InvalidIsbnException{
        validateBookMandatoryFields(book);
        validateIsbnFormat(book.getIsbn());

        persistChanges(); // Persistenza delle modifiche
    }
    
    /**
     * @brief Rimuove un libro dal catalogo.
     *
     * @pre  book != null
     * @pre  Il libro esiste nell'archivio.
     * @pre  libraryArchive != null
     *
     * @post Il libro non è più presente nell'archivio,
     *       a meno che non venga sollevata un'eccezione.
     *
     * @param book Libro da rimuovere.
     *
     * @throws UserHasActiveLoanException Se esistono prestiti attivi associati al libro.
     *
     * @note Metodo ancora da implementare: il corpo è vuoto.
     */
    public void removeBook(Book book) throws UserHasActiveLoanException{
        if (book == null) {
            throw new IllegalArgumentException("Il libro non può essere nullo");
        }

        List<Loan> loansForBook = libraryArchive.findLoansByBook(book);
        for (Loan loan : loansForBook) {
            if(loan != null && loan.isActive()){
                throw new UserHasActiveLoanException(
                        "Impossibile rimuovere il libro: sono presenti prestiti attivi associati."
                );
            }
        }

        libraryArchive.removeBook(book); // Rimozione effettiva
        persistChanges(); // Persistenza delle modifiche
    }

    /**
     * @brief Restituisce la lista di tutti i libri, ordinata per titolo.
     *
     * L'ordinamento è case-insensitive e non modifica l'ordinamento
     * interno dell'archivio (viene restituita una lista di copia).
     *
     * @return Lista di libri ordinata per titolo (mai null).
     */
    public List<Book> getBooksSortedByTitle() {
        List<Book> books = new ArrayList<>(libraryArchive.getBooks());
        books.sort(BY_TITLE_COMPARATOR);
        return books;
    }
    
    /**
     * @brief Ricerca libri in base a una stringa di query.
     *
     * La query può essere interpretata come titolo, autore o ISBN,
     * a seconda della logica implementata.
     *
     * @pre  query != null
     * @pre  libraryArchive != null
     *
     * @post true
     *
     * @param query Testo inserito dall'operatore.
     * @return Sottoinsieme del catalogo che corrisponde ai criteri di ricerca.
     *         Attualmente restituisce null finché il metodo non viene implementato.
     */
    public List<Book> searchBooks(String query) {
        if (query == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla.");
        }

        String normalized = query.trim().toLowerCase(); // Trasforma in lower-case

        // Query vuota: restituisce tutti i libri ordinati
        if (normalized.isEmpty()) {
            return getBooksSortedByTitle();
        }

        List<Book> result = new ArrayList<>(); // Lista che contiene risultato/i
        for (Book book : libraryArchive.getBooks()) {
            if (book == null) {
                continue;
            }

            boolean matches = false;

            // Titolo
            String title = book.getTitle();
            if (title != null && title.toLowerCase().contains(normalized)) {
                matches = true;
            }

            // Autori
            if (!matches && book.getAuthors() != null) {
                for (String author : book.getAuthors()) {
                    if (author != null && author.toLowerCase().contains(normalized)) {
                        matches = true;
                        break;
                    }
                }
            }

            // ISBN
            if (!matches) {
                String isbn = book.getIsbn();
                if (isbn != null && isbn.toLowerCase().contains(normalized)) {
                    matches = true;
                }
            }

            if (matches) {
                result.add(book);
            }
        }

        result.sort(BY_TITLE_COMPARATOR);
        return result;
    }

    /* --------------------------------------------------------------------- */
    /*                      Metodi di utilità interni                        */
    /* --------------------------------------------------------------------- */

    /**
     * @brief Verifica che i campi obbligatori di un libro siano valorizzati.
     *
     * Controlla:
     * - titolo non nullo/ne vuoto,
     * - almeno un autore,
     * - anno di pubblicazione positivo,
     * - ISBN non nullo/ne vuoto,
     * - numero totale di copie > 0,
     * - copie disponibili tra 0 e totale.
     *
     * @param book Libro da validare.
     *
     * @throws MandatoryFieldException se qualche vincolo non è rispettato.
     */
    private void validateBookMandatoryFields(Book book) throws MandatoryFieldException {
        if (book == null) {
            throw new MandatoryFieldException("Il libro non può essere nullo.");
        }

        // Titolo
        if (isNullOrBlank(book.getTitle())) {
            throw new MandatoryFieldException("Il titolo è obbligatorio.");
        }

        // Autori
        if (book.getAuthors() == null || book.getAuthors().isEmpty()) {
            throw new MandatoryFieldException("È necessario specificare almeno un autore.");
        }

        // Anno di pubblicazione (controllo di base: > 0)
        if (book.getReleaseYear() <= 0 || book.getReleaseYear() > Year.now().getValue()) {
            throw new MandatoryFieldException("L'anno di pubblicazione non è valido.");
        }

        // ISBN
        if (isNullOrBlank(book.getIsbn())) {
            throw new MandatoryFieldException("L'ISBN è obbligatorio.");
        }

        // Copie totali
        if (book.getTotalCopies() <= 0) {
            throw new MandatoryFieldException(
                    "Il numero totale di copie deve essere maggiore di zero."
            );
        }

        // Copie disponibili
        if (book.getAvailableCopies() < 0 ||
                book.getAvailableCopies() > book.getTotalCopies()) {
            throw new MandatoryFieldException(
                    "Le copie disponibili devono essere comprese tra 0 e il numero totale di copie."
            );
        }
    }

    /**
     * @brief Valida il formato di base dell'ISBN.
     *
     * Implementazione semplice: accetta ISBN con cifre, spazi e trattini,
     * e controlla che il numero di cifre (esclusi spazi e trattini) sia
     * tipicamente 10 o 13.
     *
     * @param isbn ISBN da validare (non null/ne vuoto).
     *
     * @throws InvalidIsbnException se il formato non è ritenuto valido.
     */
    private void validateIsbnFormat(String isbn) throws InvalidIsbnException {
        String normalized = isbn.replace("-", "").replace(" ", "");
        if (!normalized.chars().allMatch(Character::isDigit)) {
            throw new InvalidIsbnException("L'ISBN deve contenere solo cifre (eventuali trattini/spazi sono ammessi).");
        }

        int length = normalized.length();
        if (length != 10 && length != 13) {
            throw new InvalidIsbnException("L'ISBN deve contenere 10 o 13 cifre.");
        }
    }

    /**
     * @brief Verifica l'unicità dell'ISBN durante l'inserimento di un nuovo libro.
     *
     * @param isbn ISBN da verificare.
     *
     * @throws InvalidIsbnException se esiste già un libro con lo stesso ISBN.
     */
    private void validateIsbnUniquenessOnAdd(String isbn) throws InvalidIsbnException {
        Book existing = libraryArchive.findBookByIsbn(isbn);
        if (existing != null) {
            throw new InvalidIsbnException("Esiste già un libro in catalogo con lo stesso ISBN.");
        }
    }

    /**
     * @brief Effettua il salvataggio dell'archivio tramite LibraryArchiveService.
     *
     * Eventuali IOException vengono convertite in RuntimeException, in quanto
     * rappresentano un errore applicativo grave che non rientra nei normali
     * casi d'uso gestiti dall'operatore.
     */
    private void persistChanges() {
        try {
            libraryArchiveService.saveArchive(libraryArchive);
        } catch (IOException e) {
            throw new RuntimeException("Errore durante il salvataggio dell'archivio dei libri.", e);
        }
    }

    /**
     * @brief Ritorna true se la stringa è null o composta solo da spazi.
     *
     * @param value Stringa da controllare.
     * @return true se null o blank, false altrimenti.
     */
    private boolean isNullOrBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
