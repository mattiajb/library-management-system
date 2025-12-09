package swe.group04.libraryms.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @file BookTest.java
 * @brief Classe di test di unità per Book.
 *
 * Il file contiene una serie di test automatici JUnit che verificano:
 * - corretta inizializzazione di un oggetto Book
 * - incapsulamento della lista di autori (copia difensiva)
 * - comportamento del metodo equals/hashCode e casi base di confronto
 */
class BookTest {

    private Book book;
    private List<String> authors;
    
    /**
     * @brief Inizializza i dati comuni prima di ogni test.
     */
    @BeforeEach
    void setUp() {
        authors = Arrays.asList("Author One", "Author Two");
        book = new Book("Test Title", authors, 2020, "ISBN-123", 5);
    }
    
    /**
     * @brief Verifica che il costruttore inizializzi correttamente tutti i campi.
     */
    @Test
    void constructor_shouldInitializeAllFieldsCorrectly() {
        assertEquals("Test Title", book.getTitle());
        assertEquals(2020, book.getReleaseYear());
        assertEquals("ISBN-123", book.getIsbn());
        assertEquals(5, book.getTotalCopies());
        assertEquals(5, book.getAvailableCopies());

        List<String> resultAuthors = book.getAuthors();
        assertEquals(authors, resultAuthors);
    }
    
    
    /**
     * @brief Verifica che getAuthors restituisca una copia difensiva della lista interna.
     */
    @Test
    void getAuthors_shouldReturnDefensiveCopy() {
        List<String> copy = book.getAuthors();

        // stessi contenuti
        assertEquals(authors, copy);
        // ma oggetto diverso
        assertNotSame(authors, copy);

        // se modifico la copia, il Book non deve cambiare
        copy.add("New Author");
        assertEquals(2, book.getAuthors().size());
    }
    
    /**
     * @brief Verifica che equals e hashCode dipendano solo dall'ISBN.
     */
    @Test
    void equalsAndHashCode_shouldDependOnlyOnIsbn() {
        Book sameIsbn = new Book(
                "Other Title",
                Arrays.asList("X"),
                2010,
                "ISBN-123",
                3
        );

        Book differentIsbn = new Book(
                "Other Title",
                Arrays.asList("X"),
                2010,
                "ISBN-999",
                3
        );

        assertEquals(book, sameIsbn);
        assertEquals(book.hashCode(), sameIsbn.hashCode());

        assertNotEquals(book, differentIsbn);
    }
    
    /**
     * @brief Verifica la proprietà riflessiva del metodo equals.
     */
    @Test
    void equals_shouldBeReflexive() {
        assertEquals(book, book);
    }
    
    /**
     * @brief Verifica equals con null e con un oggetto di tipo diverso.
     */
    @Test
    void equals_shouldReturnFalseForNullAndOtherType() {
        assertNotEquals(book, null);
        assertNotEquals(book, "not a book");
    }
}