/**
 * @file LoanTest.java
 * @ingroup TestsModels
 * @brief Test di unità per la classe Loan.
 * 
 * Verifica:
 * - inizializzazione tramite costruttore;
 * - correttezza di getter e setter;
 * - gestione dello stato (status) tramite getStatus(), setStatus() e isActive();
 * - contratto di equals() e hashCode() basato su loanId;
 * - contenuto informativo di toString().
 */
package swe.group04.libraryms.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @ingroup TestsModels
 * @brief Suite di test per il modello Loan.
 *
 * Verifica le proprietà funzionali essenziali del prestito e
 * la coerenza dei metodi fondamentali del modello.
 */
class LoanTest {

    //  Identificativo del prestito usato come fixture di base.
    private static final int LOAN_ID = 42;

    //  @brief Utente associato al prestito.
    private User user;

    //  Libro associato al prestito (fixture).
    private Book book;

    //  Data di registrazione del prestito.
    private LocalDate loanDate;

    //  Data di scadenza prevista del prestito.
    private LocalDate dueDate;

    //  Istanza di prestito sottoposta ai test.
    private Loan loan;

    /**
     * @brief Crea un utente, un libro, le date e un prestito in stato attivo.
     */
    @BeforeEach
    void setUp() {
        user = new User("Mario", "Rossi", "mario.rossi@example.com", "S123456");
        book = new Book(
                "Clean Code",
                List.of("Robert C. Martin"),
                2008,
                "9780132350884",
                3
        );
        loanDate = LocalDate.of(2025, 1, 10);
        dueDate  = LocalDate.of(2025, 2, 10);

        //  utilizzo di status=true come caso di default
        loan = new Loan(LOAN_ID, user, book, loanDate, dueDate, true);
    }

    // ---------------------------------------------------------------------
    //                      Costruttore e getter
    // ---------------------------------------------------------------------

    /**
     * @brief Verifica l'inizializzazione dei campi tramite costruttore.
     *
     * Controlla che:
     * - l'ID sia quello atteso;
     * - user e book siano gli stessi riferimenti forniti;
     * - loanDate e dueDate corrispondano ai valori passati;
     * - returnDate sia null all'atto della creazione.
     */
    @Test
    @DisplayName("Il costruttore inizializza correttamente ID, user, book, loanDate, dueDate e returnDate=null")
    void constructorInitializesFieldsCorrectly() {
        assertEquals(LOAN_ID, loan.getLoanId());
        assertSame(user, loan.getUser());
        assertSame(book, loan.getBook());
        assertEquals(loanDate, loan.getLoanDate());
        assertEquals(dueDate, loan.getDueDate());
        assertNull(loan.getReturnDate());
    }

    /**
     * @brief Verifica il rispetto del parametro status nel costruttore
     *
     * Questo test evidenzia un comportamento atteso: se l'implementazione
     * del modello non rispetta il parametro status, il test fallisce e segnala un difetto.
     */
    @Test
    @DisplayName("Il costruttore dovrebbe rispettare il parametro status")
    void constructorShouldRespectStatusParameter() {
        Loan activeLoan  = new Loan(1, user, book, loanDate, dueDate, true);
        Loan closedLoan  = new Loan(2, user, book, loanDate, dueDate, false);

        //  Comportamento desiderato:
        assertTrue(activeLoan.getStatus(), "status=true dovrebbe produrre prestito attivo");
        assertFalse(closedLoan.getStatus(), "status=false dovrebbe produrre prestito non attivo");

        assertTrue(activeLoan.isActive());
        assertFalse(closedLoan.isActive());
        //  Con l'implementazione attuale questo test fallirà: evidenzia il bug.
    }

    // ---------------------------------------------------------------------
    //          Setter base: user, book, loanDate, dueDate, returnDate
    // ---------------------------------------------------------------------

    /**
     * @brief Verifica l'aggiornamento dei riferimenti a user e book.
     * 
     * I setter aggiornano correttamente user e book.
     */
    @Test
    @DisplayName("I setter aggiornano correttamente user e book")
    void settersUpdateUserAndBook() {
        User anotherUser = new User("Luigi", "Bianchi", "luigi.bianchi@example.com", "S654321");
        Book anotherBook = new Book(
                "Refactoring",
                List.of("Martin Fowler"),
                1999,
                "9780201485677",
                2
        );

        loan.setUser(anotherUser);
        loan.setBook(anotherBook);

        assertSame(anotherUser, loan.getUser());
        assertSame(anotherBook, loan.getBook());
    }

    /**
     * @brief Verifica l'aggiornamento delle date del prestito.
     * 
     * I setter aggiornano correttamente le date di prestito e scadenza.
     */
    @Test
    @DisplayName("I setter aggiornano correttamente le date di prestito e scadenza")
    void settersUpdateDates() {
        LocalDate newLoanDate = LocalDate.of(2025, 3, 1);
        LocalDate newDueDate  = LocalDate.of(2025, 3, 20);

        loan.setLoanDate(newLoanDate);
        loan.setDueDate(newDueDate);

        assertEquals(newLoanDate, loan.getLoanDate());
        assertEquals(newDueDate, loan.getDueDate());
    }

