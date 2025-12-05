/**
 * @file UserHasActiveLoanException.java
 * @brief Eccezione lanciata quando si tenta un'operazione su un utente che ha prestiti attivi.
 */
package swe.group04.libraryms.exceptions;

/**
 * @brief Segnala che un utente ha ancora uno o più prestiti attivi.
 *
 * Può essere lanciata, ad esempio, quando:
 * - si tenta di rimuovere un utente che ha prestiti non ancora restituiti,
 * - si vuole disabilitare o cancellare un account che non è "a saldo" con la biblioteca.
 */
public class UserHasActiveLoanException extends RuntimeException {
    
    /**
     * @brief Crea una nuova eccezione con il messaggio specificato.
     *
     * @param message Descrizione dell'errore.
     */
    public UserHasActiveLoanException(String message) {
        super(message);
    }
}