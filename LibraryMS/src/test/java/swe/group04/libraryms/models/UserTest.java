/**
 * @file UserTest.java
 * @ingroup TestsModels
 * @brief Suite di test di unità per la classe di dominio User.
 *
 * Verifica:
 * - corretta inizializzazione dei campi tramite costruttore e coerenza dei getter;
 * - uso di copie difensive per la lista dei prestiti attivi;
 * - corretto comportamento dei metodi di gestione prestiti (addLoan, removeLoan, hasActiveLoans);
 * - proprietà di equals/hashCode basate sul codice identificativo (matricola);
 * - presenza delle informazioni principali nella rappresentazione testuale (toString).
 */
package swe.group04.libraryms.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @brief Test di unità per User
 *
 * I test validano i comportamenti osservabili dell'utente, con attenzione alle copie difensive
 * sulla lista dei prestiti attivi e alle proprietà di identità basate sul campo.
 *
 * @ingroup TestsModels
 */
class UserTest {

    //  Nome utilizzato nei test.
    private static final String FIRST_NAME = "Mario";
    //  Cognome utilizzato nei test.
    private static final String LAST_NAME  = "Rossi";
    //  Email utilizzata nei test.
    private static final String EMAIL      = "mario.rossi@example.com";
    //  Codice identificativo (matricola) utilizzato nei test. */
    private static final String CODE       = "S123456";

    //  Istanza di User inizializzata prima di ogni test.
    private User user;

    /**
     * @brief Inizializza un oggetto User valido prima di ogni test.
     *
     * Garantisce isolamento tra i test: ogni test opera su un'istanza nuova.
     */
    @BeforeEach
    void setUp() {
        user = new User(FIRST_NAME, LAST_NAME, EMAIL, CODE);
    }

    // ---------------------------------------------------------------------
    //                      Costruttore e getter di base
    // ---------------------------------------------------------------------

    /**
     * @brief Verifica la coerenza tra valori passati al costruttore e getter.
     * 
     * @post La lista dei prestiti attivi è inizialmente vuota.
     */
    @Test
    @DisplayName("Il costruttore inizializza correttamente i campi di base e la lista prestiti")
    void constructorInitializesFieldsCorrectly() {
        assertEquals(FIRST_NAME, user.getFirstName());
        assertEquals(LAST_NAME,  user.getLastName());
        assertEquals(EMAIL,      user.getEmail());
        assertEquals(CODE,       user.getCode());

        //  Lista prestiti vuota all'inizio
        assertNotNull(user.getActiveLoans());
        assertTrue(user.getActiveLoans().isEmpty());
    }

    // ---------------------------------------------------------------------
    //                  getActiveLoans: copia difensiva
    // ---------------------------------------------------------------------

    /**
     * @brief Verifica che la copia restituita da getActiveLoans() non alteri lo stato interno.
     * 
     * Il test controlla sia che due invocazioni successive restituiscano liste distinte,
     * sia che operazioni sulla lista restituita non impattino la lista interna.
     */
    @Test
    @DisplayName("getActiveLoans restituisce una copia difensiva della lista interna")
    void getActiveLoansReturnsDefensiveCopy() {
        //  Stato iniziale: lista vuota
        List<Loan> loans1 = user.getActiveLoans();
        List<Loan> loans2 = user.getActiveLoans();

        //  Le due liste devono essere istanze diverse
        assertNotSame(loans1, loans2);

        //  Simulazione di un prestito aggiunto dall'oggetto
        user.addLoan(null);  //< utilizzo di null come segnaposto

        List<Loan> loans3 = user.getActiveLoans();
        assertEquals(1, loans3.size());

        //  Modifica della lista restituita
        loans3.clear();

        //  Lo stato interno non deve cambiare
        assertEquals(1, user.getActiveLoans().size());
    }

    // ---------------------------------------------------------------------
    //                      setActiveLoans
    // ---------------------------------------------------------------------

    /**
     * @brief Verifica che setActiveLoans(List) gestisca il caso null.
     * 
     * Passando null la lista interna deve diventare vuota (non null).
     * 
     * @post getActiveLoans() restituisce una lista vuota.
     */
    @Test
    @DisplayName("setActiveLoans(null) imposta la lista dei prestiti a vuota")
    void setActiveLoansNullSetsEmptyList() {
        //  Pre-caricamento di alcuni prestiti
        user.addLoan(null);
        assertFalse(user.getActiveLoans().isEmpty());

        user.setActiveLoans(null);

        assertNotNull(user.getActiveLoans());
        assertTrue(user.getActiveLoans().isEmpty());
    }

    /**
     * @brief Verifica che setActiveLoans(List) usi una copia difensiva.
     *
     * Modificando la lista passata al setter non si deve alterare lo stato interno.
     */
    @Test
    @DisplayName("setActiveLoans usa una copia difensiva della lista passata")
    void setActiveLoansUsesDefensiveCopy() {
        List<Loan> givenList = new ArrayList<>();
        givenList.add(null); //<    segnaposto

        user.setActiveLoans(givenList);

        //  Controllo del contenuto iniziale
        List<Loan> internalList = user.getActiveLoans();
        assertEquals(1, internalList.size());

        //  Modifico la lista passata al setter
        givenList.clear();

        //  Lo stato interno non deve cambiare
        assertEquals(1, user.getActiveLoans().size());
    }

