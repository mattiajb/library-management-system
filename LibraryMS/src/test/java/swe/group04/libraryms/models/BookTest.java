package swe.group04.libraryms.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookTest {

    private static final String TITLE = "Clean Code";
    private static final List<String> AUTHORS =
            Arrays.asList("Robert C. Martin");
    private static final int RELEASE_YEAR = 2008;
    private static final String ISBN = "9780132350884";
    private static final int TOTAL_COPIES = 3;

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book(TITLE, AUTHORS, RELEASE_YEAR, ISBN, TOTAL_COPIES);
    }

    // ---------------------------------------------------------------------
    // Costruttore e getter di base
    // ---------------------------------------------------------------------

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

    @Test
    @DisplayName("getAuthors restituisce una copia difensiva della lista")
    void getAuthorsReturnsDefensiveCopy() {
        List<String> authorsFromBook = book.getAuthors();

        // Modifico la lista restituita
        authorsFromBook.add("Altro Autore");

        // La lista interna non deve cambiare
        assertEquals(AUTHORS, book.getAuthors());
        assertNotSame(AUTHORS, book.getAuthors());
    }

    // ---------------------------------------------------------------------
    // Setter e copie difensive
    // ---------------------------------------------------------------------

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

    @Test
    @DisplayName("I setter semplici aggiornano correttamente i campi di base")
    void settersUpdateBasicFields() {
        book.setTitle("Nuovo Titolo");
        book.setReleaseYear(2020);

        assertEquals("Nuovo Titolo", book.getTitle());
        assertEquals(2020, book.getReleaseYear());
    }

    // NOTA: non testiamo qui la violazione volontaria degli invarianti
    // tramite setTotalCopies/setAvailableCopies, perché sarebbe
    // comportamento fuori contratto lato client.

    // ---------------------------------------------------------------------
    // hasAvailableCopies
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("hasAvailableCopies è true quando availableCopies > 0")
    void hasAvailableCopiesTrueWhenGreaterThanZero() {
        book.setAvailableCopies(1);
        assertTrue(book.hasAvailableCopies());
    }

    @Test
    @DisplayName("hasAvailableCopies è false quando availableCopies == 0")
    void hasAvailableCopiesFalseWhenZero() {
        book.setAvailableCopies(0);
        assertFalse(book.hasAvailableCopies());
    }

    // ---------------------------------------------------------------------
    // decrementAvailableCopies
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("decrementAvailableCopies diminuisce di 1 se ci sono copie disponibili")
    void decrementAvailableCopiesWhenPositive() {
        book.setAvailableCopies(2);
        book.decrementAvailableCopies();
        assertEquals(1, book.getAvailableCopies());
    }

    @Test
    @DisplayName("decrementAvailableCopies lancia IllegalStateException se availableCopies == 0")
    void decrementAvailableCopiesThrowsWhenZero() {
        book.setAvailableCopies(0);

        assertThrows(IllegalStateException.class, () -> book.decrementAvailableCopies());
        // Lo stato rimane invariato
        assertEquals(0, book.getAvailableCopies());
    }

    // ---------------------------------------------------------------------
    // incrementAvailableCopies
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("incrementAvailableCopies aumenta di 1 se availableCopies < totalCopies")
    void incrementAvailableCopiesWhenLessThanTotal() {
        // Caso limite: totalCopies = 3, availableCopies = 2
        book.setAvailableCopies(TOTAL_COPIES - 1); // 2
        book.incrementAvailableCopies();
        assertEquals(TOTAL_COPIES, book.getAvailableCopies());
    }

    @Test
    @DisplayName("incrementAvailableCopies lancia IllegalStateException se availableCopies == totalCopies")
    void incrementAvailableCopiesThrowsWhenAtTotal() {
        book.setAvailableCopies(TOTAL_COPIES); // già al massimo

        assertThrows(IllegalStateException.class, () -> book.incrementAvailableCopies());
        // Lo stato rimane invariato
        assertEquals(TOTAL_COPIES, book.getAvailableCopies());
    }

    // ---------------------------------------------------------------------
    // Verifica che la sequenza di increment/decrement rispetti gli invarianti
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Sequenza di increment/decrement mantiene availableCopies tra 0 e totalCopies")
    void incrementDecrementSequencePreservesInvariants() {
        // Stato iniziale: available = totalCopies
        assertEquals(TOTAL_COPIES, book.getAvailableCopies());

        // Decremento fino a 0 (uso solo chiamate lecite)
        while (book.getAvailableCopies() > 0) {
            book.decrementAvailableCopies();
        }
        assertEquals(0, book.getAvailableCopies());

        // Incremento fino a totalCopies
        while (book.getAvailableCopies() < book.getTotalCopies()) {
            book.incrementAvailableCopies();
        }
        assertEquals(book.getTotalCopies(), book.getAvailableCopies());
    }

    // ---------------------------------------------------------------------
    // equals e hashCode
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("equals è riflessivo, gestisce null e classi diverse")
    void equalsBasicProperties() {
        assertEquals(book, book);              // riflessivo
        assertNotEquals(book, null);           // null
        assertNotEquals(book, "stringa");      // tipo diverso
    }

    @Test
    @DisplayName("Due Book con lo stesso ISBN sono uguali, anche con altri campi diversi")
    void equalsSameIsbnDifferentOtherFields() {
        Book b1 = new Book("T1", List.of("A1"), 2000, "ISBN-123", 1);
        Book b2 = new Book("T2", List.of("A2"), 2010, "ISBN-123", 5);

        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
    }

    @Test
    @DisplayName("Due Book con ISBN diverso NON sono uguali")
    void equalsDifferentIsbn() {
        Book b1 = new Book("T1", List.of("A1"), 2000, "ISBN-1", 1);
        Book b2 = new Book("T2", List.of("A2"), 2010, "ISBN-2", 1);

        assertNotEquals(b1, b2);
    }

    // ---------------------------------------------------------------------
    // toString
    // ---------------------------------------------------------------------

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