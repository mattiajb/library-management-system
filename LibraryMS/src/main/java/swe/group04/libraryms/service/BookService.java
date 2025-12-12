/**
 * @file BookService.java
 * @brief Servizio applicativo per la gestione dei libri.
 *
 * Incapsula la logica di business relativa al catalogo libri:
 * - inserimento, aggiornamento, rimozione;
 * - validazione dei dati;
 * - accesso all'archivio corrente tramite LibraryArchiveService;
 * - persistenza delle modifiche tramite LibraryArchiveService.
 *
 * @note Il servizio opera sullo stato dell'archivio restituito da LibraryArchiveService.
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
 * Applica vincoli di business sui dati dei libri e coordina
 * aggiornamenti su LibraryArchive con la persistenza.
 */
public class BookService {

    /** Servizio per l'accesso e la persistenza dell'archivio. */
    private LibraryArchiveService libraryArchiveService; // Servizio per la persistenza dell'archivio

    /**
     * @brief Comparatore case-insensitive per ordinare i libri per titolo.
     *
     * Se il titolo è null, viene considerata la stringa vuota.
     */
    private static final Comparator<Book> BY_TITLE_COMPARATOR =
            Comparator.comparing(
                    b -> b.getTitle() == null
                            ? ""
                            : b.getTitle().toLowerCase()
            );

    /**
     * @brief Costruttore del servizio.
     *
     * @param libraryArchiveService Servizio che fornisce accesso all'archivio e alla persistenza.
     *
     * @pre  libraryArchiveService != null
     * @post this.libraryArchiveService == libraryArchiveService
     *
     * @throws IllegalArgumentException Se libraryArchiveService è nullo.
     */
    public BookService(LibraryArchiveService libraryArchiveService) {
        if (libraryArchiveService == null) {
            throw new IllegalArgumentException("libraryArchiveService non può essere nullo");
        }
        this.libraryArchiveService = libraryArchiveService;
    }

    /**
     * @brief Restituisce l'archivio corrente gestito dal LibraryArchiveService.
     *
     * @return Istanza di LibraryArchive corrente (può dipendere dal ciclo di vita del servizio).
     */
    private LibraryArchive getArchive() {
        return libraryArchiveService.getLibraryArchive();
    }

    /**
     * @brief Registra un nuovo libro nel catalogo.
     *
     * - valida i campi obbligatori del libro;
     * - verifica formato ISBN;
     * - verifica unicità ISBN nell'archivio;
     * - aggiunge il libro all'archivio;
     * - persiste le modifiche.
     *
     * @param book Libro da aggiungere al catalogo.
     *
     * @pre  book != null
     * @pre  libraryArchiveService != null
     *
     * @post Il libro risulta presente nell'archivio (in assenza di eccezioni).
     *
     * @throws MandatoryFieldException Se uno o più campi obbligatori non sono validi.
     * @throws InvalidIsbnException    Se l'ISBN non rispetta il formato atteso o è già presente.
     * @throws RuntimeException        Se il salvataggio dell'archivio fallisce (wrapping di IOException).
     */
    public void addBook(Book book) throws MandatoryFieldException, InvalidIsbnException{
        validateBookMandatoryFields(book);
        validateIsbnFormat(book.getIsbn());
        validateIsbnUniquenessOnAdd(book.getIsbn());

        getArchive().addBook(book); // Aggiunge il libro all'archivio

        persistChanges(); // Persistenza delle modifiche
    }

    /**
     * @brief Aggiorna i dati di un libro esistente.
     *
     * - valida i campi obbligatori del libro;
     * - verifica formato ISBN;
     * - persiste l'archivio.
     *
     * @param book Libro con i dati aggiornati.
     *
     * @pre  book != null
     * @pre  libraryArchiveService != null
     *
     * @post Le modifiche risultano persistite (in assenza di eccezioni).
     *
     * @throws MandatoryFieldException Se i nuovi dati violano vincoli di obbligatorietà.
     * @throws InvalidIsbnException    Se l'ISBN non è valido rispetto al formato previsto.
     * @throws RuntimeException        Se il salvataggio dell'archivio fallisce (wrapping di IOException).
     */
    public void updateBook(Book book) throws MandatoryFieldException, InvalidIsbnException{
        validateBookMandatoryFields(book);
        validateIsbnFormat(book.getIsbn());

        persistChanges(); // Persistenza delle modifiche
    }

    /**
     * @brief Rimuove un libro dal catalogo.
     *
     * - verifica precondizioni;
     * - controlla eventuali prestiti attivi associati al libro;
     * - rimuove il libro dall'archivio;
     * - persiste l'archivio aggiornato.
     *
     * @param book Libro da rimuovere.
     *
     * @pre  book != null
     * @pre  libraryArchiveService != null
     *
     * @post Il libro non è più presente nell'archivio (in assenza di eccezioni).
     *
     * @throws IllegalArgumentException      Se book è nullo.
     * @throws UserHasActiveLoanException    Se esistono prestiti attivi associati al libro.
     * @throws RuntimeException              Se il salvataggio dell'archivio fallisce (wrapping di IOException).
     */
    public void removeBook(Book book) throws UserHasActiveLoanException{
        if (book == null) {
            throw new IllegalArgumentException("Il libro non può essere nullo");
        }

        List<Loan> loansForBook = getArchive().findLoansByBook(book);
        for (Loan loan : loansForBook) {
            if(loan != null && loan.isActive()){
                throw new UserHasActiveLoanException(
                        "Impossibile rimuovere il libro: sono presenti prestiti attivi associati."
                );
            }
        }

        getArchive().removeBook(book); // Rimozione Effettiva
        persistChanges(); // Persistenza delle modifiche
    }

