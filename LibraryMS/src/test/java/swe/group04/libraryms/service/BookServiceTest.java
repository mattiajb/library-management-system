/**
 * @file BookServiceTest.java
 * @ingroup TestsService
 * @brief Suite di test di unità per il servizio applicativo BookService.
 *
 * Verifica:
 * - addBook: validazione dei campi obbligatori e dei vincoli sull’ISBN (formato e unicità);
 * - updateBook: validazione minima e gestione degli errori su input non valido;
 * - removeBook: rimozione corretta e blocco della rimozione in presenza di prestiti attivi;
 * - searchBooks: ricerca per corrispondenza su titolo/autore/ISBN.
 *
 * @note Per evitare accesso a file reali, viene utilizzata una implementazione fake
 *       in-memory di ArchiveFileService, compatibile con LibraryArchiveService.
 */
package swe.group04.libraryms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import swe.group04.libraryms.exceptions.InvalidIsbnException;
import swe.group04.libraryms.exceptions.MandatoryFieldException;
import swe.group04.libraryms.exceptions.UserHasActiveLoanException;
import swe.group04.libraryms.models.Book;
import swe.group04.libraryms.models.LibraryArchive;
import swe.group04.libraryms.models.User;
import swe.group04.libraryms.persistence.ArchiveFileService;
import swe.group04.libraryms.persistence.FileService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @brief Test di unità per BookService
 *
 * -logica di business relativa ai libri, esercitando i metodi
 *  esposti da BookService su un archivio isolato dalla persistenza reale.
 * 
 * @ingroup TestsService
 */
class BookServiceTest {

    /**
     * @brief Fake di persistenza in-memory.
     * 
     * Estende ArchiveFileService solo per compatibilità col costruttore di LibraryArchiveService.
     */
    private static class InMemoryArchiveFileService extends ArchiveFileService {
        
        //  Riferimento all'archivio memorizzato (può essere null)
        private LibraryArchive stored;

        InMemoryArchiveFileService() {
            super("IGNORED.bin", new FileService());
        }

        @Override
        public LibraryArchive loadArchive() throws IOException {
            return stored;
        }

        @Override
        public void saveArchive(LibraryArchive archive) throws IOException {
            stored = archive;
        }
    }

    private LibraryArchiveService archiveService;
    private BookService bookService;

    private static int currentYear() {
        return Year.now().getValue();
    }

    private static Book book(String title, List<String> authors, int year, String isbn, int totalCopies) {
        return new Book(title, authors, year, isbn, totalCopies);
    }

     /**
     * @brief Inizializza il contesto di test prima di ogni caso di prova.
     *
     * Prepara un LibraryArchiveService che usa un ArchiveFileService fittizio in-memory,
     * così nessun test accede a file reali.
     */
    @BeforeEach
    void setUp() {
        //  N.B.: NON viene usato new LibraryArchiveService(new LibraryArchive())
        //        --> perché usa "library-archive.dat".
        archiveService = new LibraryArchiveService(new InMemoryArchiveFileService());
        bookService = new BookService(archiveService);
    }

    /* ======================================================
                                addBook
       ====================================================== */

    /**
     * @brief Verifica che addBook inserisca correttamente un libro valido in archivio.
     *
     * Si aggiunge un libro con campi validi e si controlla che l'archivio contenga 1 elemento uguale.
     *
     * @throws Exception per propagare eventuali eccezioni inattese del caso di prova.
     */
    @Test
    @DisplayName("addBook: inserisce un libro valido")
    void addBookAddsBook() throws Exception {
        Book b = book("Clean Code", List.of("Robert C. Martin"), currentYear(), "1111111111", 2);

        bookService.addBook(b);

        assertEquals(1, archiveService.getLibraryArchive().getBooks().size());
        assertEquals(b, archiveService.getLibraryArchive().getBooks().get(0));
    }

