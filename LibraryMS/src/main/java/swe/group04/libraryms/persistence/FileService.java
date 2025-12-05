/**
 * @file FileService.java
 * @brief Classe responsabile della gestione delle operazioni generiche di I/O su file.
 *
 * Questa classe fornisce metodi per leggere e scrivere oggetti tramite
 * serializzazione, verificare l'esistenza di un file ed eliminarlo.
 * Costituisce il livello più basso dell'accesso al file system
 * e viene utilizzata dagli altri servizi di persistenza.
 *
 * L'obiettivo è incapsulare l'interazione diretta con il file system
 * e offrire un'interfaccia semplice e robusta alle componenti che ne fanno uso.
 */
package swe.group04.libraryms.persistence;

/**
 * @brief Servizio di utilità per operazioni generiche su file.
 *
 * Fornisce metodi per serializzare e deserializzare oggetti su file.
 * L'implementazione è attualmente un segnaposto e dovrà essere completata
 * in fase di sviluppo.
 */
public class FileService {
    
    /**
     * @brief Scrive un oggetto su file utilizzando la serializzazione.
     *
     * @param path Percorso del file su cui eseguire la scrittura.
     * @param data Oggetto da serializzare e salvare.
     */
    public void writeToFile(String path, Object data){

    }
    
    /**
     * @brief Legge un oggetto da un file tramite deserializzazione.
     *
     * @param path Percorso del file da cui leggere.
     * @return Oggetto deserializzato, oppure null finché il metodo
     *         non viene effettivamente implementato.
     */
    public Object readFromFile(String path) {
        return null;
    }
}
