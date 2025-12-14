/**
 * @file ArchiveFileService.java
 * @brief Servizio di persistenza per l'archivio della biblioteca.
 *
 * Questa classe appartiene al livello di persistenza dell'applicazione
 * ed è responsabile della serializzazione e deserializzazione
 * dell'oggetto.
 * Il servizio delega le operazioni di I/O a basso livello al FileService,
 * mantenendo separata la logica di accesso ai file dalla logica di dominio.
 *
 * @note Questa classe non esegue validazioni di business
 *       sull'archivio caricato o salvato.
 */
package swe.group04.libraryms.persistence;

import java.io.IOException;
import swe.group04.libraryms.models.LibraryArchive;

/**
 * @brief Implementa la persistenza dell'archivio tramite file.
 *
 */
public class ArchiveFileService {

    /** Percorso del file contenente l'archivio serializzato */
    private String archiveFilePath;

    /** Servizio di I/O per la gestione dei file */
    private FileService fileService;

    /**
     * @brief Costruisce un servizio di persistenza per l'archivio.
     *
     * @param archiveFilePath Percorso del file dell'archivio.
     * @param fileService     Servizio di I/O da utilizzare.
     *
     * @pre  archiveFilePath != null
     * @pre  fileService != null
     */
    public ArchiveFileService(String archiveFilePath, FileService fileService) {
        this.archiveFilePath = archiveFilePath;
        this.fileService = fileService;
    }

    /**
     * @brief Imposta il percorso del file dell'archivio.
     *
     * @param path Nuovo percorso del file.
     *
     * @pre  path != null
     * @post this.archiveFilePath == path
     */
    public void setArchiveFilePath(String path) {
        this.archiveFilePath = path;
    }

    /**
     * @brief Restituisce il percorso del file dell'archivio.
     *
     * @return Percorso del file attualmente configurato.
     */
    public String getArchiveFilePath() {
        return this.archiveFilePath;
    }

    /**
     * @brief Imposta il servizio di I/O utilizzato.
     *
     * @param fileService Servizio di I/O.
     *
     * @pre  fileService != null
     * @post this.fileService == fileService
     */
    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * @brief Carica l'archivio della biblioteca da file.
     *
     * Il metodo legge il contenuto del file configurato e
     * verifica che l'oggetto deserializzato sia di tipo LibraryArchive.
     *
     * @return Archivio della biblioteca caricato da file.
     *
     * @pre  archiveFilePath != null
     * @pre  fileService != null
     *
     * @throws IOException Se:
     *         - la lettura del file fallisce;
     *         - il contenuto del file non rappresenta un LibraryArchive valido.
     */
    public LibraryArchive loadArchive() throws IOException {

        Object data = fileService.readFromFile(archiveFilePath);

        if (!(data instanceof LibraryArchive)) {
            throw new IOException("Il contenuto del file non rappresenta un archivio valido.");
        }

        return (LibraryArchive) data;
    }

    /**
     * @brief Salva l'archivio corrente su file.
     *
     * Serializza l'oggetto LibraryArchive e lo scrive
     * nel file configurato.
     *
     * @param archive Archivio da salvare.
     *
     * @pre  archive != null
     * @pre  archiveFilePath != null
     * @pre  fileService != null
     *
     * @throws IOException Se la scrittura su file fallisce.
     */
    public void saveArchive(LibraryArchive archive) throws IOException {
        fileService.writeToFile(archiveFilePath, archive);
    }
}