    /**
     * @brief Verifica che addBook lanci MandatoryFieldException quando il parametro book è null.
     *
     * Chiamata addBook(null) deve produrre MandatoryFieldException.
     */
    @Test
    @DisplayName("addBook: book null -> MandatoryFieldException")
    void addBookNullThrows() {
        assertThrows(MandatoryFieldException.class, () -> bookService.addBook(null));
    }

    /**
     * @brief Verifica che addBook lanci MandatoryFieldException quando il titolo è blank.
     *
     * Libro con titolo composto solo da spazi deve essere rifiutato.
     */
    @Test
    @DisplayName("addBook: titolo blank -> MandatoryFieldException")
    void addBookBlankTitleThrows() {
        Book b = book("   ", List.of("Autore"), currentYear(), "1111111111", 1);
        assertThrows(MandatoryFieldException.class, () -> bookService.addBook(b));
    }

    
    /**
     * @brief Verifica che addBook lanci MandatoryFieldException quando la lista autori è vuota.
     *
     * La lista degli autori vuota è considerata violazione dei campi obbligatori.
     *
     * @note Il test usa List.of() e non null per evitare comportamenti non definiti lato costruttore Book.
     */
    @Test
    @DisplayName("addBook: authors vuota -> MandatoryFieldException (NO null per evitare NPE nel costruttore Book)")
    void addBookEmptyAuthorsThrows() {
        Book b = book("Titolo", List.of(), currentYear(), "1111111111", 1);
        assertThrows(MandatoryFieldException.class, () -> bookService.addBook(b));
    }

    /**
     * @brief Verifica che addBook lanci MandatoryFieldException quando l'anno è nel futuro.
     */
    @Test
    @DisplayName("addBook: anno > anno corrente -> MandatoryFieldException")
    void addBookInvalidYearThrows() {
        Book b = book("Titolo", List.of("Autore"), currentYear() + 1, "1111111111", 1);
        assertThrows(MandatoryFieldException.class, () -> bookService.addBook(b));
    }

    /**
     * @brief Verifica che addBook lanci InvalidIsbnException su ISBN non numerico (al netto di spazi/trattini).
     */
    @Test
    @DisplayName("addBook: ISBN non numerico (tolti spazi/trattini) -> InvalidIsbnException")
    void addBookInvalidIsbnFormatThrows() {
        Book b = book("Titolo", List.of("Autore"), currentYear(), "ABC-123", 1);
        assertThrows(InvalidIsbnException.class, () -> bookService.addBook(b));
    }

    /**
     * @brief Verifica che addBook lanci InvalidIsbnException se la lunghezza dell'ISBN non è 10 o 13 cifre.
     */
    @Test
    @DisplayName("addBook: ISBN lunghezza != 10 e != 13 -> InvalidIsbnException")
    void addBookIsbnWrongLengthThrows() {
        Book b = book("Titolo", List.of("Autore"), currentYear(), "12345", 1);
        assertThrows(InvalidIsbnException.class, () -> bookService.addBook(b));
    }

      /**
     * @brief Verifica che addBook lanci InvalidIsbnException se si tenta di inserire un ISBN già presente.
     *
     * L'inserimento del primo libro è ok; l'inserimento di un secondo libro con stesso ISBN deve fallire.
     */
    @Test
    @DisplayName("addBook: ISBN duplicato -> InvalidIsbnException")
    void addBookDuplicateIsbnThrows() throws Exception {
        Book b1 = book("A", List.of("Autore"), currentYear(), "1111111111", 1);
        Book b2 = book("B", List.of("Autore"), currentYear(), "1111111111", 1);

        bookService.addBook(b1);
        assertThrows(InvalidIsbnException.class, () -> bookService.addBook(b2));
    }

    /* ======================================================
                            updateBook
       ====================================================== */

    /**
     * @brief Verifica che updateBook non lanci eccezioni con dati validi.
     *
     * L'aggiornamento con campi obbligatori presenti e ISBN valido deve essere accettato.
     */
    @Test
    @DisplayName("updateBook: valido -> non lancia")
    void updateBookValidDoesNotThrow() {
        Book b = book("Titolo", List.of("Autore"), currentYear(), "1111111111", 1);
        assertDoesNotThrow(() -> bookService.updateBook(b));
    }

