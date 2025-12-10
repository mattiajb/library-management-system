package swe.group04.libraryms.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private static final String FIRST_NAME = "Mario";
    private static final String LAST_NAME  = "Rossi";
    private static final String EMAIL      = "mario.rossi@example.com";
    private static final String CODE       = "S123456";

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(FIRST_NAME, LAST_NAME, EMAIL, CODE);
    }

    // ---------------------------------------------------------------------
    // Costruttore e getter di base
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Il costruttore inizializza correttamente i campi di base e la lista prestiti")
    void constructorInitializesFieldsCorrectly() {
        assertEquals(FIRST_NAME, user.getFirstName());
        assertEquals(LAST_NAME,  user.getLastName());
        assertEquals(EMAIL,      user.getEmail());
        assertEquals(CODE,       user.getCode());

        // Lista prestiti vuota all'inizio
        assertNotNull(user.getActiveLoans());
        assertTrue(user.getActiveLoans().isEmpty());
    }

    // ---------------------------------------------------------------------
    // getActiveLoans: copia difensiva
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("getActiveLoans restituisce una copia difensiva della lista interna")
    void getActiveLoansReturnsDefensiveCopy() {
        // Stato iniziale: lista vuota
        List<Loan> loans1 = user.getActiveLoans();
        List<Loan> loans2 = user.getActiveLoans();

        // Le due liste devono essere istanze diverse
        assertNotSame(loans1, loans2);

        // Simuliamo un prestito aggiunto dall'oggetto
        user.addLoan(null);  // usiamo null come segnaposto

        List<Loan> loans3 = user.getActiveLoans();
        assertEquals(1, loans3.size());

        // Modifico la lista restituita
        loans3.clear();

        // Lo stato interno non deve cambiare
        assertEquals(1, user.getActiveLoans().size());
    }

    // ---------------------------------------------------------------------
    // setActiveLoans
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("setActiveLoans(null) imposta la lista dei prestiti a vuota")
    void setActiveLoansNullSetsEmptyList() {
        // Pre-carico qualche prestito
        user.addLoan(null);
        assertFalse(user.getActiveLoans().isEmpty());

        user.setActiveLoans(null);

        assertNotNull(user.getActiveLoans());
        assertTrue(user.getActiveLoans().isEmpty());
    }

    @Test
    @DisplayName("setActiveLoans usa una copia difensiva della lista passata")
    void setActiveLoansUsesDefensiveCopy() {
        List<Loan> givenList = new ArrayList<>();
        givenList.add(null); // segnaposto

        user.setActiveLoans(givenList);

        // Controllo del contenuto iniziale
        List<Loan> internalList = user.getActiveLoans();
        assertEquals(1, internalList.size());

        // Modifico la lista passata al setter
        givenList.clear();

        // Lo stato interno non deve cambiare
        assertEquals(1, user.getActiveLoans().size());
    }

    // ---------------------------------------------------------------------
    // Setter base (firstName, lastName, email)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("I setter delle informazioni anagrafiche aggiornano correttamente i campi")
    void basicSettersUpdateFields() {
        user.setFirstName("Luigi");
        user.setLastName("Bianchi");
        user.setEmail("luigi.bianchi@example.com");

        assertEquals("Luigi",                       user.getFirstName());
        assertEquals("Bianchi",                     user.getLastName());
        assertEquals("luigi.bianchi@example.com",   user.getEmail());
        // code resta immutato
        assertEquals(CODE, user.getCode());
    }

    // ---------------------------------------------------------------------
    // addLoan / removeLoan / hasActiveLoans
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("hasActiveLoans è false quando non ci sono prestiti")
    void hasActiveLoansFalseWhenEmpty() {
        assertFalse(user.hasActiveLoans());
    }

    @Test
    @DisplayName("addLoan aggiunge un prestito attivo e hasActiveLoans diventa true")
    void addLoanAddsLoanAndHasActiveLoansBecomesTrue() {
        assertFalse(user.hasActiveLoans());

        user.addLoan(null);

        assertTrue(user.hasActiveLoans());
        assertEquals(1, user.getActiveLoans().size());
    }

    @Test
    @DisplayName("removeLoan rimuove un prestito presente")
    void removeLoanRemovesExistingLoan() {
        // Aggiungo un prestito (null come placeholder)
        user.addLoan(null);
        assertEquals(1, user.getActiveLoans().size());
        assertTrue(user.hasActiveLoans());

        user.removeLoan(null);

        assertTrue(user.getActiveLoans().isEmpty());
        assertFalse(user.hasActiveLoans());
    }

    @Test
    @DisplayName("removeLoan su prestito non presente non altera la lista")
    void removeLoanOnNonExistingLoanKeepsListUnchanged() {
        // Lista vuota
        assertTrue(user.getActiveLoans().isEmpty());

        user.removeLoan(null); // non presente

        // Rimane vuota
        assertTrue(user.getActiveLoans().isEmpty());
    }

    // ---------------------------------------------------------------------
    // equals e hashCode
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("equals è riflessivo, gestisce null e classi diverse")
    void equalsBasicProperties() {
        assertEquals(user, user);          // riflessivo
        assertNotEquals(user, null);       // confronto con null
        assertNotEquals(user, "stringa");  // tipo diverso
    }

    @Test
    @DisplayName("Due User con lo stesso code sono uguali, anche se altri campi differiscono")
    void equalsSameCodeDifferentOtherFields() {
        User u1 = new User("A", "B", "a.b@example.com", "CODE-1");
        User u2 = new User("X", "Y", "x.y@example.com", "CODE-1");

        assertEquals(u1, u2);
        assertEquals(u1.hashCode(), u2.hashCode());
    }

    @Test
    @DisplayName("Due User con code diversi NON sono uguali")
    void equalsDifferentCodes() {
        User u1 = new User("A", "B", "a.b@example.com", "CODE-1");
        User u2 = new User("A", "B", "a.b@example.com", "CODE-2");

        assertNotEquals(u1, u2);
    }

    // ---------------------------------------------------------------------
    // toString
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("toString contiene le informazioni principali dell'utente")
    void toStringContainsMainInfo() {
        user.addLoan(null); // 1 prestito, giusto per il conteggio

        String s = user.toString();

        assertTrue(s.contains(CODE));
        assertTrue(s.contains(FIRST_NAME));
        assertTrue(s.contains(LAST_NAME));
        assertTrue(s.contains(EMAIL));

        // Deve contenere il numero di prestiti attivi (1 in questo caso)
        assertTrue(s.contains("Active Loans"));
        assertTrue(s.contains("1"));
    }
}