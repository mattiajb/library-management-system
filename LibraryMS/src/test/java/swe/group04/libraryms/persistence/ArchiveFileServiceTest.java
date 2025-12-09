package swe.group04.libraryms.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import swe.group04.libraryms.models.LibraryArchive;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @file ArchiveFileServiceTest.java
 * @brief Test di unità per la classe ArchiveFileService.
 *
 * I test verificano il corretto salvataggio e caricamento di un
 * oggetto LibraryArchive tramite FileService.
 */
class ArchiveFileServiceTest {

    private FileService fileService;
    private ArchiveFileService archiveFileService;

    /** Percorso del file di test usato nei casi di prova. */
    private String testFilePath;

    /**
     * @brief Inizializza i servizi e il percorso del file di test.
     */
    @BeforeEach
    void setUp() {
        fileService = new FileService();
        testFilePath = "archiveTest.bin";

        // pulizia preventiva
        File f = new File(testFilePath);
        if (f.exists()) {
            f.delete();
        }

        archiveFileService = new ArchiveFileService(testFilePath, fileService);
    }

    /**
     * @brief Elimina il file di test alla fine di ogni esecuzione.
     */
    @AfterEach
    void tearDown() {
        File f = new File(testFilePath);
        if (f.exists()) {
            f.delete();
        }
    }

    /**
     * @brief Verifica che un LibraryArchive serializzato venga caricato correttamente.
     *
     * Scrive un LibraryArchive su file tramite FileService, poi usa
     * ArchiveFileService.loadArchive() per rileggerlo.
     */
    @Test
    void loadArchive_shouldReturnLibraryArchiveWhenFileContainsValidArchive() throws IOException {
        // arrange: creo un archivio e lo scrivo su file
        LibraryArchive original = new LibraryArchive();
        fileService.writeToFile(testFilePath, original);

        // act
        LibraryArchive loaded = archiveFileService.loadArchive();

        // assert
        assertNotNull(loaded);
        assertTrue(loaded instanceof LibraryArchive);
    }

    /**
     * @brief Verifica che un contenuto non valido causi un'IOException.
     *
     * Scrive nel file un oggetto che NON è un LibraryArchive e controlla
     * che loadArchive() lanci un'eccezione.
     */
    @Test
    void loadArchive_withWrongObjectType_shouldThrowIOException() throws IOException {
        // arrange: scrivo nel file una stringa, non un LibraryArchive
        String wrongData = "not a LibraryArchive";
        fileService.writeToFile(testFilePath, wrongData);

        // act + assert
        assertThrows(IOException.class, () -> archiveFileService.loadArchive());
    }

    /**
     * @brief Verifica che saveArchive scriva correttamente un LibraryArchive su file.
     *
     * Salva un archivio tramite ArchiveFileService e lo rilegge tramite FileService.
     */
    @Test
    void saveArchive_shouldWriteLibraryArchiveToFile() throws IOException, ClassNotFoundException {
        // arrange
        LibraryArchive archive = new LibraryArchive();

        // act: salvataggio tramite ArchiveFileService
        archiveFileService.saveArchive(archive);

        // assert: rilettura diretta dal file usando FileService
        Object read = fileService.readFromFile(testFilePath);
        assertNotNull(read);
        assertTrue(read instanceof LibraryArchive);
    }
}