    // ---------------------------------------------------------------------
    //                  Setter base (firstName, lastName, email)
    // ---------------------------------------------------------------------

    /**
     * @brief Verifica che i setter anagrafici aggiornino correttamente i campi.
     *
     * Controlla che la matricola rimanga invariata.
     */
    @Test
    @DisplayName("I setter delle informazioni anagrafiche aggiornano correttamente i campi")
    void basicSettersUpdateFields() {
        user.setFirstName("Luigi");
        user.setLastName("Bianchi");
        user.setEmail("luigi.bianchi@example.com");

        assertEquals("Luigi",                       user.getFirstName());
        assertEquals("Bianchi",                     user.getLastName());
        assertEquals("luigi.bianchi@example.com",   user.getEmail());
        //  code resta immutato
        assertEquals(CODE, user.getCode());
    }

    // ---------------------------------------------------------------------
    //                  addLoan / removeLoan / hasActiveLoans
    // ---------------------------------------------------------------------

    /**
     * @brief Verifica che hasActiveLoans() sia false quando non ci sono prestiti.
     */
    @Test
    @DisplayName("hasActiveLoans è false quando non ci sono prestiti")
    void hasActiveLoansFalseWhenEmpty() {
        assertFalse(user.hasActiveLoans());
    }

    /**
     * @brief Verifica che dopo l'aggiunta di un prestito con addLoan(Loan) si aggiorni lo stato.
     *
     * Dopo l'aggiunta, hasActiveLoans deve diventare true e la dimensione aumentare.
     */
    @Test
    @DisplayName("addLoan aggiunge un prestito attivo e hasActiveLoans diventa true")
    void addLoanAddsLoanAndHasActiveLoansBecomesTrue() {
        assertFalse(user.hasActiveLoans());

        user.addLoan(null);

        assertTrue(user.hasActiveLoans());
        assertEquals(1, user.getActiveLoans().size());
    }

    /**
     * @brief Verifica che removeLoan(Loan) rimuova un prestito presente.
     *
     * Dopo la rimozione, la lista deve risultare vuota.
     */
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

    /**
     * @brief Verifica che removeLoan(Loan) non alteri lo stato se il prestito non è presente.
     */
    @Test
    @DisplayName("removeLoan su prestito non presente non altera la lista")
    void removeLoanOnNonExistingLoanKeepsListUnchanged() {
        //  Lista vuota
        assertTrue(user.getActiveLoans().isEmpty());

        user.removeLoan(null); // non presente

        //  Rimane vuota
        assertTrue(user.getActiveLoans().isEmpty());
    }

    // ---------------------------------------------------------------------
    //                      equals e hashCode
    // ---------------------------------------------------------------------

    /**
     * @brief Verifica proprietà base di equals: riflessività e gestione di null/tipi diversi.
     */
    @Test
    @DisplayName("equals è riflessivo, gestisce null e classi diverse")
    void equalsBasicProperties() {
        assertEquals(user, user);          // riflessivo
        assertNotEquals(user, null);       // confronto con null
        assertNotEquals(user, "stringa");  // tipo diverso
    }

    /**
     * @brief Verifica che due User con lo stesso code risultino uguali.
     *
     * L'uguaglianza è basata sul codice identificativo (matricola).
     * 
     * @post hashCode coerente con equals.
     */
    @Test
    @DisplayName("Due User con lo stesso code sono uguali, anche se altri campi differiscono")
    void equalsSameCodeDifferentOtherFields() {
        User u1 = new User("A", "B", "a.b@example.com", "CODE-1");
        User u2 = new User("X", "Y", "x.y@example.com", "CODE-1");

        assertEquals(u1, u2);
        assertEquals(u1.hashCode(), u2.hashCode());
    }

    /**
     * @brief Verifica che due User con code diverso non siano uguali.
     */
    @Test
    @DisplayName("Due User con code diversi NON sono uguali")
    void equalsDifferentCodes() {
        User u1 = new User("A", "B", "a.b@example.com", "CODE-1");
        User u2 = new User("A", "B", "a.b@example.com", "CODE-2");

        assertNotEquals(u1, u2);
    }

    // ---------------------------------------------------------------------
    //                      toString
    // ---------------------------------------------------------------------

    /**
     * @brief Verifica che toString() includa informazioni principali.
     *
     * Controlla presenza di matricola, nome, cognome, email e conteggio prestiti.
     */
    @Test
    @DisplayName("toString contiene le informazioni principali dell'utente")
    void toStringContainsMainInfo() {
        user.addLoan(null); // 1 prestito, giusto per il conteggio

        String s = user.toString();

        assertTrue(s.contains(CODE));
        assertTrue(s.contains(FIRST_NAME));
        assertTrue(s.contains(LAST_NAME));
        assertTrue(s.contains(EMAIL));

        //  Deve contenere il numero di prestiti attivi (1 in questo caso)
        assertTrue(s.contains("Active Loans"));
        assertTrue(s.contains("1"));
    }
}