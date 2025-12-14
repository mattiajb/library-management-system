/**
 * @file LoanServiceTest.java
 * @ingroup TestsService
 * @brief Suite di test di unità per il servizio applicativo LoanService.
 *
 * Verifica:
 * - validazione dei campi obbligatori nella registrazione di un prestito;
 * - controllo della disponibilità delle copie di un libro;
 * - rispetto del limite massimo di prestiti attivi per utente;
 * - corretto aggiornamento dello stato del prestito in fase di restituzione;
 * - coerenza tra stato del prestito e numero di copie disponibili del libro.
 *
 * @note Tutti i test utilizzano un meccanismo di persistenza fittizio
 *       (in-memory) per evitare l'accesso al file system reale.
 */
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

/**
 * @brief Test di unità per LoanService.
 *
 * I test coprono:
 * - registerLoan: validazioni, vincoli di business e creazione del prestito;
 * - returnLoan: chiusura del prestito e aggiornamento delle copie disponibili.
 */
class LoanServiceTest {

    /**
     * @brief Implementazione fittizia di ArchiveFileService in memoria.
     */
    private static class InMemoryArchiveFileService extends ArchiveFileService {

        ///  Riferimento all'archivio memorizzato
        private LibraryArchive stored;

        /**
         * @brief Costruttore del servizio fittizio.
         *
         * Il percorso del file è ignorato
         */
        InMemoryArchiveFileService() {
            super("IGNORED.bin", new FileService());
        }

        /**
         * @brief Restituisce l'archivio salvato in memoria.
         */
        @Override
        public LibraryArchive loadArchive() throws IOException { return stored; }

        /**
         * @brief Salva l'archivio in memoria.
         */
        @Override
        public void saveArchive(LibraryArchive archive) throws IOException { stored = archive; }
    }

    ///  Servizio di archiviazione condiviso con LoanService
    private LibraryArchiveService archiveService;

    /// Service sotto test
    private LoanService loanService;

    /**
     * @brief Inizializza il contesto di test prima di ogni caso di prova.
     *
     * Ogni test lavora su un archivio nuovo e indipendente.
     */
    @BeforeEach
    void setUp() {
        archiveService = new LibraryArchiveService(new InMemoryArchiveFileService());
        loanService = new LoanService(archiveService);
    }

    /**
     * @brief Metodo di utilità per creare un libro con un dato numero di copie.
     *
     * @return Nuova istanza di Book.
     */
    private static Book bookWithCopies(int totalCopies) {
        return new Book("Titolo", List.of("Autore"), Year.now().getValue(), "1111111111", totalCopies);
    }

    /**
     * @brief Verifica che registerLoan lanci MandatoryFieldException se l'utente è null.
     */
    @Test
    @DisplayName("registerLoan: user null -> MandatoryFieldException")
    void registerLoanNullUserThrows() {
        Book b = bookWithCopies(1);
        assertThrows(MandatoryFieldException.class,
                () -> loanService.registerLoan(null, b, LocalDate.now().plusDays(7)));
    }

    /**
     * @brief Verifica che registerLoan lanci MandatoryFieldException se il libro è null.
     */
    @Test
    @DisplayName("registerLoan: book null -> MandatoryFieldException")
    void registerLoanNullBookThrows() {
        User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        assertThrows(MandatoryFieldException.class,
                () -> loanService.registerLoan(u, null, LocalDate.now().plusDays(7)));
    }

    /**
     * @brief Verifica che registerLoan lanci MandatoryFieldException se la data di scadenza è null.
     */
    @Test
    @DisplayName("registerLoan: dueDate null -> MandatoryFieldException")
    void registerLoanNullDueDateThrows() {
        User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        Book b = bookWithCopies(1);
        assertThrows(MandatoryFieldException.class,
                () -> loanService.registerLoan(u, b, null));
    }

    /**
     * @brief Verifica che registerLoan lanci NoAvailableCopiesException se il libro non ha copie disponibili.
     */
    @Test
    @DisplayName("registerLoan: nessuna copia disponibile -> NoAvailableCopiesException")
    void registerLoanNoCopiesThrows() {
        User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        Book b = bookWithCopies(1);
        b.decrementAvailableCopies(); // esaurisce le copie

        assertThrows(NoAvailableCopiesException.class,
                () -> loanService.registerLoan(u, b, LocalDate.now().plusDays(7)));
    }

    /**
     * @brief Verifica che registerLoan lanci MandatoryFieldException se la data di scadenza è nel passato.
     */
    @Test
    @DisplayName("registerLoan: dueDate nel passato -> MandatoryFieldException")
    void registerLoanPastDueDateThrows() {
        User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        Book b = bookWithCopies(1);

        assertThrows(MandatoryFieldException.class,
                () -> loanService.registerLoan(u, b, LocalDate.now().minusDays(1)));
    }

    /**
     * @brief Verifica che registerLoan lanci MaxLoansReachedException se l'utente ha già 3 prestiti attivi.
     */
    @Test
    @DisplayName("registerLoan: oltre 3 prestiti attivi -> MaxLoansReachedException")
    void registerLoanMaxLoansThrows() throws Exception {
        LibraryArchive a = archiveService.getLibraryArchive();
        User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        a.addUser(u);

        for (int i = 0; i < 3; i++) {
            Book b = new Book("B" + i, List.of("Autore"), Year.now().getValue(), "111111111" + i, 1);
            a.addBook(b);
            a.addLoan(u, b, LocalDate.now().plusDays(10));
        }

        Book extra = new Book("Extra", List.of("Autore"), Year.now().getValue(), "2222222222", 1);
        a.addBook(extra);

        assertThrows(MaxLoansReachedException.class,
                () -> loanService.registerLoan(u, extra, LocalDate.now().plusDays(7)));
    }

    /**
     * @brief Verifica che registerLoan crei correttamente il prestito,
     *        decrementi le copie del libro e aggiorni l'archivio.
     */
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

    /**
     * @brief Verifica che returnLoan lanci MandatoryFieldException se il prestito è null.
     */
    @Test
    @DisplayName("returnLoan: null -> MandatoryFieldException")
    void returnLoanNullThrows() {
        assertThrows(MandatoryFieldException.class, () -> loanService.returnLoan(null));
    }

    /**
     * @brief Verifica che returnLoan lanci MandatoryFieldException se il prestito è già chiuso.
     */
    @Test
    @DisplayName("returnLoan: prestito già chiuso -> MandatoryFieldException")
    void returnLoanAlreadyClosedThrows() {
        User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        Book b = bookWithCopies(1);
        Loan loan = new Loan(1, u, b, LocalDate.now(), LocalDate.now().plusDays(7), false);

        assertThrows(MandatoryFieldException.class, () -> loanService.returnLoan(loan));
    }

    /**
     * @brief Verifica che returnLoan chiuda il prestito e incrementi le copie del libro.
     */
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