package swe.group04.libraryms.service;

import org.junit.jupiter.api.*;
import swe.group04.libraryms.exceptions.*;
import swe.group04.libraryms.models.Book;
import swe.group04.libraryms.models.LibraryArchive;
import swe.group04.libraryms.models.Loan;
import swe.group04.libraryms.models.User;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoanServiceTest {

    private static final String DEFAULT_ARCHIVE_FILE = "library-archive.dat";

    private LibraryArchive archive;
    private LoanService loanService;

    @BeforeEach
    void setUp() {
        loanService = ServiceLocator.getLoanService();
        archive = ServiceLocator.getArchiveService().getLibraryArchive();
        clearArchiveCompletely();
    }

    @AfterEach
    void tearDown() {
        File f = new File(DEFAULT_ARCHIVE_FILE);
        if (f.exists()) {
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        }
    }

    private void clearArchiveCompletely() {
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

    private User mkUser(String first, String last, String email, String code) {
        return new User(first, last, email, code);
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
        assertThrows(IllegalArgumentException.class, () -> new LoanService(null));
    }

    /* --------------------------------------------------------------------- */
    /*                             registerLoan                               */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("registerLoan: crea un prestito valido, decrementa copie, persiste")
    void registerLoanCreatesLoanAndDecrementsCopies() throws Exception {
        User u = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        Book b = mkBook("Reti", "Kurose", 2020, "1234567890", 2);

        archive.addUser(u);
        archive.addBook(b);

        LocalDate due = LocalDate.now().plusDays(7);

        int beforeCopies = b.getAvailableCopies();
        Loan loan = loanService.registerLoan(u, b, due);

        assertNotNull(loan);
        assertTrue(loan.isActive());
        assertEquals(beforeCopies - 1, b.getAvailableCopies());

        // Deve essere stato inserito in archivio
        assertEquals(1, archive.getLoans().size());
        assertTrue(archive.getLoans().contains(loan));
    }

    @Test
    @DisplayName("registerLoan: MandatoryFieldException se user è null")
    void registerLoanThrowsIfUserNull() {
        Book b = mkBook("Reti", "Kurose", 2020, "1234567890", 1);
        assertThrows(MandatoryFieldException.class,
                () -> loanService.registerLoan(null, b, LocalDate.now().plusDays(1)));
    }

    @Test
    @DisplayName("registerLoan: MandatoryFieldException se book è null")
    void registerLoanThrowsIfBookNull() {
        User u = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        assertThrows(MandatoryFieldException.class,
                () -> loanService.registerLoan(u, null, LocalDate.now().plusDays(1)));
    }

    @Test
    @DisplayName("registerLoan: MandatoryFieldException se dueDate è null")
    void registerLoanThrowsIfDueDateNull() {
        User u = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        Book b = mkBook("Reti", "Kurose", 2020, "1234567890", 1);

        assertThrows(MandatoryFieldException.class,
                () -> loanService.registerLoan(u, b, null));
    }

    @Test
    @DisplayName("registerLoan: NoAvailableCopiesException se non ci sono copie disponibili")
    void registerLoanThrowsIfNoAvailableCopies() {
        User u = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        Book b = mkBook("Reti", "Kurose", 2020, "1234567890", 1);

        // porto availableCopies a 0 in modo coerente col modello
        b.decrementAvailableCopies();

        assertEquals(0, b.getAvailableCopies());

        assertThrows(NoAvailableCopiesException.class,
                () -> loanService.registerLoan(u, b, LocalDate.now().plusDays(1)));
    }

    @Test
    @DisplayName("registerLoan: MaxLoansReachedException se l'utente ha già 3 prestiti attivi")
    void registerLoanThrowsIfMaxLoansReached() {
        User u = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        archive.addUser(u);

        // 3 libri diversi, 3 prestiti attivi già presenti
        Book b1 = mkBook("B1", "A1", 2020, "1111111111", 1);
        Book b2 = mkBook("B2", "A2", 2020, "2222222222", 1);
        Book b3 = mkBook("B3", "A3", 2020, "3333333333", 1);
        Book b4 = mkBook("B4", "A4", 2020, "4444444444", 1);

        archive.addBook(b1);
        archive.addBook(b2);
        archive.addBook(b3);
        archive.addBook(b4);

        archive.addLoan(u, b1, LocalDate.now().plusDays(7));
        archive.addLoan(u, b2, LocalDate.now().plusDays(7));
        archive.addLoan(u, b3, LocalDate.now().plusDays(7));

        assertThrows(MaxLoansReachedException.class,
                () -> loanService.registerLoan(u, b4, LocalDate.now().plusDays(7)));
    }

    @Test
    @DisplayName("registerLoan: MandatoryFieldException se dueDate è nel passato")
    void registerLoanThrowsIfDueDateInPast() {
        User u = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        Book b = mkBook("Reti", "Kurose", 2020, "1234567890", 1);

        assertThrows(MandatoryFieldException.class,
                () -> loanService.registerLoan(u, b, LocalDate.now().minusDays(1)));
    }

    /* --------------------------------------------------------------------- */
    /*                               returnLoan                               */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("returnLoan: chiude prestito attivo, imposta returnDate, incrementa copie")
    void returnLoanClosesLoanAndIncrementsCopies() throws Exception {
        User u = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        Book b = mkBook("Reti", "Kurose", 2020, "1234567890", 1);

        archive.addUser(u);
        archive.addBook(b);

        // creo prestito tramite service così decrementa le copie
        Loan loan = loanService.registerLoan(u, b, LocalDate.now().plusDays(7));
        assertEquals(0, b.getAvailableCopies());
        assertTrue(loan.isActive());
        assertNull(loan.getReturnDate());

        loanService.returnLoan(loan);

        assertFalse(loan.isActive());
        assertNotNull(loan.getReturnDate());
        assertEquals(1, b.getAvailableCopies());
    }

    @Test
    @DisplayName("returnLoan: MandatoryFieldException se loan è null")
    void returnLoanThrowsIfLoanNull() {
        assertThrows(MandatoryFieldException.class, () -> loanService.returnLoan(null));
    }

    @Test
    @DisplayName("returnLoan: MandatoryFieldException se loan è già chiuso")
    void returnLoanThrowsIfLoanAlreadyClosed() {
        User u = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        Book b = mkBook("Reti", "Kurose", 2020, "1234567890", 1);

        // creo un prestito e lo chiudo manualmente
        Loan loan = archive.addLoan(u, b, LocalDate.now().plusDays(7));
        loan.setStatus(false);

        assertFalse(loan.isActive());
        assertThrows(MandatoryFieldException.class, () -> loanService.returnLoan(loan));
    }

    /* --------------------------------------------------------------------- */
    /*                               getActiveLoan                            */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("getActiveLoan: restituisce solo prestiti attivi ordinati per dueDate (nullsLast)")
    void getActiveLoanReturnsOnlyActiveSortedByDueDate() {
        User u = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        Book b1 = mkBook("B1", "A1", 2020, "1111111111", 1);
        Book b2 = mkBook("B2", "A2", 2020, "2222222222", 1);
        Book b3 = mkBook("B3", "A3", 2020, "3333333333", 1);

        // prestiti: due tra loro attivi, uno chiuso
        Loan l1 = archive.addLoan(u, b1, LocalDate.now().plusDays(10));
        Loan l2 = archive.addLoan(u, b2, LocalDate.now().plusDays(3));
        Loan l3 = archive.addLoan(u, b3, LocalDate.now().plusDays(5));
        l3.setStatus(false); // chiuso

        List<Loan> active = loanService.getActiveLoan();

        assertEquals(2, active.size());
        assertTrue(active.contains(l1));
        assertTrue(active.contains(l2));
        assertFalse(active.contains(l3));

        // ordinamento: l2 (3 giorni) prima di l1 (10 giorni)
        assertEquals(List.of(l2, l1), active);
    }

    /* --------------------------------------------------------------------- */
    /*                                 isLate                                 */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("isLate: false se loan è null")
    void isLateFalseIfNull() {
        assertFalse(loanService.isLate(null));
    }

    @Test
    @DisplayName("isLate: false se loan non è attivo")
    void isLateFalseIfNotActive() {
        User u = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        Book b = mkBook("Reti", "Kurose", 2020, "1234567890", 1);

        Loan loan = archive.addLoan(u, b, LocalDate.now().minusDays(2));
        loan.setStatus(false);

        assertFalse(loanService.isLate(loan));
    }

    @Test
    @DisplayName("isLate: false se dueDate è null")
    void isLateFalseIfDueDateNull() {
        User u = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        Book b = mkBook("Reti", "Kurose", 2020, "1234567890", 1);

        Loan loan = archive.addLoan(u, b, LocalDate.now().plusDays(7));
        // forzo null se il modello lo consente; se non lo consente, questo test va rimosso
        loan.setDueDate(null);

        assertFalse(loanService.isLate(loan));
    }

    @Test
    @DisplayName("isLate: true se attivo e dueDate precedente a oggi")
    void isLateTrueIfActiveAndDueDateBeforeToday() {
        User u = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        Book b = mkBook("Reti", "Kurose", 2020, "1234567890", 1);

        Loan loan = archive.addLoan(u, b, LocalDate.now().minusDays(1));
        assertTrue(loan.isActive());

        assertTrue(loanService.isLate(loan));
    }

    @Test
    @DisplayName("isLate: false se attivo ma dueDate è oggi o futura")
    void isLateFalseIfDueDateTodayOrFuture() {
        User u = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        Book b = mkBook("Reti", "Kurose", 2020, "1234567890", 1);

        Loan today = archive.addLoan(u, b, LocalDate.now());
        Loan future = archive.addLoan(u, b, LocalDate.now().plusDays(1));

        assertFalse(loanService.isLate(today));
        assertFalse(loanService.isLate(future));
    }

    /* --------------------------------------------------------------------- */
    /*                         getLoansSortedByDueDate                         */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("getLoansSortedByDueDate: ordina tutti i prestiti per dueDate (nullsLast)")
    void getLoansSortedByDueDateSortsAllLoans() {
        User u = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        Book b1 = mkBook("B1", "A1", 2020, "1111111111", 1);
        Book b2 = mkBook("B2", "A2", 2020, "2222222222", 1);
        Book b3 = mkBook("B3", "A3", 2020, "3333333333", 1);

        Loan l1 = archive.addLoan(u, b1, LocalDate.now().plusDays(10));
        Loan l2 = archive.addLoan(u, b2, LocalDate.now().plusDays(3));
        Loan l3 = archive.addLoan(u, b3, LocalDate.now().plusDays(5));

        List<Loan> sorted = loanService.getLoansSortedByDueDate();
        assertEquals(List.of(l2, l3, l1), sorted);
    }
}
