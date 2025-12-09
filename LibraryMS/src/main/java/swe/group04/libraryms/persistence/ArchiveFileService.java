/**
 * @file ArchiveFileService.java
 * @brief Gestisce il caricamento e il salvataggio dell'archivio della biblioteca.
 *
 * Questa classe rappresenta il componente responsabile della persistenza
 * dell'archivio principale del sistema. Si appoggia a FileService per
 * eseguire le operazioni di I/O a basso livello.
 *
 * Responsabilità principali:
 * - Caricamento dell'archivio al lancio dell'applicazione.
 * - Salvataggio dell'archivio dopo le modifiche rilevanti.
 *
 * Nota: questa classe non verifica la validità del percorso del file
 * né la configurazione di FileService; si assume che il chiamante
 * li imposti correttamente prima dell'uso.
 */
package swe.group04.libraryms.persistence;

import java.io.IOException;
import swe.group04.libraryms.models.LibraryArchive;

/**
 * @brief Servizio per la persistenza dell'archivio della biblioteca.
 *
 * Permette di configurare il percorso del file e il servizio di I/O
 * da utilizzare per serializzare e deserializzare un oggetto LibraryArchive.
 */
public class ArchiveFileService {
    
    private String archiveFilePath;
    private FileService fileService;

    public ArchiveFileService(String archiveFilePath,  FileService fileService)
    {
        this.archiveFilePath = archiveFilePath;
        this.fileService = fileService;
    }

    /**
     * @brief Imposta il percorso del file dell'archivio.
     *
     * @pre  path != null
     * @post getArchiveFilePath().equals(path)
     *
     * @param path Nuovo percorso del file dell'archivio.
     */
    public void setArchiveFilePath(String path) {
        this.archiveFilePath = path;
    }
    
    /**
     * @brief Restituisce il percorso del file dell'archivio.
     *
     * @pre  Nessuna.
     * @post true   // non modifica lo stato dell'oggetto
     *
     * @return Il percorso attualmente configurato, oppure null se non impostato.
     */
    public String getArchiveFilePath(){
        return this.archiveFilePath;
    }
    
    /**
     * @brief Imposta il servizio di I/O utilizzato per leggere/scrivere l'archivio.
     *
     * @pre  fileService != null
     * @post this.fileService == fileService
     *
     * @param fileService Oggetto responsabile delle operazioni su file.
     */
    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }
    
    /**
     * @brief Carica l'archivio della biblioteca dal file configurato.
     *
     * @pre  archiveFilePath != null
     * @pre  fileService != null
     * @pre  Il file indicato da archiveFilePath esiste ed è leggibile.
     *
     * @post true   // il metodo non modifica lo stato interno dell'oggetto
     *
     * @return L'archivio caricato da file.
     *
     * @throws IOException Se la lettura dal file fallisce.
     *
     * @note L'implementazione attuale è un segnaposto e restituisce null.
     *       La logica di caricamento dovrà essere completata in fase di sviluppo.
     */
    public LibraryArchive loadArchive() throws IOException {
        Object data = fileService.readFromFile(archiveFilePath);

        // Controllo del tipo di dato letto da file
        if (!(data instanceof LibraryArchive)){ throw new IOException("Il contenuto del file non è valido. "); }

        return (LibraryArchive) data;
    }
    
    /**
     * @brief Salva l'archivio corrente su file.
     *
     * @pre  archive != null
     * @pre  archiveFilePath != null
     * @pre  fileService != null
     *
     * @post true   // l'effetto è esterno: scrittura su file
     *
     * @param archive Oggetto contenente l'archivio da salvare.
     *
     * @throws IOException Se si verifica un errore durante la scrittura.
     *
     * @note Il metodo è attualmente vuoto: la logica di salvataggio
     *       verrà implementata successivamente.
     */
    public void saveArchive(LibraryArchive archive) throws IOException{
        fileService.writeToFile(archiveFilePath, archive); // Parametri passati: destinazione, archivio
    }
}