    /**
    * @brief Verifica che updateBook lanci MandatoryFieldException quando book è null.
    */
    @Test
    @DisplayName("updateBook: null -> MandatoryFieldException")
    void updateBookNullThrows() {
        assertThrows(MandatoryFieldException.class, () -> bookService.updateBook(null));
    }

     /**
     * @brief Verifica che updateBook lanci InvalidIsbnException con ISBN non valido.
     */
    @Test
    @DisplayName("updateBook: ISBN non valido -> InvalidIsbnException")
    void updateBookInvalidIsbnThrows() {
        Book b = book("Titolo", List.of("Autore"), currentYear(), "12-AB", 1);
        assertThrows(InvalidIsbnException.class, () -> bookService.updateBook(b));
    }

    /* ======================================================
                            removeBook
       ====================================================== */

    /**
    * @brief Verifica che removeBook lanci IllegalArgumentException quando il parametro book è null.
    */
    @Test
    @DisplayName("removeBook: null -> IllegalArgumentException")
    void removeBookNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> bookService.removeBook(null));
    }

    /**
    * @brief Verifica che removeBook rimuova un libro se non esistono prestiti attivi associati.
    *
    * Inserisce un libro, lo rimuove e verifica che l'archivio sia vuoto.
    */
    @Test
    @DisplayName("removeBook: rimuove se non ci sono prestiti attivi")
    void removeBookRemovesWhenNoActiveLoans() throws Exception {
        Book b = book("Titolo", List.of("Autore"), currentYear(), "1111111111", 1);
        bookService.addBook(b);

        assertDoesNotThrow(() -> bookService.removeBook(b));
        assertTrue(archiveService.getLibraryArchive().getBooks().isEmpty());
    }

    /**
    * @brief Verifica che removeBook lanci UserHasActiveLoanException se esistono prestiti attivi associati al libro.
    *
    * Crea un prestito attivo nell'archivio per lo stesso libro e verifica che la rimozione fallisca.
    */
    @Test
    @DisplayName("removeBook: se ci sono prestiti attivi associati -> UserHasActiveLoanException")
    void removeBookThrowsIfHasActiveLoans() throws Exception {
        LibraryArchive a = archiveService.getLibraryArchive();

        Book b = book("Titolo", List.of("Autore"), currentYear(), "1111111111", 1);
        User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");

        a.addBook(b);
        a.addUser(u);

        //  crea prestito attivo
        a.addLoan(u, b, LocalDate.now().plusDays(7));

        assertThrows(UserHasActiveLoanException.class, () -> bookService.removeBook(b));
    }

    /* ======================================================
                    searchBooks (Soluzione A)
       ====================================================== */

     /**
     * @brief Verifica che searchBooks trovi corrispondenze su titolo, autore e ISBN.
     *
     * Inserisce due libri e verifica:
     * - match parziale sul titolo (case-insensitive);
     * - match parziale su autore (case-insensitive);
     * - match su ISBN non ambiguo con 1 risultato atteso.
     */
    @Test
    @DisplayName("searchBooks: match su titolo/autore/isbn (query non ambigue) - Soluzione A")
    void searchBooksMatchesTitleAuthorIsbn() throws Exception {
        Book b1 = book("Algoritmi", List.of("Cormen"), currentYear(), "9999999999999", 1);
        Book b2 = book("Reti di Calcolatori", List.of("Kurose"), currentYear(), "1234567890", 1);

        bookService.addBook(b1);
        bookService.addBook(b2);

        assertEquals(1, bookService.searchBooks("algorit").size());
        assertEquals(1, bookService.searchBooks("kuro").size());

        //  ISBN NON ambiguo -> 1 risultato
        assertEquals(1, bookService.searchBooks("1234567890").size());
        assertEquals(b2, bookService.searchBooks("1234567890").get(0));
    }
}