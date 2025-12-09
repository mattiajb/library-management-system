package swe.group04.libraryms.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @file FileServiceTest.java
 * @brief Test di unità per la classe FileService.
 *
 * I test verificano il corretto funzionamento delle operazioni di
 * scrittura e lettura di oggetti serializzabili su file.
 */
class FileServiceTest {

    private FileService fileService;

    private String testFilePath;

    /**
     * @brief Inizializza il servizio e il percorso del file di test.
     */
    @BeforeEach
    void setUp() {
        fileService = new FileService();
        testFilePath = "fileServiceTest.bin";

        // pulizia preventiva se il file esiste già
        File f = new File(testFilePath);
        if (f.exists()) {
            // ignoriamo l'esito, è solo best effort
            f.delete();
        }
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
     * @brief Verifica che scrittura e lettura funzionino con una semplice stringa.
     *
     * Il test scrive una stringa su file e poi la rilegge, controllando che
     * il valore letto coincida con quello originale.
     */
    @Test
    void writeAndRead_shouldPersistStringValue() throws IOException {
        String original = "Hello, FileService!";

        fileService.writeToFile(testFilePath, original);
        Object read = fileService.readFromFile(testFilePath);

        assertNotNull(read);
        assertTrue(read instanceof String);
        assertEquals(original, read);
    }

    /**
     * @brief Verifica che scrittura e lettura funzionino con una lista serializzabile.
     *
     * Il test usa una List<String>, che è serializzabile, per verificare
     * la correttezza della serializzazione/deserializzazione.
     */
    @Test
    void writeAndRead_shouldPersistSerializableCollection() throws IOException {
        List<String> original = List.of("A", "B", "C");

        fileService.writeToFile(testFilePath, original);
        Object read = fileService.readFromFile(testFilePath);

        assertNotNull(read);
        assertTrue(read instanceof List);
        assertEquals(original, read);
    }

    /**
     * @brief Verifica che la lettura da un file inesistente causi un'eccezione di I/O.
     */
    @Test
    void readFromFile_nonExistingFile_shouldThrowIOException() {
        String nonExistingPath = "this-file-does-not-exist.bin";

        assertThrows(IOException.class, () -> fileService.readFromFile(nonExistingPath));
    }
}