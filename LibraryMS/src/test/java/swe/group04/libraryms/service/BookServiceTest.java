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

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookServiceTest {

    private LibraryArchive archive;
    private LibraryArchiveService archiveService;
    private BookService bookService;

    // -------------------------- Utility fixture --------------------------

    private Book createValidBook(String isbn) {
        return new Book(
                "Clean Code",
                List.of("Robert C. Martin"),
                2008,
                isbn,
                3
        );
    }

    @BeforeEach
    void setUp() {
        archive = new LibraryArchive();

        // Replica lo stesso schema di ArchiveFileServiceTest
        FileService fileService = new FileService();
        String testFilePath = "bookServiceTestArchive.bin";
        ArchiveFileService afs = new ArchiveFileService(testFilePath, fileService);

        archiveService = new LibraryArchiveService(afs);

        bookService = new BookService(archive, archiveService);
    }

    // -------------------------- Costruttore ------------------------------

    @Test
    @DisplayName("Il costruttore lancia IllegalArgumentException se libraryArchive è null")
    void constructorThrowsIfLibraryArchiveIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new BookService(null, archiveService));
    }

    @Test
    @DisplayName("Il costruttore lancia IllegalArgumentException se libraryArchiveService è null")
    void constructorThrowsIfLibraryArchiveServiceIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new BookService(archive, null));
    }

    @Test
    @DisplayName("Il costruttore accetta parametri non null senza lanciare eccezioni")
    void constructorAcceptsValidParameters() {
        assertDoesNotThrow(() -> new BookService(new LibraryArchive(), archiveService));
    }

    // -------------------------- addBook: caso base -----------------------

    @Test
    @DisplayName("addBook con libro valido aggiunge all'archivio")
    void addBookValidAddsToArchive() throws Exception {
        Book book = createValidBook("9780132350884");

        bookService.addBook(book);

        List<Book> books = archive.getBooks();
        assertEquals(1, books.size());
        assertEquals(book, books.get(0));
    }

    // -------------------------- addBook: campi obbligatori ---------------

    @Test
    @DisplayName("addBook con libro null lancia MandatoryFieldException")
    void addBookNullThrowsMandatoryFieldException() {
        assertThrows(MandatoryFieldException.class,
                () -> bookService.addBook(null));

        assertTrue(archive.getBooks().isEmpty());
    }

    @Test
    @DisplayName("addBook: titolo obbligatorio")
    void addBookTitleMandatory() {
        Book book = createValidBook("9780132350884");
        book.setTitle("   "); // titolo invalido

        assertThrows(MandatoryFieldException.class,
                () -> bookService.addBook(book));

        assertTrue(archive.getBooks().isEmpty());
    }

    @Test
    @DisplayName("addBook: almeno un autore è obbligatorio")
    void addBookAuthorsMandatory() {
        Book book = createValidBook("9780132350884");
        book.setAuthors(List.of()); // nessun autore

        assertThrows(MandatoryFieldException.class,
                () -> bookService.addBook(book));

        assertTrue(archive.getBooks().isEmpty());
    }

    @Test
    @DisplayName("addBook: anno di pubblicazione deve essere > 0 e non nel futuro")
    void addBookReleaseYearMustBeValid() {
        Book book = createValidBook("9780132350884");
        book.setReleaseYear(-1);

        assertThrows(MandatoryFieldException.class,
                () -> bookService.addBook(book));

        assertTrue(archive.getBooks().isEmpty());
    }

    @Test
    @DisplayName("addBook: numero totale di copie deve essere > 0")
    void addBookTotalCopiesMustBePositive() {
        Book book = createValidBook("9780132350884");
        book.setTotalCopies(0);

        assertThrows(MandatoryFieldException.class,
                () -> bookService.addBook(book));

        assertTrue(archive.getBooks().isEmpty());
    }

    @Test
    @DisplayName("addBook: copie disponibili devono essere tra 0 e totale")
    void addBookAvailableCopiesWithinBounds() {
        Book book = createValidBook("9780132350884");
        book.setAvailableCopies(book.getTotalCopies() + 1); // > total

        assertThrows(MandatoryFieldException.class,
                () -> bookService.addBook(book));

        assertTrue(archive.getBooks().isEmpty());
    }

    // -------------------------- addBook: ISBN -----------------------------

    @Test
    @DisplayName("addBook: ISBN obbligatorio (non null, non blanco)")
    void addBookIsbnMandatory() {
        Book book = createValidBook("   "); // ISBN bianco

        assertThrows(MandatoryFieldException.class,
                () -> bookService.addBook(book));

        assertTrue(archive.getBooks().isEmpty());
    }

    @Test
    @DisplayName("addBook: ISBN deve contenere solo cifre (spazi e trattini ammessi)")
    void addBookInvalidIsbnCharacters() {
        Book book = createValidBook("ABC-123-XYZ");

        assertThrows(InvalidIsbnException.class,
                () -> bookService.addBook(book));

        assertTrue(archive.getBooks().isEmpty());
    }

    @Test
    @DisplayName("addBook: ISBN deve avere 10 o 13 cifre")
    void addBookInvalidIsbnLength() {
        Book book = createValidBook("12345");

        assertThrows(InvalidIsbnException.class,
                () -> bookService.addBook(book));

        assertTrue(archive.getBooks().isEmpty());
    }

    @Test
    @DisplayName("addBook: ISBN duplicato -> InvalidIsbnException")
    void addBookDuplicateIsbnThrows() throws Exception {
        // Primo libro già in archivio
        Book book1 = createValidBook("9780132350884");
        archive.addBook(book1);

        // Secondo libro con stesso ISBN
        Book book2 = createValidBook("9780132350884");

        assertThrows(InvalidIsbnException.class,
                () -> bookService.addBook(book2));

        assertEquals(1, archive.getBooks().size());
    }

    // -------------------------- updateBook --------------------------------

    @Test
    @DisplayName("updateBook con libro valido non lancia eccezioni")
    void updateBookValidDoesNotThrow() throws Exception {
        Book book = createValidBook("9780132350884");
        archive.addBook(book);

        assertDoesNotThrow(() -> bookService.updateBook(book));
    }

    @Test
    @DisplayName("updateBook: libro nullo -> MandatoryFieldException")
    void updateBookNullThrowsMandatoryFieldException() {
        assertThrows(MandatoryFieldException.class,
                () -> bookService.updateBook(null));
    }

    @Test
    @DisplayName("updateBook: titolo obbligatorio")
    void updateBookTitleMandatory() {
        Book book = createValidBook("9780132350884");
        book.setTitle("  ");

        assertThrows(MandatoryFieldException.class,
                () -> bookService.updateBook(book));
    }

    // -------------------------- removeBook --------------------------------

    @Test
    @DisplayName("removeBook: libro null -> IllegalArgumentException")
    void removeBookNullThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> bookService.removeBook(null));
    }

    @Test
    @DisplayName("removeBook: presenza di prestiti attivi -> UserHasActiveLoanException")
    void removeBookWithActiveLoansThrowsUserHasActiveLoanException() throws Exception {
        Book book = createValidBook("9780132350884");
        archive.addBook(book);

        User user = new User("Mario", "Rossi", "mario.rossi@example.com", "S123456");
        archive.addUser(user);

        LocalDate loanDate = LocalDate.of(2025, 1, 10);
        // Usa la firma reale di LibraryArchive.addLoan(User, Book, LocalDate)
        archive.addLoan(user, book, loanDate);

        assertThrows(UserHasActiveLoanException.class,
                () -> bookService.removeBook(book));

        // Il libro deve rimanere in archivio
        assertTrue(archive.getBooks().contains(book));
    }

    @Test
    @DisplayName("removeBook: nessun prestito attivo -> libro rimosso")
    void removeBookNoActiveLoansRemovesBook() throws Exception {
        Book book = createValidBook("9780132350884");
        archive.addBook(book);

        assertTrue(archive.getLoans().isEmpty());

        bookService.removeBook(book);

        assertFalse(archive.getBooks().contains(book));
    }

    // -------------------------- getBooksSortedByTitle ---------------------

    @Test
    @DisplayName("getBooksSortedByTitle ordina per titolo in modo case-insensitive")
    void getBooksSortedByTitleSortsCaseInsensitive() {
        Book b1 = new Book("Zeta", List.of("A"), 2000, "1111111111", 1);
        Book b2 = new Book("alpha", List.of("B"), 2000, "2222222222", 1);
        Book b3 = new Book("Beta", List.of("C"), 2000, "3333333333", 1);

        archive.addBook(b1);
        archive.addBook(b2);
        archive.addBook(b3);

        List<Book> sorted = bookService.getBooksSortedByTitle();

        assertEquals(List.of(b2, b3, b1), sorted);
    }

    @Test
    @DisplayName("getBooksSortedByTitle: titoli null trattati come stringa vuota")
    void getBooksSortedByTitleHandlesNullTitles() {
        Book b1 = new Book("Zeta", List.of("A"), 2000, "1111111111", 1);
        Book b2 = new Book("alpha", List.of("B"), 2000, "2222222222", 1);
        Book b3 = new Book("X", List.of("C"), 2000, "3333333333", 1);
        b3.setTitle(null); // titolo null

        archive.addBook(b1);
        archive.addBook(b2);
        archive.addBook(b3);

        List<Book> sorted = bookService.getBooksSortedByTitle();

        // b3 (titolo null -> "") viene per primo
        assertEquals(b3, sorted.get(0));
    }

    // -------------------------- searchBooks -------------------------------

    @Test
    @DisplayName("searchBooks: query null -> IllegalArgumentException")
    void searchBooksNullQueryThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> bookService.searchBooks(null));
    }

    @Test
    @DisplayName("searchBooks: query vuota -> restituisce tutti i libri ordinati")
    void searchBooksBlankQueryReturnsAllSorted() {
        Book b1 = new Book("Zeta", List.of("A"), 2000, "1111111111", 1);
        Book b2 = new Book("alpha", List.of("B"), 2000, "2222222222", 1);
        archive.addBook(b1);
        archive.addBook(b2);

        List<Book> expected = bookService.getBooksSortedByTitle();
        List<Book> result   = bookService.searchBooks("   ");

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("searchBooks: match su titolo (case-insensitive, substring)")
    void searchBooksMatchesTitle() {
        Book b1 = createValidBook("9780132350884"); // "Clean Code"
        Book b2 = new Book("Refactoring", List.of("Martin Fowler"), 1999, "9780201485677", 2);
        archive.addBook(b1);
        archive.addBook(b2);

        List<Book> result = bookService.searchBooks("clean");

        assertEquals(1, result.size());
        assertEquals(b1, result.get(0));
    }

    @Test
    @DisplayName("searchBooks: match su autore (case-insensitive, substring)")
    void searchBooksMatchesAuthor() {
        Book b1 = new Book("Clean Code", List.of("Robert C. Martin"), 2008, "9780132350884", 3);
        Book b2 = new Book("Refactoring", List.of("Martin Fowler"), 1999, "9780201485677", 2);
        Book b3 = new Book("Design Patterns", List.of("Erich Gamma"), 1994, "9780201633610", 2);

        archive.addBook(b1);
        archive.addBook(b2);
        archive.addBook(b3);

        List<Book> result = bookService.searchBooks("martin");

        // Match su "Robert C. Martin" e "Martin Fowler"
        assertEquals(2, result.size());
        assertTrue(result.contains(b1));
        assertTrue(result.contains(b2));
    }

    @Test
    @DisplayName("searchBooks: match su ISBN (case-insensitive, substring)")
    void searchBooksMatchesIsbn() {
        Book b1 = createValidBook("978-0132350884");
        Book b2 = new Book("Refactoring", List.of("Martin Fowler"), 1999, "978-0201485677", 2);
        archive.addBook(b1);
        archive.addBook(b2);

        List<Book> result = bookService.searchBooks("0201485677");

        assertEquals(1, result.size());
        assertEquals(b2, result.get(0));
    }
}