/**
 * @file NoAvailableCopiesException.java
 * @brief Eccezione lanciata quando non ci sono copie disponibili di un libro.
 */
package swe.group04.libraryms.exceptions;

/**
 * @brief Segnala l'impossibilità di aprire un nuovo prestito per mancanza di copie.
 *
 * Viene tipicamente lanciata quando:
 * - tutte le copie di un libro risultano già in prestito,
 * - il numero di copie disponibili è uguale a zero.
 */
public class NoAvailableCopiesException extends RuntimeException {
    
    /**
     * @brief Crea una nuova eccezione con il messaggio specificato.
     *
     * @param message Descrizione dell'errore.
     */
    public NoAvailableCopiesException(String message) {
        super(message);
    }
}