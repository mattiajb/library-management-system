/**
 * @file BookTest.java
 * @ingroup TestsModels
 * @brief Suite di test di unità per la classe di dominio Book.
 *
 * Verifica:
 * - corretta inizializzazione dei campi tramite costruttore e coerenza dei getter;
 * - uso di copie difensive per la lista degli autori;
 * - corretto funzionamento dei metodi di business relativi alle copie disponibili
 * - proprietà di equals/hashCode basate sull'ISBN;
 * - presenza delle informazioni principali nella rappresentazione testuale (toString).
 */
package swe.group04.libraryms.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @brief Test di unità per Book
 *
 * I test riservano particolare attenzione agli invarianti relativi al numero di copie disponibili
 * e alle copie difensive sulle collezioni.
 *
 * @ingroup TestsModels
 */
class BookTest {

    //  Titolo utilizzato nei test
    private static final String TITLE = "Clean Code";
    
    //  Lista autori utilizzata nei test
    private static final List<String> AUTHORS = Arrays.asList("Robert C. Martin");
    
    //  Anno di pubblicazione utilizzato nei test
    private static final int RELEASE_YEAR = 2008;
    
    //  ISBN utilizzato nei test
    private static final String ISBN = "9780132350884";
    
    //  Numero totale di copie utilizzato nei test. */
    private static final int TOTAL_COPIES = 3;

    //  Istanza di Book inizializzata prima di ogni test.
    private Book book;

    /**
     * @brief Inizializza un oggetto Book valido prima di ogni test.
     *
     * @details
     * Garantisce isolamento tra i test: ogni test opera su un'istanza nuova.
     */
    @BeforeEach
    void setUp() {
        book = new Book(TITLE, AUTHORS, RELEASE_YEAR, ISBN, TOTAL_COPIES);
    }

    // ---------------------------------------------------------------------
    //                      Costruttore e getter di base
    // ---------------------------------------------------------------------

    /**
     * @brief Controlla la coerenza tra valori passati al costruttore e getter.
     * 
     * @post Le proprietà di Book coincidono con i valori attesi.
     */
    @Test
    @DisplayName("Il costruttore inizializza correttamente tutti i campi")
    void constructorInitializesFieldsCorrectly() {
        assertEquals(TITLE, book.getTitle());
        assertEquals(AUTHORS, book.getAuthors());
        assertEquals(RELEASE_YEAR, book.getReleaseYear());
        assertEquals(ISBN, book.getIsbn());
        assertEquals(TOTAL_COPIES, book.getTotalCopies());
        assertEquals(TOTAL_COPIES, book.getAvailableCopies());
    }

    /**
     * @brief La modifica della lista restituita non deve alterare lo stato interno.
     * 
     * @post La lista interna degli autori non cambia se si modifica la copia.
     */
    @Test
    @DisplayName("getAuthors restituisce una copia difensiva della lista")
    void getAuthorsReturnsDefensiveCopy() {
        List<String> authorsFromBook = book.getAuthors();

        //  Modifico la lista restituita
        authorsFromBook.add("Altro Autore");

        //  La lista interna non deve cambiare
        assertEquals(AUTHORS, book.getAuthors());
        assertNotSame(AUTHORS, book.getAuthors());
    }

    // ---------------------------------------------------------------------
    //                      Setter e copie difensive
    // ---------------------------------------------------------------------

    /**
     * @brief Dopo setAuthors, modificare la lista passata non deve influire sul Book.
     * 
     * @post La lista interna contiene solo gli autori presenti al momento della chiamata.
     */
    @Test
    @DisplayName("setAuthors sostituisce gli autori usando una copia difensiva")
    void setAuthorsUsesDefensiveCopy() {
        List<String> newAuthors = new ArrayList<>();
        newAuthors.add("Primo Autore");

        book.setAuthors(newAuthors);

        // La lista interna ha il contenuto atteso
        assertEquals(newAuthors, book.getAuthors());

        // Modifico la lista passata come parametro
        newAuthors.add("Secondo Autore");

        // La lista interna non deve cambiare
        assertEquals(1, book.getAuthors().size());
        assertEquals("Primo Autore", book.getAuthors().get(0));
    }

    /**
     * @brief Controlla la modifica di titolo e anno di pubblicazione.
     * 
     * @post I getter restituiscono i nuovi valori impostati.
     */
    @Test
    @DisplayName("I setter semplici aggiornano correttamente i campi di base")
    void settersUpdateBasicFields() {
        book.setTitle("Nuovo Titolo");
        book.setReleaseYear(2020);

        assertEquals("Nuovo Titolo", book.getTitle());
        assertEquals(2020, book.getReleaseYear());
    }

    /**
     * @note
     * Non vengono testate qui violazioni volontarie degli invarianti tramite
     * setTotalCopies/setAvailableCopies poiché rappresentano un uso fuori contratto
     * da parte del chiamante.
     */

    // ---------------------------------------------------------------------
    //                      hasAvailableCopies
    // ---------------------------------------------------------------------

    /**
     * @brief Verifica che hasAvailableCopies() sia true se availableCopies > 0.
     */
    @Test
    @DisplayName("hasAvailableCopies è true quando availableCopies > 0")
    void hasAvailableCopiesTrueWhenGreaterThanZero() {
        book.setAvailableCopies(1);
        assertTrue(book.hasAvailableCopies());
    }

    /**
     * @brief Verifica che hasAvailableCopies() sia false se availableCopies == 0.
     */
    @Test
    @DisplayName("hasAvailableCopies è false quando availableCopies == 0")
    void hasAvailableCopiesFalseWhenZero() {
        book.setAvailableCopies(0);
        assertFalse(book.hasAvailableCopies());
    }

