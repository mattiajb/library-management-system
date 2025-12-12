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

class LibraryArchiveServiceTest {

    /** Fake in-memory: niente file. */
    private static class InMemoryArchiveFileService extends ArchiveFileService {
        private LibraryArchive stored;

        InMemoryArchiveFileService() {
            super("IGNORED.bin", new FileService());
        }

        @Override
        public LibraryArchive loadArchive() throws IOException {
            return stored;
        }

        @Override
        public void saveArchive(LibraryArchive archive) throws IOException {
            stored = archive;
        }
    }

    private LibraryArchiveService service;
    private InMemoryArchiveFileService afs;

    @BeforeEach
    void setUp() {
        afs = new InMemoryArchiveFileService();
        service = new LibraryArchiveService(afs);
    }

    @Test
    @DisplayName("Ctor: ArchiveFileService null -> IllegalArgumentException")
    void ctorArchiveFileServiceNullThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new LibraryArchiveService((ArchiveFileService) null));
    }

    @Test
    @DisplayName("Ctor: LibraryArchive null -> IllegalArgumentException")
    void ctorLibraryArchiveNullThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new LibraryArchiveService((LibraryArchive) null));
    }

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

    @Test
    @DisplayName("saveArchive: null -> IllegalArgumentException")
    void saveArchiveNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.saveArchive(null));
    }

    @Test
    @DisplayName("saveArchive: salva in memoria e aggiorna riferimento interno")
    void saveArchiveStoresInMemory() throws IOException {
        LibraryArchive a = new LibraryArchive();

        service.saveArchive(a);

        assertSame(a, service.getLibraryArchive());
        assertSame(a, afs.loadArchive()); // conferma che è finito nella fake persistence
    }

    @Test
    @DisplayName("loadArchive: se persistence ritorna null -> crea archivio vuoto")
    void loadArchiveWhenNullCreatesEmpty() throws IOException {
        // stored = null
        LibraryArchive loaded = service.loadArchive();

        assertNotNull(loaded);
        assertTrue(loaded.getBooks().isEmpty());
        assertTrue(loaded.getUsers().isEmpty());
        assertTrue(loaded.getLoans().isEmpty());
        assertSame(loaded, service.getLibraryArchive());
    }

    @Test
    @DisplayName("loadArchive: se persistence ha archivio -> lo carica e lo imposta")
    void loadArchiveLoadsExisting() throws IOException {
        LibraryArchive pre = new LibraryArchive();
        afs.saveArchive(pre);

        LibraryArchive loaded = service.loadArchive();

        assertSame(pre, loaded);
        assertSame(pre, service.getLibraryArchive());
    }

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