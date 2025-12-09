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

import java.io.*;

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
     * @pre path != null
     *
     * @post Il file indicato da path contiene una rappresentazione serializzata di data
     *
     * @param path Percorso del file su cui eseguire la scrittura.
     * @param data Oggetto da serializzare e salvare.
     */
    public void writeToFile(String path, Object data) throws IOException {

        if(path == null) {
            throw new IllegalArgumentException("Il percorso del file non può essere nullo.");
        }
        if(data == null) {
            throw new IllegalArgumentException("L'oggetto da salvare non può essere nullo.");
        }

        try (
        FileOutputStream fos = new FileOutputStream(path);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        ) {
            oos.writeObject(data);
        }
    }
    
    /**
     * @brief Legge un oggetto da un file tramite deserializzazione.
     *
     * @pre path != null
     *
     * @post true // Il metodo non modifica lo stato interno del file service
     *
     * @param path Percorso del file da cui leggere.
     * @return Oggetto deserializzato, oppure null finché il metodo
     *         non viene effettivamente implementato.
     */
    public Object readFromFile(String path) throws IOException {

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(path))
        )) {
            return ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Errore di lettura del file: " + path, e);
        }

    }
}
