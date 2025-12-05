/**
 * @file InvalidEmailException.java
 * @brief Eccezione lanciata quando l'email di un utente non rispetta il formato atteso.
 */
package swe.group04.libraryms.exceptions;

/**
 * @brief Segnala un errore di validazione sull'email dell'utente.
 */
public class InvalidEmailException extends RuntimeException{
    
    /**
     * @brief Crea una nuova eccezione con il messaggio specificato.
     *
     * @param[in] message Messaggio descrittivo dell'errore.
     */
    public InvalidEmailException(String message) {
        super(message);
    }
}
