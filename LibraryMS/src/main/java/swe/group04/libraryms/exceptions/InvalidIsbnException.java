/**
 * @file InvalidIsbnException.java
 * @brief Eccezione lanciata quando un ISBN non è valido.
 */
package swe.group04.libraryms.exceptions;

/**
 * @brief Segnala un errore relativo al formato o alla validità di un ISBN.
 *
 * Può essere lanciata, ad esempio, quando:
 * - l'ISBN non rispetta il formato atteso, oppure
 * - l'ISBN è già presente nel catalogo e viola un vincolo di unicità.
 */
public class InvalidIsbnException extends RuntimeException {
    
    /**
     * @brief Crea una nuova eccezione con il messaggio specificato.
     *
     * @param message Descrizione dell'errore.
     */
    public InvalidIsbnException(String message) {
        super(message);
    }
}