package swe.group04.libraryms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import swe.group04.libraryms.exceptions.MandatoryFieldException;
import swe.group04.libraryms.exceptions.MaxLoansReachedException;
import swe.group04.libraryms.exceptions.NoAvailableCopiesException;
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

class LoanServiceTest {

    private LibraryArchive archive;
    private LibraryArchiveService archiveService;
    private LoanService loanService;

    private String testFilePath;

    @BeforeEach
    void setUp() {
        archive = new LibraryArchive();

        FileService fileService = new FileService();
        testFilePath = "loanServiceTestArchive.bin";

        // pulizia preventiva
        File f = new File(testFilePath);
        if (f.exists()) {
            f.delete();
        }

        ArchiveFileService afs = new ArchiveFileService(testFilePath, fileService);
        archiveService = new LibraryArchiveService(afs);

        loanService = new LoanService(archive, archiveService);
    }

    // -------------------------------------------------------------
    // Costruttore
    // -------------------------------------------------------------

    @Test
    @DisplayName("Il costruttore lancia IllegalArgumentException se libraryArchive è null")
    void constructorThrowsIfLibraryArchiveIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new LoanService(null, archiveService));
    }

    @Test
    @DisplayName("Il costruttore lancia IllegalArgumentException se libraryArchiveService è null")
    void constructorThrowsIfLibraryArchiveServiceIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new LoanService(archive, null));
    }

    @Test
    @DisplayName("Il costruttore accetta parametri non null senza lanciare eccezioni")
    void constructorAcceptsValidParameters() {
        assertDoesNotThrow(() -> new LoanService(new LibraryArchive(), archiveService));
    }

    // -------------------------------------------------------------
    // registerLoan
    // -------------------------------------------------------------

    private Book createBookWithCopies(String isbn, int totalCopies) {
        return new Book(
                "Some Title",
                List.of("Some Author"),
                2020,
                isbn,
                totalCopies
        );
    }

    private User createUser(String code) {
        return new User("Mario", "Rossi", code + "@example.com", code);
    }

    @Test
    @DisplayName("registerLoan con parametri validi crea un prestito e decrementa le copie disponibili")
    void registerLoanValidCreatesLoanAndDecrementsCopies()
            throws MandatoryFieldException, NoAvailableCopiesException, MaxLoansReachedException {

        User user = createUser("S123456");
        Book book = createBookWithCopies("9780132350884", 3);

        archive.addUser(user);
        archive.addBook(book);

        int availableBefore = book.getAvailableCopies();
        LocalDate dueDate = LocalDate.now().plusDays(7);

        Loan loan = loanService.registerLoan(user, book, dueDate);

        assertNotNull(loan);
        assertTrue(loan.isActive());
        assertEquals(availableBefore - 1, book.getAvailableCopies());
        assertTrue(archive.getLoans().contains(loan));
    }

    @Test
    @DisplayName("registerLoan: utente null -> MandatoryFieldException")
    void registerLoanUserNullThrowsMandatoryFieldException() {
        Book book = createBookWithCopies("9780132350884", 3);
        archive.addBook(book);
        LocalDate dueDate = LocalDate.now().plusDays(7);

        assertThrows(MandatoryFieldException.class,
                () -> loanService.registerLoan(null, book, dueDate));

        assertTrue(archive.getLoans().isEmpty());
    }

    @Test
    @DisplayName("registerLoan: libro null -> MandatoryFieldException")
    void registerLoanBookNullThrowsMandatoryFieldException() {
        User user = createUser("S123456");
        archive.addUser(user);
        LocalDate dueDate = LocalDate.now().plusDays(7);

        assertThrows(MandatoryFieldException.class,
                () -> loanService.registerLoan(user, null, dueDate));

        assertTrue(archive.getLoans().isEmpty());
    }

    @Test
    @DisplayName("registerLoan: dueDate null -> MandatoryFieldException")
    void registerLoanDueDateNullThrowsMandatoryFieldException() {
        User user = createUser("S123456");
        Book book = createBookWithCopies("9780132350884", 3);
        archive.addUser(user);
        archive.addBook(book);

        assertThrows(MandatoryFieldException.class,
                () -> loanService.registerLoan(user, book, null));

        assertTrue(archive.getLoans().isEmpty());
    }

    @Test
    @DisplayName("registerLoan: nessuna copia disponibile -> NoAvailableCopiesException")
    void registerLoanNoAvailableCopiesThrows() {
        User user = createUser("S123456");
        Book book = createBookWithCopies("9780132350884", 1);
        archive.addUser(user);
        archive.addBook(book);

        // simulo esaurimento copie
        book.setAvailableCopies(0);

        LocalDate dueDate = LocalDate.now().plusDays(7);

        assertThrows(NoAvailableCopiesException.class,
                () -> loanService.registerLoan(user, book, dueDate));

        assertTrue(archive.getLoans().isEmpty());
    }

    @Test
    @DisplayName("registerLoan: utente con 3 prestiti attivi -> MaxLoansReachedException")
    void registerLoanUserAtMaxLoansThrows() throws MandatoryFieldException, NoAvailableCopiesException, MaxLoansReachedException {
        User user = createUser("S123456");
        archive.addUser(user);

        // Creo 3 libri diversi e 3 prestiti attivi
        Book b1 = createBookWithCopies("1111111111", 3);
        Book b2 = createBookWithCopies("2222222222", 3);
        Book b3 = createBookWithCopies("3333333333", 3);
        archive.addBook(b1);
        archive.addBook(b2);
        archive.addBook(b3);

        LocalDate d1 = LocalDate.now().plusDays(10);
        LocalDate d2 = LocalDate.now().plusDays(11);
        LocalDate d3 = LocalDate.now().plusDays(12);

        loanService.registerLoan(user, b1, d1);
        loanService.registerLoan(user, b2, d2);
        loanService.registerLoan(user, b3, d3);

        // Ora provo un quarto prestito
        Book b4 = createBookWithCopies("4444444444", 3);
        archive.addBook(b4);
        LocalDate d4 = LocalDate.now().plusDays(13);

        assertThrows(MaxLoansReachedException.class,
                () -> loanService.registerLoan(user, b4, d4));
    }

    @Test
    @DisplayName("registerLoan: dueDate nel passato -> MandatoryFieldException")
    void registerLoanDueDateInPastThrowsMandatoryFieldException() {
        User user = createUser("S123456");
        Book book = createBookWithCopies("9780132350884", 3);
        archive.addUser(user);
        archive.addBook(book);

        LocalDate pastDate = LocalDate.now().minusDays(1);

        assertThrows(MandatoryFieldException.class,
                () -> loanService.registerLoan(user, book, pastDate));

        assertTrue(archive.getLoans().isEmpty());
    }

    @Test
    @DisplayName("registerLoan: se il salvataggio fallisce, viene lanciata RuntimeException")
    void registerLoanPersistFailureThrowsRuntimeException()
            throws MandatoryFieldException, NoAvailableCopiesException, MaxLoansReachedException {

        // Creo un ArchiveFileService finto che fallisce nel salvataggio
        FileService fileService = new FileService();
        ArchiveFileService failingAfs = new ArchiveFileService("failingLoanService.bin", fileService) {
            @Override
            public void saveArchive(LibraryArchive archive) throws IOException {
                throw new IOException("Errore di salvataggio forzato");
            }
        };
        LibraryArchiveService failingArchiveService = new LibraryArchiveService(failingAfs);
        LoanService failingLoanService = new LoanService(archive, failingArchiveService);

        User user = createUser("S999999");
        Book book = createBookWithCopies("9999999999", 3);
        archive.addUser(user);
        archive.addBook(book);
        LocalDate dueDate = LocalDate.now().plusDays(7);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> failingLoanService.registerLoan(user, book, dueDate));

        assertTrue(ex.getMessage().contains("Errore durante il salvataggio"));
    }

    // -------------------------------------------------------------
    // returnLoan
    // -------------------------------------------------------------

    @Test
    @DisplayName("returnLoan: loan null -> MandatoryFieldException")
    void returnLoanNullThrowsMandatoryFieldException() {
        assertThrows(MandatoryFieldException.class,
                () -> loanService.returnLoan(null));
    }

    @Test
    @DisplayName("returnLoan: prestito già chiuso -> MandatoryFieldException")
    void returnLoanAlreadyClosedThrowsMandatoryFieldException() {
        User user = createUser("S123456");
        Book book = createBookWithCopies("9780132350884", 3);

        Loan loan = new Loan(1, user, book,
                LocalDate.now().minusDays(2),
                LocalDate.now().plusDays(5),
                true);

        loan.setStatus(false); // lo segno come chiuso

        assertThrows(MandatoryFieldException.class,
                () -> loanService.returnLoan(loan));
    }

   @Test
