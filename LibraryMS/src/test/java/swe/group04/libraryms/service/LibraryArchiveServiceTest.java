/**
 * @file LibraryArchiveServiceTest.java
 * @ingroup TestsService
 * @brief Suite di test di unità per la classe LibraryArchiveService.
 *
 * Verifica:
 * - validazione dei costruttori;
 * - inizializzazione lazy dell'archivio;
 * - salvataggio e caricamento dell'archivio;
 * - gestione delle eccezioni di I/O (IOException e FileNotFoundException);
 * - coerenza tra archivio persistito e riferimento interno del servizio.
 *
 * @note Tutti i test utilizzano una persistenza fittizia in memoria
 *       per evitare l'accesso al file system reale.
 */
package swe.group04.libraryms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import swe.group04.libraryms.models.LibraryArchive;
import swe.group04.libraryms.persistence.ArchiveFileService;
import swe.group04.libraryms.persistence.FileService;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @brief Test di unità per LibraryArchiveService.
 * 
 * I test verificano il corretto comportamento del servizio
 * responsabile dell’accesso e della persistenza dell’archivio della biblioteca.
 * 
 * @ingroup TestsService
 */
class LibraryArchiveServiceTest {

    /**
     * @brief Implementazione fittizia di ArchiveFileService in memoria.
     *
     * Permette di simulare il comportamento della persistenza
     * senza accedere al file system.
     */
    private static class InMemoryArchiveFileService extends ArchiveFileService {

        /// Riferimento all'archivio memorizzato
        private LibraryArchive stored;

        /**
         * @brief Costruttore del servizio fittizio.
         */
        InMemoryArchiveFileService() {
            super("IGNORED.bin", new FileService());
        }

        /**
         * @brief Restituisce l'archivio memorizzato in memoria.
         */
        @Override
        public LibraryArchive loadArchive() throws IOException {
            return stored;
        }

        /**
         * @brief Salva l'archivio in memoria.
         */
        @Override
        public void saveArchive(LibraryArchive archive) throws IOException {
            stored = archive;
        }
    }

    /// Service sotto test.
    private LibraryArchiveService service;

    /// Servizio di persistenza fittizio.
    private InMemoryArchiveFileService afs;

    /**
     * @brief Inizializza il contesto di test prima di ogni caso di prova.
     */
    @BeforeEach
    void setUp() {
        afs = new InMemoryArchiveFileService();
        service = new LibraryArchiveService(afs);
    }

    /**
     * @brief Verifica che il costruttore lanci IllegalArgumentException
     *        se ArchiveFileService è null.
     */
    @Test
    @DisplayName("Ctor: ArchiveFileService null -> IllegalArgumentException")
    void ctorArchiveFileServiceNullThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new LibraryArchiveService((ArchiveFileService) null));
    }

    /**
     * @brief Verifica che il costruttore lanci IllegalArgumentException
     *        se LibraryArchive è null.
     */
    @Test
    @DisplayName("Ctor: LibraryArchive null -> IllegalArgumentException")
    void ctorLibraryArchiveNullThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new LibraryArchiveService((LibraryArchive) null));
    }

    /**
     * @brief Verifica che getLibraryArchive inizializzi un archivio vuoto
     *        se non ancora presente e ne mantenga il riferimento.
     */
    @Test
    @DisplayName("getLibraryArchive: inizializza archivio vuoto se null")
    void getLibraryArchiveInitializes() {
        LibraryArchive a1 = service.getLibraryArchive();
        assertNotNull(a1);
        assertTrue(a1.getBooks().isEmpty());
        assertTrue(a1.getUsers().isEmpty());
        assertTrue(a1.getLoans().isEmpty());

        LibraryArchive a2 = service.getLibraryArchive();
        assertSame(a1, a2);
    }

    /**
     * @brief Verifica che saveArchive lanci IllegalArgumentException
     *        se l'archivio passato è null.
     */
    @Test
    @DisplayName("saveArchive: null -> IllegalArgumentException")
    void saveArchiveNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.saveArchive(null));
    }

    /**
     * @brief Verifica che saveArchive salvi correttamente l'archivio
     *        e aggiorni il riferimento interno del servizio.
     */
    @Test
    @DisplayName("saveArchive: salva in memoria e aggiorna riferimento interno")
    void saveArchiveStoresInMemory() throws IOException {
        LibraryArchive a = new LibraryArchive();

        service.saveArchive(a);

        assertSame(a, service.getLibraryArchive());
        assertSame(a, afs.loadArchive());
    }

    /**
     * @brief Verifica che loadArchive crei un archivio vuoto
     *        se la persistenza restituisce null.
     */
    @Test
    @DisplayName("loadArchive: se persistence ritorna null -> crea archivio vuoto")
    void loadArchiveWhenNullCreatesEmpty() throws IOException {
        LibraryArchive loaded = service.loadArchive();

        assertNotNull(loaded);
        assertTrue(loaded.getBooks().isEmpty());
        assertTrue(loaded.getUsers().isEmpty());
        assertTrue(loaded.getLoans().isEmpty());
        assertSame(loaded, service.getLibraryArchive());
    }

    /**
     * @brief Verifica che loadArchive carichi correttamente
     *        un archivio esistente dalla persistenza.
     */
    @Test
    @DisplayName("loadArchive: se persistence ha archivio -> lo carica e lo imposta")
    void loadArchiveLoadsExisting() throws IOException {
        LibraryArchive pre = new LibraryArchive();
        afs.saveArchive(pre);

        LibraryArchive loaded = service.loadArchive();

        assertSame(pre, loaded);
        assertSame(pre, service.getLibraryArchive());
    }

    /**
     * @brief Verifica che loadArchive rilanci IOException
     *        se non si tratta di FileNotFoundException.
     */
    @Test
    @DisplayName("loadArchive: rilancia IOException non FileNotFoundException")
    void loadArchiveRethrowsGenericIOException() {
        ArchiveFileService failing = new InMemoryArchiveFileService() {
            @Override
            public LibraryArchive loadArchive() throws IOException {
                throw new IOException("Errore I/O generico");
            }
        };
        LibraryArchiveService s = new LibraryArchiveService(failing);

        assertThrows(IOException.class, s::loadArchive);
    }

    /**
     * @brief Verifica che loadArchive gestisca FileNotFoundException
     *        creando un nuovo archivio vuoto.
     */
    @Test
    @DisplayName("loadArchive: FileNotFoundException -> crea archivio vuoto")
    void loadArchiveFileNotFoundCreatesEmpty() throws IOException {
        ArchiveFileService failing = new InMemoryArchiveFileService() {
            @Override
            public LibraryArchive loadArchive() throws IOException {
                throw new FileNotFoundException("missing");
            }
        };
        LibraryArchiveService s = new LibraryArchiveService(failing);

        LibraryArchive loaded = s.loadArchive();
        assertNotNull(loaded);
        assertTrue(loaded.getBooks().isEmpty());
        assertTrue(loaded.getUsers().isEmpty());
        assertTrue(loaded.getLoans().isEmpty());
    }
}