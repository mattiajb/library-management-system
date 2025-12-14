/**
 * @file FileServiceTest.java
 * @ingroup TestsPersistence
 * @brief Suite di test di unità per la classe FileService.
 *
 * Verifica:
 * - la corretta scrittura e lettura di oggetti semplici serializzabili (String);
 * - la corretta gestione di collezioni serializzabili (List);
 * - il corretto sollevamento di eccezioni in caso di lettura da file inesistenti.
 */
package swe.group04.libraryms.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @brief Suite di test per FileService.
 * 
 * I test operano su file temporanei creati durante l'esecuzione e
 * rimossi al termine di ciascun caso di prova.
 * 
 * @ingroup TestsPersistence
 */
class FileServiceTest {

    private FileService fileService;

    //  Percorso del file di test utilizzato nei casi di prova
    private String testFilePath;

    /**
     * @brief Inizializza il servizio e prepara il file di test.
     *
     * Prima di ogni test viene istanziato un nuovo FileService
     * e viene garantita la rimozione di eventuali file residui
     * da esecuzioni precedenti.
     */
    @BeforeEach
    void setUp() {
        fileService = new FileService();
        testFilePath = "fileServiceTest.bin";

        //  pulizia preventiva se il file esiste già
        File f = new File(testFilePath);
        if (f.exists()) {
            //  ignoriamo l'esito, è solo best effort
            f.delete();
        }
    }

    /**
     * @brief Rimuove il file di test al termine di ogni esecuzione.
     *
     * Garantisce che i test siano indipendenti tra loro e
     * non lascino effetti collaterali sul file system.
     */
    @AfterEach
    void tearDown() {
        File f = new File(testFilePath);
        if (f.exists()) {
            f.delete();
        }
    }

    /**
     * @brief Verifica la persistenza di una stringa tramite serializzazione.
     *
     * Il test scrive una stringa su file e la rilegge,
     * controllando che l'oggetto deserializzato coincida
     * con quello originale.
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
     * @brief Verifica la persistenza di una collezione serializzabile.
     *
     * Il test utilizza una List<String>, verificando che
     * la serializzazione e deserializzazione mantengano
     * correttamente il contenuto della collezione.
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
     * @brief Verifica che la lettura da un file inesistente generi un errore.
     *
     * Il test controlla che FileService sollevi un'eccezione
     * di tipo IOException quando si tenta di leggere
     * da un file che non esiste.
     */
    @Test
    void readFromFile_nonExistingFile_shouldThrowIOException() {
        String nonExistingPath = "this-file-does-not-exist.bin";

        assertThrows(IOException.class, () -> fileService.readFromFile(nonExistingPath));
    }
}