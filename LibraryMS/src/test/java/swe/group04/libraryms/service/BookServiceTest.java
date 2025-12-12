package swe.group04.libraryms.service;

import org.junit.jupiter.api.*;
import swe.group04.libraryms.exceptions.InvalidIsbnException;
import swe.group04.libraryms.exceptions.MandatoryFieldException;
import swe.group04.libraryms.exceptions.UserHasActiveLoanException;
import swe.group04.libraryms.models.Book;
import swe.group04.libraryms.models.LibraryArchive;
import swe.group04.libraryms.models.Loan;
import swe.group04.libraryms.models.User;

import java.io.File;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookServiceTest {

    private static final String DEFAULT_ARCHIVE_FILE = "library-archive.dat";

    private LibraryArchive archive;
    private BookService bookService;

    @BeforeEach
    void setUp() {
        // Service singleton condivisi
        bookService = ServiceLocator.getBookService();
        archive = ServiceLocator.getArchiveService().getLibraryArchive();

        // Reset "pulito" dell'archivio condiviso (getX() ritorna copie -> devo rimuovere via metodi)
        clearArchiveCompletely();
    }

    @AfterEach
    void tearDown() {
        // pulizia file di persistenza usato dal costruttore LibraryArchiveService(LibraryArchive)
        File f = new File(DEFAULT_ARCHIVE_FILE);
        if (f.exists()) {
            // non assertare: su alcune macchine/CI potrebbe fallire per permessi/lock
            // ma la suite deve comunque completare
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        }
    }

    /* --------------------------------------------------------------------- */
    /*                                Helpers                                */
    /* --------------------------------------------------------------------- */

    private void clearArchiveCompletely() {
        // rimuovo prima i prestiti, poi libri, poi utenti (ordine "sicuro")
        for (Loan l : archive.getLoans()) {
            archive.removeLoan(l);
        }
        for (Book b : archive.getBooks()) {
            archive.removeBook(b);
        }
        for (User u : archive.getUsers()) {
            archive.removeUser(u);
        }
    }

    private Book mkBook(String title, String author, int year, String isbn, int copies) {
        return new Book(title, List.of(author), year, isbn, copies);
    }

    /* --------------------------------------------------------------------- */
    /*                              Constructor                               */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("Costruttore: IllegalArgumentException se LibraryArchiveService è null")
    void constructorThrowsIfArchiveServiceNull() {
        assertThrows(IllegalArgumentException.class, () -> new BookService(null));
    }

    /* --------------------------------------------------------------------- */
    /*                                 addBook                                */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("addBook: aggiunge un libro valido e lo rende trovabile per ISBN")
    void addBookAddsBookSuccessfully() throws Exception {
        Book b = mkBook("Clean Code", "Martin", 2008, "9780132350884", 2);

        bookService.addBook(b);

        assertEquals(1, archive.getBooks().size());
        assertSame(b, archive.findBookByIsbn("9780132350884"));
    }

    @Test
    @DisplayName("addBook: MandatoryFieldException se book è null")
    void addBookThrowsIfNullBook() {
        assertThrows(MandatoryFieldException.class, () -> bookService.addBook(null));
    }

    @Test
    @DisplayName("addBook: MandatoryFieldException se titolo è blank")
    void addBookThrowsIfBlankTitle() {
        Book b = mkBook("   ", "Martin", 2008, "9780132350884", 1);
        assertThrows(MandatoryFieldException.class, () -> bookService.addBook(b));
    }

    @Test
    @DisplayName("addBook: MandatoryFieldException se lista autori è vuota")
    void addBookThrowsIfEmptyAuthors() {
        Book b = new Book("Clean Code", List.of(), 2008, "9780132350884", 1);
        assertThrows(MandatoryFieldException.class, () -> bookService.addBook(b));
    }

    @Test
    @DisplayName("addBook: MandatoryFieldException se anno è nel futuro")
    void addBookThrowsIfYearInFuture() {
        int future = Year.now().getValue() + 1;
        Book b = mkBook("Future Book", "Author", future, "9780132350884", 1);
        assertThrows(MandatoryFieldException.class, () -> bookService.addBook(b));
    }

    @Test
    @DisplayName("addBook: InvalidIsbnException se ISBN contiene caratteri non numerici (oltre a spazi/trattini)")
    void addBookThrowsIfIsbnHasLetters() {
        Book b = mkBook("Clean Code", "Martin", 2008, "9780A32350884", 1);
        assertThrows(InvalidIsbnException.class, () -> bookService.addBook(b));
    }

    @Test
    @DisplayName("addBook: InvalidIsbnException se ISBN non ha 10 o 13 cifre (ignorando spazi/trattini)")
    void addBookThrowsIfIsbnWrongLength() {
        Book b = mkBook("Clean Code", "Martin", 2008, "123456789", 1); // 9 cifre
        assertThrows(InvalidIsbnException.class, () -> bookService.addBook(b));
    }

    @Test
    @DisplayName("addBook: InvalidIsbnException se ISBN già presente")
    void addBookThrowsIfDuplicateIsbn() throws Exception {
        Book b1 = mkBook("Clean Code", "Martin", 2008, "9780132350884", 1);
        Book b2 = mkBook("Clean Code 2", "Martin", 2009, "9780132350884", 1);

        bookService.addBook(b1);
        assertThrows(InvalidIsbnException.class, () -> bookService.addBook(b2));
    }

    /* --------------------------------------------------------------------- */
    /*                               updateBook                               */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("updateBook: non lancia con libro valido (persistenza inclusa)")
    void updateBookValidDoesNotThrow() throws Exception {
        Book b = mkBook("Refactoring", "Fowler", 1999, "9780201485677", 1);
        bookService.addBook(b);

        // modifico l'oggetto (è lo stesso presente in archivio perché l'archivio contiene quel riferimento)
        b.setTitle("Refactoring (2nd)");
        assertDoesNotThrow(() -> bookService.updateBook(b));

        // l'oggetto è lo stesso, quindi il cambiamento è visibile
        assertEquals("Refactoring (2nd)", archive.findBookByIsbn("9780201485677").getTitle());
    }

    @Test
    @DisplayName("updateBook: MandatoryFieldException se libro è null")
    void updateBookThrowsIfNull() {
        assertThrows(MandatoryFieldException.class, () -> bookService.updateBook(null));
    }

    @Test
    @DisplayName("updateBook: InvalidIsbnException se ISBN non valido")
    void updateBookThrowsIfInvalidIsbn() {
        Book b = mkBook("X", "Y", 2000, "12-34", 1); // 4 cifre
        assertThrows(InvalidIsbnException.class, () -> bookService.updateBook(b));
    }

    /* --------------------------------------------------------------------- */
    /*                               removeBook                               */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("removeBook: IllegalArgumentException se book è null")
    void removeBookThrowsIfNull() {
        assertThrows(IllegalArgumentException.class, () -> bookService.removeBook(null));
    }

    @Test
    @DisplayName("removeBook: rimuove il libro se non ci sono prestiti attivi associati")
    void removeBookRemovesIfNoActiveLoans() throws Exception {
        Book b = mkBook("Algoritmi", "Cormen", 2009, "1234567890123", 1);
        bookService.addBook(b);

        assertDoesNotThrow(() -> bookService.removeBook(b));
        assertNull(archive.findBookByIsbn("1234567890123"));
        assertTrue(archive.getBooks().isEmpty());
    }

    @Test
    @DisplayName("removeBook: UserHasActiveLoanException se esiste un prestito attivo sul libro")
    void removeBookThrowsIfActiveLoanExists() throws Exception {
        Book b = mkBook("Reti di Calcolatori", "Kurose", 2020, "1234567890", 1);
        bookService.addBook(b);

        User u = new User("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        archive.addUser(u);

        // Creo un prestito attivo direttamente nell'archivio (LoanService non serve qui)
        archive.addLoan(u, b, LocalDate.now().plusDays(7));

        assertThrows(UserHasActiveLoanException.class, () -> bookService.removeBook(b));
        assertNotNull(archive.findBookByIsbn("1234567890")); // non deve essere stato rimosso
    }

    /* --------------------------------------------------------------------- */
    /*                          getBooksSortedBy...                           */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("getBooksSortedByTitle: ordina per titolo case-insensitive")
    void getBooksSortedByTitleSortsCaseInsensitive() throws Exception {
        Book b1 = mkBook("zeta", "A", 2000, "1111111111", 1);
        Book b2 = mkBook("Alpha", "A", 2000, "2222222222", 1);
        Book b3 = mkBook("beta", "A", 2000, "3333333333", 1);

        bookService.addBook(b1);
        bookService.addBook(b2);
        bookService.addBook(b3);

        List<Book> sorted = bookService.getBooksSortedByTitle();
        assertEquals(List.of(b2, b3, b1), sorted);
    }

    @Test
    @DisplayName("getBooksSortedByAuthor: ordina per primo autore case-insensitive")
    void getBooksSortedByAuthorSortsByFirstAuthor() throws Exception {
        Book b1 = mkBook("T1", "Zed", 2000, "1111111111", 1);
        Book b2 = mkBook("T2", "alpha", 2000, "2222222222", 1);
        Book b3 = mkBook("T3", "Beta", 2000, "3333333333", 1);

        bookService.addBook(b1);
        bookService.addBook(b2);
        bookService.addBook(b3);

        List<Book> sorted = bookService.getBooksSortedByAuthor();
        assertEquals(List.of(b2, b3, b1), sorted);
    }

    @Test
    @DisplayName("getBooksSortedByYear: ordina per anno crescente")
    void getBooksSortedByYearSortsAscending() throws Exception {
        Book b1 = mkBook("T1", "A", 2020, "1111111111", 1);
        Book b2 = mkBook("T2", "A", 1999, "2222222222", 1);
        Book b3 = mkBook("T3", "A", 2010, "3333333333", 1);

        bookService.addBook(b1);
        bookService.addBook(b2);
        bookService.addBook(b3);

        List<Book> sorted = bookService.getBooksSortedByYear();
        assertEquals(List.of(b2, b3, b1), sorted);
    }

    /* --------------------------------------------------------------------- */
    /*                               searchBooks                              */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("searchBooks: IllegalArgumentException se query è null")
    void searchBooksThrowsIfNullQuery() {
        assertThrows(IllegalArgumentException.class, () -> bookService.searchBooks(null));
    }

    @Test
    @DisplayName("searchBooks: query vuota => restituisce tutti i libri ordinati per titolo")
    void searchBooksEmptyReturnsAllSortedByTitle() throws Exception {
        Book b1 = mkBook("zeta", "A", 2000, "1111111111", 1);
        Book b2 = mkBook("Alpha", "A", 2000, "2222222222", 1);
        Book b3 = mkBook("beta", "A", 2000, "3333333333", 1);

        bookService.addBook(b1);
        bookService.addBook(b2);
        bookService.addBook(b3);

        List<Book> res = bookService.searchBooks("   ");
        assertEquals(List.of(b2, b3, b1), res);
    }

    @Test
    @DisplayName("searchBooks: match su titolo (case-insensitive)")
    void searchBooksMatchesTitle() throws Exception {
        Book b1 = mkBook("Reti di Calcolatori", "Kurose", 2020, "1234567890", 1);
        Book b2 = mkBook("Algoritmi", "Cormen", 2009, "1234567890123", 1);

        bookService.addBook(b1);
        bookService.addBook(b2);

        List<Book> res = bookService.searchBooks("reti");
        assertEquals(List.of(b1), res);
    }

    @Test
    @DisplayName("searchBooks: match su autore (case-insensitive)")
    void searchBooksMatchesAuthor() throws Exception {
        Book b1 = mkBook("Reti di Calcolatori", "Kurose", 2020, "1234567890", 1);
        Book b2 = mkBook("Algoritmi", "Cormen", 2009, "1234567890123", 1);

        bookService.addBook(b1);
        bookService.addBook(b2);

        List<Book> res = bookService.searchBooks("kurose");
        assertEquals(List.of(b1), res);
    }

    @Test
    @DisplayName("searchBooks: match su ISBN (case-insensitive) e ritorna lista ordinata per titolo")
    void searchBooksMatchesIsbnAndSortsByTitle() throws Exception {
        Book b1 = mkBook("Reti di Calcolatori", "Kurose", 2020, "1234567890", 1);
        Book b2 = mkBook("Algoritmi", "Cormen", 2009, "1234567890123", 1);

        bookService.addBook(b1);
        bookService.addBook(b2);

        // query = ISBN completo del secondo libro -> match UNIVOCO (evita falsi positivi come nel tuo errore)
        List<Book> res = bookService.searchBooks("1234567890123");
        assertEquals(List.of(b2), res);
    }

    @Test
    @DisplayName("searchBooks: se più libri matchano, il risultato è ordinato per titolo")
    void searchBooksMultipleMatchesAreSortedByTitle() throws Exception {
        Book b1 = mkBook("Zeta Networks", "Kurose", 2020, "1111111111", 1);
        Book b2 = mkBook("Alpha Networks", "Kurose", 2020, "2222222222", 1);
        Book b3 = mkBook("Beta", "Other", 2020, "3333333333", 1);

        bookService.addBook(b1);
        bookService.addBook(b2);
        bookService.addBook(b3);

        List<Book> res = bookService.searchBooks("networks"); // match b1 e b2 sul titolo
        assertEquals(List.of(b2, b1), res); // Alpha..., poi Zeta...
    }
}