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

class BookServiceTest {

    /**
     * Fake in-memory: niente file.
     * Nota: estende ArchiveFileService solo per compatibilità col costruttore di LibraryArchiveService.
     */
    private static class InMemoryArchiveFileService extends ArchiveFileService {
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

    @BeforeEach
    void setUp() {
        // IMPORTANTISSIMO: NON usare new LibraryArchiveService(new LibraryArchive())
        // perché quello costruttore usa "library-archive.dat".
        archiveService = new LibraryArchiveService(new InMemoryArchiveFileService());
        bookService = new BookService(archiveService);
    }

    /* ===========================
            addBook
       =========================== */

    @Test
    @DisplayName("addBook: inserisce un libro valido")
    void addBookAddsBook() throws Exception {
        Book b = book("Clean Code", List.of("Robert C. Martin"), currentYear(), "1111111111", 2);

        bookService.addBook(b);

        assertEquals(1, archiveService.getLibraryArchive().getBooks().size());
        assertEquals(b, archiveService.getLibraryArchive().getBooks().get(0));
    }

    @Test
    @DisplayName("addBook: book null -> MandatoryFieldException")
    void addBookNullThrows() {
        assertThrows(MandatoryFieldException.class, () -> bookService.addBook(null));
    }

    @Test
    @DisplayName("addBook: titolo blank -> MandatoryFieldException")
    void addBookBlankTitleThrows() {
        Book b = book("   ", List.of("Autore"), currentYear(), "1111111111", 1);
        assertThrows(MandatoryFieldException.class, () -> bookService.addBook(b));
    }

    @Test
    @DisplayName("addBook: authors vuota -> MandatoryFieldException (NO null per evitare NPE nel costruttore Book)")
    void addBookEmptyAuthorsThrows() {
        Book b = book("Titolo", List.of(), currentYear(), "1111111111", 1);
        assertThrows(MandatoryFieldException.class, () -> bookService.addBook(b));
    }

    @Test
    @DisplayName("addBook: anno > anno corrente -> MandatoryFieldException")
    void addBookInvalidYearThrows() {
        Book b = book("Titolo", List.of("Autore"), currentYear() + 1, "1111111111", 1);
        assertThrows(MandatoryFieldException.class, () -> bookService.addBook(b));
    }

    @Test
    @DisplayName("addBook: ISBN non numerico (tolti spazi/trattini) -> InvalidIsbnException")
    void addBookInvalidIsbnFormatThrows() {
        Book b = book("Titolo", List.of("Autore"), currentYear(), "ABC-123", 1);
        assertThrows(InvalidIsbnException.class, () -> bookService.addBook(b));
    }

    @Test
    @DisplayName("addBook: ISBN lunghezza != 10 e != 13 -> InvalidIsbnException")
    void addBookIsbnWrongLengthThrows() {
        Book b = book("Titolo", List.of("Autore"), currentYear(), "12345", 1);
        assertThrows(InvalidIsbnException.class, () -> bookService.addBook(b));
    }

    @Test
    @DisplayName("addBook: ISBN duplicato -> InvalidIsbnException")
    void addBookDuplicateIsbnThrows() throws Exception {
        Book b1 = book("A", List.of("Autore"), currentYear(), "1111111111", 1);
        Book b2 = book("B", List.of("Autore"), currentYear(), "1111111111", 1);

        bookService.addBook(b1);
        assertThrows(InvalidIsbnException.class, () -> bookService.addBook(b2));
    }

    /* ===========================
            updateBook
       =========================== */

    @Test
    @DisplayName("updateBook: valido -> non lancia")
    void updateBookValidDoesNotThrow() {
        Book b = book("Titolo", List.of("Autore"), currentYear(), "1111111111", 1);
        assertDoesNotThrow(() -> bookService.updateBook(b));
    }

    @Test
    @DisplayName("updateBook: null -> MandatoryFieldException")
    void updateBookNullThrows() {
        assertThrows(MandatoryFieldException.class, () -> bookService.updateBook(null));
    }

    @Test
    @DisplayName("updateBook: ISBN non valido -> InvalidIsbnException")
    void updateBookInvalidIsbnThrows() {
        Book b = book("Titolo", List.of("Autore"), currentYear(), "12-AB", 1);
        assertThrows(InvalidIsbnException.class, () -> bookService.updateBook(b));
    }

    /* ===========================
            removeBook
       =========================== */

    @Test
    @DisplayName("removeBook: null -> IllegalArgumentException")
    void removeBookNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> bookService.removeBook(null));
    }

    @Test
    @DisplayName("removeBook: rimuove se non ci sono prestiti attivi")
    void removeBookRemovesWhenNoActiveLoans() throws Exception {
        Book b = book("Titolo", List.of("Autore"), currentYear(), "1111111111", 1);
        bookService.addBook(b);

        assertDoesNotThrow(() -> bookService.removeBook(b));
        assertTrue(archiveService.getLibraryArchive().getBooks().isEmpty());
    }

    @Test
    @DisplayName("removeBook: se ci sono prestiti attivi associati -> UserHasActiveLoanException")
    void removeBookThrowsIfHasActiveLoans() throws Exception {
        LibraryArchive a = archiveService.getLibraryArchive();

        Book b = book("Titolo", List.of("Autore"), currentYear(), "1111111111", 1);
        User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");

        a.addBook(b);
        a.addUser(u);

        // crea prestito attivo
        a.addLoan(u, b, LocalDate.now().plusDays(7));

        assertThrows(UserHasActiveLoanException.class, () -> bookService.removeBook(b));
    }

    /* ===========================
            searchBooks (Soluzione A)
       =========================== */

    @Test
    @DisplayName("searchBooks: match su titolo/autore/isbn (query non ambigue) - Soluzione A")
    void searchBooksMatchesTitleAuthorIsbn() throws Exception {
        Book b1 = book("Algoritmi", List.of("Cormen"), currentYear(), "9999999999999", 1);
        Book b2 = book("Reti di Calcolatori", List.of("Kurose"), currentYear(), "1234567890", 1);

        bookService.addBook(b1);
        bookService.addBook(b2);

        assertEquals(1, bookService.searchBooks("algorit").size());
        assertEquals(1, bookService.searchBooks("kuro").size());

        // ISBN NON ambiguo -> 1 risultato
        assertEquals(1, bookService.searchBooks("1234567890").size());
        assertEquals(b2, bookService.searchBooks("1234567890").get(0));
    }
}