    /**
     * @brief Verifica chd setReturnDate aggiorni correttamente la data di restituzione.
     *
     * Controlla che:
     * - inizialmente returnDate sia null;
     * - venga impostata una data;
     * - sia lecito reimpostarla a null.
     */
    @Test
    @DisplayName("setReturnDate aggiorna correttamente la data di restituzione")
    void setReturnDateUpdatesReturnDate() {
        assertNull(loan.getReturnDate());

        LocalDate returnDate = LocalDate.of(2025, 1, 25);
        loan.setReturnDate(returnDate);

        assertEquals(returnDate, loan.getReturnDate());

        // È lecito anche riportarla a null (prestito di nuovo considerato 'non restituito')
        loan.setReturnDate(null);
        assertNull(loan.getReturnDate());
    }

    // ---------------------------------------------------------------------
    //                  status, setStatus e isActive
    // ---------------------------------------------------------------------

    /**
     * @brief Verifica la coerenza tra getStatus() e isActive().
     */
    @Test
    @DisplayName("getStatus e isActive riflettono lo stato corrente del prestito")
    void isActiveReflectsStatus() {
        //  Caso 1: prestito attivo
        loan.setStatus(true);
        assertTrue(loan.getStatus());
        assertTrue(loan.isActive());

        //  Caso 2: prestito non attivo
        loan.setStatus(false);
        assertFalse(loan.getStatus());
        assertFalse(loan.isActive());
    }

    /**
     * @brief Verifica che setStatus imposti e ritorni il nuovo valore.
     */
    @Test
    @DisplayName("setStatus imposta lo stato e restituisce il nuovo valore")
    void setStatusUpdatesAndReturnsNewValue() {
        boolean result1 = loan.setStatus(false);
        assertFalse(result1);
        assertFalse(loan.getStatus());

        boolean result2 = loan.setStatus(true);
        assertTrue(result2);
        assertTrue(loan.getStatus());
    }

    // ---------------------------------------------------------------------
    //                      equals e hashCode
    // ---------------------------------------------------------------------

    /**
     * @brief Verifica proprietà basilari di equals().
     * 
     * Equals è riflessivo, gestisce null e oggetti di classe diversa.
     */
    @Test
    @DisplayName("equals è riflessivo, gestisce null e oggetti di classe diversa")
    void equalsBasicProperties() {
        assertEquals(loan, loan);        // riflessivo
        assertNotEquals(loan, null);     // confronto con null
        assertNotEquals(loan, "stringa"); // tipo diverso
    }

    /**
     * @brief Verifica che equals/hashCode dipendano da loanId.
     * 
     * Due Loan con lo stesso loanId sono uguali, anche se altri campi differiscono.
     */
    @Test
    @DisplayName("Due Loan con lo stesso loanId sono uguali, anche se altri campi differiscono")
    void equalsSameLoanIdDifferentOtherFields() {
        Loan l1 = new Loan(100, user, book, loanDate, dueDate, true);

        User anotherUser = new User("X", "Y", "x.y@example.com", "S000000");
        Book anotherBook = new Book(
                "Another Book",
                List.of("Some Author"),
                2020,
                "1111111111111",
                1
        );
        LocalDate ld2 = LocalDate.of(2030, 1, 1);
        LocalDate dd2 = LocalDate.of(2030, 2, 1);

        Loan l2 = new Loan(100, anotherUser, anotherBook, ld2, dd2, false);

        assertEquals(l1, l2);
        assertEquals(l1.hashCode(), l2.hashCode());
    }

    /**
     * @brief Verifica la disuguaglianza su loanId differente.
     * 
     * Due Loan con loanId diversi NON sono uguali.
     */
    @Test
    @DisplayName("Due Loan con loanId diversi NON sono uguali")
    void equalsDifferentLoanId() {
        Loan l1 = new Loan(1, user, book, loanDate, dueDate, true);
        Loan l2 = new Loan(2, user, book, loanDate, dueDate, true);

        assertNotEquals(l1, l2);
    }

    // ---------------------------------------------------------------------
    //                      toString
    // ---------------------------------------------------------------------

    /**
     * @brief Verifica il contenuto informativo di toString().
     * 
     * toString contiene le informazioni principali: ID, user code, book isbn, date.
     * Controlla anche il caso di returnDate nulla ("Not returned") e il caso
     * in cui returnDate è impostata (la data deve comparire in stringa).
     */
    @Test
    @DisplayName("toString contiene le informazioni principali (ID, user code, book isbn, date)")
    void toStringContainsMainInfo() {
        String s = loan.toString();

        //  ID del prestito
        assertTrue(s.contains("Loan ID"));
        assertTrue(s.contains(String.valueOf(LOAN_ID)));

        //  User code
        assertTrue(s.contains(user.getCode()));

        //  Book ISBN
        assertTrue(s.contains(book.getIsbn()));

        //  Loan & due date
        assertTrue(s.contains(loanDate.toString()));
        assertTrue(s.contains(dueDate.toString()));

        //  Return Date quando null => "Not returned"
        assertTrue(s.contains("Not returned"));

        //  Se imposto una returnDate, la stringa deve contenerla
        LocalDate returnDate = LocalDate.of(2025, 1, 25);
        loan.setReturnDate(returnDate);
        String s2 = loan.toString();

        assertTrue(s2.contains(returnDate.toString()));
        assertFalse(s2.contains("Not returned"));
    }
}