    // ---------------------------------------------------------------------
    //                      decrementAvailableCopies
    // ---------------------------------------------------------------------

    /**
     * @brief Verifica che decrementAvailableCopies() decrementi di 1 quando le copie sono disponibili.
     *
     * @pre availableCopies > 0
     * @post availableCopies == availableCopies@pre - 1
     */
    @Test
    @DisplayName("decrementAvailableCopies diminuisce di 1 se ci sono copie disponibili")
    void decrementAvailableCopiesWhenPositive() {
        book.setAvailableCopies(2);
        book.decrementAvailableCopies();
        assertEquals(1, book.getAvailableCopies());
    }

    /**
     * @brief Verifica che decrementAvailableCopies() lanci eccezione a 0 copie disponibili.
     *
     * @pre availableCopies == 0
     * @post Lo stato rimane invariato.
     */
    @Test
    @DisplayName("decrementAvailableCopies lancia IllegalStateException se availableCopies == 0")
    void decrementAvailableCopiesThrowsWhenZero() {
        book.setAvailableCopies(0);

        assertThrows(IllegalStateException.class, () -> book.decrementAvailableCopies());
        // Lo stato rimane invariato
        assertEquals(0, book.getAvailableCopies());
    }

    // ---------------------------------------------------------------------
    //                      incrementAvailableCopies
    // ---------------------------------------------------------------------

    /**
     * @brief Verifica che incrementAvailableCopies() incrementi di 1 se availableCopies < totalCopies.
     *
     * @pre availableCopies < totalCopies
     * @post availableCopies == availableCopies@pre + 1
     */
    @Test
    @DisplayName("incrementAvailableCopies aumenta di 1 se availableCopies < totalCopies")
    void incrementAvailableCopiesWhenLessThanTotal() {
        //  Caso limite: totalCopies = 3, availableCopies = 2
        book.setAvailableCopies(TOTAL_COPIES - 1); // 2
        book.incrementAvailableCopies();
        assertEquals(TOTAL_COPIES, book.getAvailableCopies());
    }

    /**
     * @brief Verifica che incrementAvailableCopies() lanci eccezione se availableCopies == totalCopies.
     *
     * @pre availableCopies == totalCopies
     * @post Lo stato rimane invariato.
     */
    @Test
    @DisplayName("incrementAvailableCopies lancia IllegalStateException se availableCopies == totalCopies")
    void incrementAvailableCopiesThrowsWhenAtTotal() {
        book.setAvailableCopies(TOTAL_COPIES); // già al massimo

        assertThrows(IllegalStateException.class, () -> book.incrementAvailableCopies());
        //  Lo stato rimane invariato
        assertEquals(TOTAL_COPIES, book.getAvailableCopies());
    }

    /**
     * @brief Verifica che una sequenza di operazioni lecite mantenga gli invarianti sulle copie.
     *
     * @brief AvailableCopies deve rimanere nel range [0, totalCopies].
     */
    @Test
    @DisplayName("Sequenza di increment/decrement mantiene availableCopies tra 0 e totalCopies")
    void incrementDecrementSequencePreservesInvariants() {
        //  Stato iniziale: available = totalCopies
        assertEquals(TOTAL_COPIES, book.getAvailableCopies());

        //  Decrementare fino a 0, usando solo chiamate lecite
        while (book.getAvailableCopies() > 0) {
            book.decrementAvailableCopies();
        }
        assertEquals(0, book.getAvailableCopies());

        //  Incrementare fino a totalCopies
        while (book.getAvailableCopies() < book.getTotalCopies()) {
            book.incrementAvailableCopies();
        }
        assertEquals(book.getTotalCopies(), book.getAvailableCopies());
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
        assertEquals(book, book);              // riflessivo
        assertNotEquals(book, null);           // null
        assertNotEquals(book, "stringa");      // tipo diverso
    }

    /**
     * @brief Verifica l'uguaglianza tra due Book, con identificativo ISBN identico
     * 
     * @post hashCode coerente con equals.
     */
    @Test
    @DisplayName("Due Book con lo stesso ISBN sono uguali, anche con altri campi diversi")
    void equalsSameIsbnDifferentOtherFields() {
        Book b1 = new Book("T1", List.of("A1"), 2000, "ISBN-123", 1);
        Book b2 = new Book("T2", List.of("A2"), 2010, "ISBN-123", 5);

        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
    }

    /**
     * @brief Verifica la disuguaglianza tra due Book, con identificativi ISBN diversi
     */
    @Test
    @DisplayName("Due Book con ISBN diverso NON sono uguali")
    void equalsDifferentIsbn() {
        Book b1 = new Book("T1", List.of("A1"), 2000, "ISBN-1", 1);
        Book b2 = new Book("T2", List.of("A2"), 2010, "ISBN-2", 1);

        assertNotEquals(b1, b2);
    }

    // ---------------------------------------------------------------------
    //                      toString
    // ---------------------------------------------------------------------

    /**
     * @brief Controlla presenza di titolo, ISBN, anno e dati copie.
     */
    @Test
    @DisplayName("toString contiene le informazioni principali del libro")
    void toStringContainsMainInfo() {
        String s = book.toString();

        assertTrue(s.contains(TITLE));
        assertTrue(s.contains(ISBN));
        assertTrue(s.contains(String.valueOf(RELEASE_YEAR)));
        assertTrue(s.contains(String.valueOf(TOTAL_COPIES)));
        assertTrue(s.contains(String.valueOf(book.getAvailableCopies())));
    }
}