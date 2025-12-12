/**
 * @file FileService.java
 * @brief Classe responsabile delle operazioni di I/O su file tramite serializzazione.
 *
 * Fornisce metodi generici per:
 * - scrivere un oggetto su file (serializzazione);
 * - leggere un oggetto da file (deserializzazione).
 *
 * @note non contiene logica di business e non valida la correttezza semantica dei dati.
 */
package swe.group04.libraryms.persistence;

import java.io.*;

/**
 * @brief Servizio di utilità per operazioni generiche su file.
 *
 */
public class FileService {

    /**
     * @brief Scrive un oggetto su file utilizzando la serializzazione.
     *
     * @param path Percorso del file su cui eseguire la scrittura.
     * @param data Oggetto da serializzare e salvare.
     *
     * @pre  path != null
     * @pre  data != null
     *
     * @post Il file indicato da path contiene una rappresentazione serializzata di data.
     *
     * @throws IllegalArgumentException Se path è nullo o se data è nullo.
     * @throws IOException Se si verifica un errore di I/O durante la scrittura.
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
     * @param path Percorso del file da cui leggere.
     *
     * @pre  path != null
     *
     * @return Oggetto deserializzato letto dal file.
     *
     * @throws IOException Se si verifica un errore di I/O durante la lettura.
     * @throws RuntimeException Se la classe dell'oggetto deserializzato non è disponibile
     *                          (wrapping della ClassNotFoundException).
     *
     * @note Se il file non esiste o non è accessibile, viene sollevata IOException
     *       dalle classi di I/O utilizzate (FileInputStream/ObjectInputStream).
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