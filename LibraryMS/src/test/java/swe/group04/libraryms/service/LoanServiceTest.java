package swe.group04.libraryms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import swe.group04.libraryms.exceptions.MaxLoansReachedException;
import swe.group04.libraryms.exceptions.MandatoryFieldException;
import swe.group04.libraryms.exceptions.NoAvailableCopiesException;
import swe.group04.libraryms.models.Book;
import swe.group04.libraryms.models.LibraryArchive;
import swe.group04.libraryms.models.Loan;
import swe.group04.libraryms.models.User;
import swe.group04.libraryms.persistence.ArchiveFileService;
import swe.group04.libraryms.persistence.FileService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoanServiceTest {

    private static class InMemoryArchiveFileService extends ArchiveFileService {
        private LibraryArchive stored;

        InMemoryArchiveFileService() {
            super("IGNORED.bin", new FileService());
        }

        @Override
        public LibraryArchive loadArchive() throws IOException { return stored; }

        @Override
        public void saveArchive(LibraryArchive archive) throws IOException { stored = archive; }
    }

    private LibraryArchiveService archiveService;
    private LoanService loanService;

    @BeforeEach
    void setUp() {
        archiveService = new LibraryArchiveService(new InMemoryArchiveFileService());
        loanService = new LoanService(archiveService);
    }

    private static Book bookWithCopies(int totalCopies) {
        return new Book("Titolo", List.of("Autore"), Year.now().getValue(), "1111111111", totalCopies);
    }

    @Test
    @DisplayName("registerLoan: user null -> MandatoryFieldException")
    void registerLoanNullUserThrows() {
        Book b = bookWithCopies(1);
        assertThrows(MandatoryFieldException.class,
                () -> loanService.registerLoan(null, b, LocalDate.now().plusDays(7)));
    }

    @Test
    @DisplayName("registerLoan: book null -> MandatoryFieldException")
    void registerLoanNullBookThrows() {
        User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        assertThrows(MandatoryFieldException.class,
                () -> loanService.registerLoan(u, null, LocalDate.now().plusDays(7)));
    }

    @Test
    @DisplayName("registerLoan: dueDate null -> MandatoryFieldException")
    void registerLoanNullDueDateThrows() {
        User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        Book b = bookWithCopies(1);
        assertThrows(MandatoryFieldException.class,
                () -> loanService.registerLoan(u, b, null));
    }

    @Test
    @DisplayName("registerLoan: nessuna copia disponibile -> NoAvailableCopiesException")
    void registerLoanNoCopiesThrows() {
        User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        Book b = bookWithCopies(1);
        // esaurisco copie
        b.decrementAvailableCopies();

        assertThrows(NoAvailableCopiesException.class,
                () -> loanService.registerLoan(u, b, LocalDate.now().plusDays(7)));
    }

    @Test
    @DisplayName("registerLoan: dueDate nel passato -> MandatoryFieldException")
    void registerLoanPastDueDateThrows() {
        User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        Book b = bookWithCopies(1);

        assertThrows(MandatoryFieldException.class,
                () -> loanService.registerLoan(u, b, LocalDate.now().minusDays(1)));
    }

    @Test
    @DisplayName("registerLoan: oltre 3 prestiti attivi -> MaxLoansReachedException")
    void registerLoanMaxLoansThrows() throws Exception {
        LibraryArchive a = archiveService.getLibraryArchive();
        User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        a.addUser(u);

        // 3 prestiti attivi già presenti
        for (int i = 0; i < 3; i++) {
            Book b = new Book("B" + i, List.of("Autore"), Year.now().getValue(), "111111111" + i, 1);
            a.addBook(b);
            a.addLoan(u, b, LocalDate.now().plusDays(10)); // attivo
        }

        Book extra = new Book("Extra", List.of("Autore"), Year.now().getValue(), "2222222222", 1);
        a.addBook(extra);

        assertThrows(MaxLoansReachedException.class,
                () -> loanService.registerLoan(u, extra, LocalDate.now().plusDays(7)));
    }

    @Test
    @DisplayName("registerLoan: crea prestito, decrementa copie e persiste in memoria")
    void registerLoanCreatesLoanAndDecrementsCopies() throws Exception {
        LibraryArchive a = archiveService.getLibraryArchive();
        User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        Book b = bookWithCopies(1);

        a.addUser(u);
        a.addBook(b);

        Loan loan = loanService.registerLoan(u, b, LocalDate.now().plusDays(7));

        assertNotNull(loan);
        assertEquals(0, b.getAvailableCopies());
        assertEquals(1, a.getLoans().size());
        assertTrue(a.getLoans().contains(loan));
    }

    @Test
    @DisplayName("returnLoan: null -> MandatoryFieldException")
    void returnLoanNullThrows() {
        assertThrows(MandatoryFieldException.class, () -> loanService.returnLoan(null));
    }

    @Test
    @DisplayName("returnLoan: prestito già chiuso -> MandatoryFieldException")
    void returnLoanAlreadyClosedThrows() {
        User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        Book b = bookWithCopies(1);
        Loan loan = new Loan(1, u, b, LocalDate.now(), LocalDate.now().plusDays(7), false); // già chiuso

        assertThrows(MandatoryFieldException.class, () -> loanService.returnLoan(loan));
    }

    @Test
    @DisplayName("returnLoan: chiude prestito e incrementa copie")
    void returnLoanClosesAndIncrementsCopies() throws Exception {
        LibraryArchive a = archiveService.getLibraryArchive();
        User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        Book b = bookWithCopies(1);

        a.addUser(u);
        a.addBook(b);

        Loan loan = loanService.registerLoan(u, b, LocalDate.now().plusDays(7));
        assertEquals(0, b.getAvailableCopies());

        loanService.returnLoan(loan);

        assertFalse(loan.isActive());
        assertNotNull(loan.getReturnDate());
        assertEquals(1, b.getAvailableCopies());
    }
}