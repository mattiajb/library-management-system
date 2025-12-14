/**
 * @file ServiceLocator.java
 * @brief Service Locator per l'accesso centralizzato ai servizi applicativi.
 *
 * Fornisce punti di accesso globali ai servizi di applicazione
 *
 * Scelte architetturali:
 * - I servizi vengono istanziati una sola volta (static final) e condivisi.
 * - Tutti i service specifici condividono la stessa istanza di LibraryArchiveService,
 *   quindi operano sul medesimo oggetto LibraryArchive.
 *
 * @note Il caricamento da file dell'archivio non avviene qui: viene
 *       invocato all'avvio dell'applicazione (es. nel controller principale)
 *       tramite LibraryArchiveService::loadArchive().
 */
package swe.group04.libraryms.service;

import swe.group04.libraryms.models.LibraryArchive;

public class ServiceLocator {

    private static final LibraryArchiveService archiveService =
            new LibraryArchiveService(new LibraryArchive());

    private static final BookService bookService =
            new BookService(archiveService);

    private static final UserService userService =
            new UserService(archiveService);

    private static final LoanService loanService =
            new LoanService(archiveService);

    public static LibraryArchiveService getArchiveService() {
        return archiveService;
    }

    public static BookService getBookService() {
        return bookService;
    }

    public static UserService getUserService() {
        return userService;
    }

    public static LoanService getLoanService() {
        return loanService;
    }
}