    /**
     * @brief Restituisce tutti i libri ordinati per titolo (case-insensitive).
     *
     * L'ordinamento è effettuato su una copia della lista restituita dall'archivio.
     *
     * @pre  libraryArchiveService != null
     * @post true
     *
     * @return Lista di libri ordinata per titolo (mai null).
     */
    public List<Book> getBooksSortedByTitle() {
        List<Book> books = new ArrayList<>(getArchive().getBooks());
        books.sort(BY_TITLE_COMPARATOR);
        return books;
    }

    /**
     * @brief Restituisce tutti i libri ordinati per autore.
     *
     * Criterio: confronto case-insensitive sul primo autore della lista.
     * Se la lista autori è vuota, viene usata stringa vuota come chiave.
     *
     * @pre  libraryArchiveService != null
     * @post true
     *
     * @return Lista di libri ordinata per autore (mai null).
     */
    public List<Book> getBooksSortedByAuthor() {
        return getArchive().getBooks().stream()
                .sorted((b1, b2) -> {
                    String a1 = b1.getAuthors().isEmpty() ? "" : b1.getAuthors().get(0).toLowerCase();
                    String a2 = b2.getAuthors().isEmpty() ? "" : b2.getAuthors().get(0).toLowerCase();
                    return a1.compareTo(a2);
                })
                .toList();
    }

    /**
     * @brief Restituisce tutti i libri ordinati per anno di pubblicazione.
     *
     * @pre  libraryArchiveService != null
     * @post true
     *
     * @return Lista di libri ordinata per anno (mai null).
     */
    public List<Book> getBooksSortedByYear() {
        return getArchive().getBooks().stream()
                .sorted(Comparator.comparingInt(Book::getReleaseYear))
                .toList();
    }

    /**
     * @brief Ricerca libri nel catalogo tramite query testuale.
     *
     * La query viene normalizzata in lower-case e confrontata tramite
     * matching per sottostringa su:
     * - titolo;
     * - autori;
     * - ISBN.
     *
     * Se la query è vuota (dopo trim), restituisce l'intero catalogo ordinato per titolo.
     *
     * @param query Testo di ricerca inserito dall'operatore.
     *
     * @pre  query != null
     * @pre  libraryArchiveService != null
     *
     * @post true
     *
     * @return Lista dei libri che soddisfano il criterio di ricerca (mai null).
     *
     * @throws IllegalArgumentException Se query è null.
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
        for (Book book : getArchive().getBooks()) {
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
     * @param book Libro da validare.
     *
     * @pre  true
     * @post true
     *
     * @throws MandatoryFieldException Se qualche vincolo non è rispettato.
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

        // Anno di pubblicazione
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
     * @brief Valida il formato dell'ISBN.
     *
     * - ammette cifre, spazi e trattini;
     * - dopo normalizzazione (rimozione spazi e trattini) deve contenere solo cifre;
     * - lunghezza delle cifre normalizzate pari a 10 o 13.
     *
     * @param isbn ISBN da validare.
     *
     * @pre  isbn != null
     * @post true
     *
     * @throws InvalidIsbnException Se il formato non è valido secondo le regole adottate.
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
     * @pre  isbn != null
     * @post true
     *
     * @throws InvalidIsbnException Se esiste già un libro con lo stesso ISBN.
     */
    private void validateIsbnUniquenessOnAdd(String isbn) throws InvalidIsbnException {
        Book existing = getArchive().findBookByIsbn(isbn);
        if (existing != null) {
            throw new InvalidIsbnException("Esiste già un libro in catalogo con lo stesso ISBN.");
        }
    }

    /* ---------------------------------------------------------------------- */
    /*                Metodi di supporto e persistenza                         */
    /* ---------------------------------------------------------------------- */
    
    /**
     * @brief Persiste le modifiche dell'archivio tramite LibraryArchiveService.
     *
     * Converte eventuali IOException in RuntimeException, poiché la persistenza
     * fallita rappresenta un errore applicativo.
     *
     * @throws RuntimeException Se il salvataggio fallisce (wrapping di IOException).
     */
    private void persistChanges() {
        try {
            libraryArchiveService.saveArchive(getArchive());
        } catch (IOException e) {
            throw new RuntimeException("Errore durante il salvataggio dell'archivio dei libri.", e);
        }
    }

    /**
     * @brief Verifica se una stringa è nulla o composta solo da spazi.
     *
     * @param value Stringa da controllare.
     *
     * @return true se value è null o blank, false altrimenti.
     */
    private boolean isNullOrBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}