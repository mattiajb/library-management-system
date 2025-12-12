package swe.group04.libraryms.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import swe.group04.libraryms.models.Book;
import swe.group04.libraryms.models.LibraryArchive;
import swe.group04.libraryms.models.Loan;
import swe.group04.libraryms.models.User;
import swe.group04.libraryms.persistence.ArchiveFileService;
import swe.group04.libraryms.persistence.FileService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LibraryArchiveServiceTest {

    private FileService fileService;
    private ArchiveFileService archiveFileService;
    private LibraryArchiveService libraryArchiveService;
    private String testFilePath;

    @BeforeEach
    void setUp() {
        fileService = new FileService();
        testFilePath = "libraryArchiveServiceTest.bin";

        // pulizia preventiva del file di test
        File f = new File(testFilePath);
        if (f.exists()) {
            assertTrue(f.delete());
        }

        archiveFileService = new ArchiveFileService(testFilePath, fileService);
        libraryArchiveService = new LibraryArchiveService(archiveFileService);
    }

    @AfterEach
    void tearDown() {
        File f = new File(testFilePath);
        if (f.exists()) {
            assertTrue(f.delete());
        }
    }

    // -------------------------------------------------------------
    // Costruttori
    // -------------------------------------------------------------

    @Test
    @DisplayName("Il costruttore (ArchiveFileService) lancia IllegalArgumentException se archiveFileService è null")
    void constructorArchiveFileServiceThrowsIfNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new LibraryArchiveService((ArchiveFileService) null)); // cast per evitare ambiguità
    }

    @Test
    @DisplayName("Il costruttore (LibraryArchive) lancia IllegalArgumentException se libraryArchive è null")
    void constructorLibraryArchiveThrowsIfNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new LibraryArchiveService((LibraryArchive) null)); // cast per evitare ambiguità
    }

    @Test
    @DisplayName("Il costruttore (LibraryArchive) imposta l'archivio interno e getLibraryArchive lo restituisce")
    void constructorLibraryArchiveSetsInternalArchive() {
        LibraryArchive shared = new LibraryArchive();
        LibraryArchiveService s = new LibraryArchiveService(shared);

        assertSame(shared, s.getLibraryArchive());
    }

    @Test
    @DisplayName("getLibraryArchive inizializza un archivio vuoto se non esiste ancora (ctor con ArchiveFileService)")
    void getLibraryArchiveInitializesArchiveIfNull() {
        LibraryArchive archive1 = libraryArchiveService.getLibraryArchive();

        assertNotNull(archive1);
        assertTrue(archive1.getBooks().isEmpty());
        assertTrue(archive1.getUsers().isEmpty());
        assertTrue(archive1.getLoans().isEmpty());

        // chiamata successiva deve restituire lo stesso riferimento
        LibraryArchive archive2 = libraryArchiveService.getLibraryArchive();
        assertSame(archive1, archive2);
    }

    // -------------------------------------------------------------
    // loadArchive
    // -------------------------------------------------------------

    @Test
    @DisplayName("loadArchive carica un LibraryArchive esistente dal file e lo imposta come archivio corrente")
    void loadArchiveLoadsExistingArchiveFromFile() throws IOException {
        // arrange: creo un archivio con qualche dato e lo salvo con LO STESSO ArchiveFileService
        LibraryArchive original = new LibraryArchive();

        Book book = new Book("Clean Code",
                List.of("Robert C. Martin"),
                2008,
                "9780132350884",
                3);
        original.addBook(book);

        User user = new User("Mario", "Rossi", "mario.rossi@unisa.it", "S123456");
        original.addUser(user);

        LocalDate dueDate = LocalDate.of(2025, 1, 10);
        original.addLoan(user, book, dueDate);

        // 🔧 FIX: scrivo usando archiveFileService (stesso canale che loadArchive usa per leggere)
        archiveFileService.saveArchive(original);

        // act
        LibraryArchive loaded = libraryArchiveService.loadArchive();

        // assert
        assertNotNull(loaded);
        assertEquals(1, loaded.getBooks().size());
        assertEquals(1, loaded.getUsers().size());
        assertEquals(1, loaded.getLoans().size());

        // deve essere anche l'archivio interno corrente
        assertSame(loaded, libraryArchiveService.getLibraryArchive());
    }

    @Test
    @DisplayName("loadArchive con file mancante crea un nuovo archivio vuoto")
    void loadArchiveWithMissingFileCreatesEmptyArchive() throws IOException {
        // arrange: mi assicuro che il file non esista
        File f = new File(testFilePath);
        if (f.exists()) {
            assertTrue(f.delete());
        }

        // act
        LibraryArchive loaded = libraryArchiveService.loadArchive();

        // assert
        assertNotNull(loaded);
        assertTrue(loaded.getBooks().isEmpty());
        assertTrue(loaded.getUsers().isEmpty());
        assertTrue(loaded.getLoans().isEmpty());

        // e deve coincidere con l'archivio interno
        assertSame(loaded, libraryArchiveService.getLibraryArchive());
    }

    @Test
    @DisplayName("loadArchive rilancia IOException non FileNotFoundException")
    void loadArchiveRethrowsNonFileNotFoundIOException() {
        ArchiveFileService failingAfs = new ArchiveFileService(testFilePath, fileService) {
            @Override
            public LibraryArchive loadArchive() throws IOException {
                throw new IOException("Errore I/O generico");
            }
        };

        LibraryArchiveService failingService = new LibraryArchiveService(failingAfs);

        assertThrows(IOException.class, failingService::loadArchive);
    }

    // -------------------------------------------------------------
    // saveArchive
    // -------------------------------------------------------------

    @Test
    @DisplayName("saveArchive con archivio null lancia IllegalArgumentException")
    void saveArchiveWithNullThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> libraryArchiveService.saveArchive(null));
    }

    @Test
    @DisplayName("saveArchive salva l'archivio su file e aggiorna il riferimento interno")
    void saveArchiveWritesArchiveToFileAndUpdatesInternalReference() throws IOException {
        // arrange
        LibraryArchive archive = new LibraryArchive();

        Book book = new Book("Refactoring",
                List.of("Martin Fowler"),
                1999,
                "9780201485677",
                2);
        archive.addBook(book);

        // act
        libraryArchiveService.saveArchive(archive);

        // assert 1
        assertSame(archive, libraryArchiveService.getLibraryArchive());

        // assert 2: verifico ricaricando tramite lo stesso servizio (evito dipendere dal formato FileService)
        LibraryArchive reloaded = libraryArchiveService.loadArchive();
        assertNotNull(reloaded);
        assertEquals(1, reloaded.getBooks().size());
        assertEquals("Refactoring", reloaded.getBooks().get(0).getTitle());
    }

    // -------------------------------------------------------------
    // getAllBooks / getAllUsers / getAllLoans
    // -------------------------------------------------------------

    @Test
    @DisplayName("getAllBooks restituisce i libri dell'archivio corrente")
    void getAllBooksReturnsBooksFromCurrentArchive() {
        LibraryArchive archive = libraryArchiveService.getLibraryArchive();

        Book b1 = new Book("A", List.of("Autore1"), 2000, "1111111111", 1);
        Book b2 = new Book("B", List.of("Autore2"), 2001, "2222222222", 1);
        archive.addBook(b1);
        archive.addBook(b2);

        List<Book> books = libraryArchiveService.getAllBooks();
        assertEquals(2, books.size());
        assertTrue(books.contains(b1));
        assertTrue(books.contains(b2));
    }

    @Test
    @DisplayName("getAllUsers restituisce gli utenti dell'archivio corrente")
    void getAllUsersReturnsUsersFromCurrentArchive() {
        LibraryArchive archive = libraryArchiveService.getLibraryArchive();

        User u1 = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        User u2 = new User("Luca", "Bianchi", "l.bianchi@unisa.it", "S2");
        archive.addUser(u1);
        archive.addUser(u2);

        List<User> users = libraryArchiveService.getAllUsers();
        assertEquals(2, users.size());
        assertTrue(users.contains(u1));
        assertTrue(users.contains(u2));
    }

    @Test
    @DisplayName("getAllLoans restituisce i prestiti dell'archivio corrente")
    void getAllLoansReturnsLoansFromCurrentArchive() {
        LibraryArchive archive = libraryArchiveService.getLibraryArchive();

        Book book = new Book("A", List.of("Autore1"), 2000, "1111111111", 1);
        User user = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        archive.addBook(book);
        archive.addUser(user);

        LocalDate dueDate = LocalDate.of(2025, 1, 10);
        Loan loan = archive.addLoan(user, book, dueDate);

        List<Loan> loans = libraryArchiveService.getAllLoans();
        assertEquals(1, loans.size());
        assertTrue(loans.contains(loan));
    }
}