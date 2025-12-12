package swe.group04.libraryms.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import swe.group04.libraryms.models.Book;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServiceLocatorTest {

    @Test
    @DisplayName("I getter non restituiscono null")
    void gettersReturnNonNullServices() {
        assertNotNull(ServiceLocator.getArchiveService());
        assertNotNull(ServiceLocator.getBookService());
        assertNotNull(ServiceLocator.getUserService());
        assertNotNull(ServiceLocator.getLoanService());
    }

    @Test
    @DisplayName("I getter restituiscono sempre la stessa istanza (singleton statico)")
    void gettersReturnSameInstances() {
        assertSame(ServiceLocator.getArchiveService(), ServiceLocator.getArchiveService());
        assertSame(ServiceLocator.getBookService(), ServiceLocator.getBookService());
        assertSame(ServiceLocator.getUserService(), ServiceLocator.getUserService());
        assertSame(ServiceLocator.getLoanService(), ServiceLocator.getLoanService());
    }

    @Test
    @DisplayName("I service condividono lo stesso archivio (wiring corretto)")
    void servicesShareSameArchive() throws Exception {
        var archiveService = ServiceLocator.getArchiveService();
        var bookService = ServiceLocator.getBookService();

        int initialBooks = archiveService.getLibraryArchive().getBooks().size();

        Book b = new Book(
                "Test Shared Archive",
                List.of("Author"),
                2020,
                "1234567890",
                1
        );

        bookService.addBook(b);

        // se BookService e ArchiveService puntano allo stesso archivio, qui deve vedersi l'inserimento
        assertEquals(initialBooks + 1, archiveService.getLibraryArchive().getBooks().size());
        assertNotNull(archiveService.getLibraryArchive().findBookByIsbn("1234567890"));
    }
}