@DisplayName("returnLoan: prestito attivo viene chiuso, data restituzione impostata, copie incrementate")
void returnLoanActiveLoanIsClosedAndCopiesIncremented() {
    User user = createUser("S123456");
    Book book = createBookWithCopies("9780132350884", 3);

    // Simulo uno stato coerente con un prestito attivo:
    // una copia è già stata presa in prestito, quindi le copie disponibili
    // sono totalCopies - 1
    book.decrementAvailableCopies();
    int availableBefore = book.getAvailableCopies(); // ora è 2

    LocalDate dueDate = LocalDate.now().plusDays(7);
    Loan loan = new Loan(
            1,
            user,
            book,
            LocalDate.now().minusDays(1),
            dueDate,
            true // prestito attivo
    );

    // act
    loanService.returnLoan(loan);

    // assert
    assertFalse(loan.isActive());
    assertNotNull(loan.getReturnDate());
    assertEquals(LocalDate.now(), loan.getReturnDate());
    assertEquals(availableBefore + 1, book.getAvailableCopies());
    // opzionale: controlla che sia tornato al massimo
    assertEquals(book.getTotalCopies(), book.getAvailableCopies());
}

    // -------------------------------------------------------------
    // getActiveLoan
    // -------------------------------------------------------------

    @Test
    @DisplayName("getActiveLoan restituisce solo i prestiti attivi ordinati per dueDate")
    void getActiveLoanReturnsOnlyActiveSortedByDueDate() {
        User user = createUser("S123456");
        archive.addUser(user);

        Book b1 = createBookWithCopies("1111111111", 3);
        Book b2 = createBookWithCopies("2222222222", 3);
        Book b3 = createBookWithCopies("3333333333", 3);
        archive.addBook(b1);
        archive.addBook(b2);
        archive.addBook(b3);

        LocalDate d1 = LocalDate.now().plusDays(10);
        LocalDate d2 = LocalDate.now().plusDays(5);
        LocalDate d3 = LocalDate.now().plusDays(7);

        Loan l1 = archive.addLoan(user, b1, d1);
        Loan l2 = archive.addLoan(user, b2, d2);
        Loan l3 = archive.addLoan(user, b3, d3);

        // rendo l3 non attivo
        l3.setStatus(false);

        List<Loan> active = loanService.getActiveLoan();

        assertEquals(2, active.size());
        assertTrue(active.contains(l1));
        assertTrue(active.contains(l2));

        // ordinati per dueDate: prima d2, poi d1
        assertEquals(l2, active.get(0));
        assertEquals(l1, active.get(1));
    }

    // -------------------------------------------------------------
    // isLate
    // -------------------------------------------------------------

    @Test
    @DisplayName("isLate: loan null -> false")
    void isLateNullLoanReturnsFalse() {
        assertFalse(loanService.isLate(null));
    }

    @Test
    @DisplayName("isLate: loan non attivo -> false")
    void isLateInactiveLoanReturnsFalse() {
        User user = createUser("S123456");
        Book book = createBookWithCopies("9780132350884", 3);

        Loan loan = new Loan(1, user, book,
                LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(1),
                true);
        loan.setStatus(false);

        assertFalse(loanService.isLate(loan));
    }

    @Test
    @DisplayName("isLate: dueDate null -> false")
    void isLateDueDateNullReturnsFalse() {
        User user = createUser("S123456");
        Book book = createBookWithCopies("9780132350884", 3);

        Loan loan = new Loan(1, user, book,
                LocalDate.now().minusDays(10),
                null,
                true);

        assertFalse(loanService.isLate(loan));
    }

    @Test
    @DisplayName("isLate: prestito attivo con dueDate nel futuro o oggi -> false")
    void isLateActiveWithDueDateTodayOrFutureReturnsFalse() {
        User user = createUser("S123456");
        Book book = createBookWithCopies("9780132350884", 3);

        Loan loanToday = new Loan(1, user, book,
                LocalDate.now().minusDays(1),
                LocalDate.now(),
                true);
        Loan loanFuture = new Loan(2, user, book,
                LocalDate.now().minusDays(2),
                LocalDate.now().plusDays(1),
                true);

        assertFalse(loanService.isLate(loanToday));
        assertFalse(loanService.isLate(loanFuture));
    }

    @Test
    @DisplayName("isLate: prestito attivo con dueDate nel passato -> true")
    void isLateActiveWithDueDateInPastReturnsTrue() {
        User user = createUser("S123456");
        Book book = createBookWithCopies("9780132350884", 3);

        Loan loan = new Loan(1, user, book,
                LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(1),
                true);

        assertTrue(loanService.isLate(loan));
    }

    // -------------------------------------------------------------
    // getLoansSortedByDueDate
    // -------------------------------------------------------------

    @Test
    @DisplayName("getLoansSortedByDueDate restituisce tutti i prestiti ordinati per dueDate")
    void getLoansSortedByDueDateReturnsAllSorted() {
        User user = createUser("S123456");
        archive.addUser(user);

        Book b1 = createBookWithCopies("1111111111", 3);
        Book b2 = createBookWithCopies("2222222222", 3);
        Book b3 = createBookWithCopies("3333333333", 3);
        archive.addBook(b1);
        archive.addBook(b2);
        archive.addBook(b3);

        LocalDate d1 = LocalDate.now().plusDays(10);
        LocalDate d2 = LocalDate.now().plusDays(5);
        LocalDate d3 = LocalDate.now().plusDays(7);

        Loan l1 = archive.addLoan(user, b1, d1);
        Loan l2 = archive.addLoan(user, b2, d2);
        Loan l3 = archive.addLoan(user, b3, d3);

        List<Loan> sorted = loanService.getLoansSortedByDueDate();

        assertEquals(3, sorted.size());
        assertEquals(l2, sorted.get(0)); // d2
        assertEquals(l3, sorted.get(1)); // d3
        assertEquals(l1, sorted.get(2)); // d1
    }
}