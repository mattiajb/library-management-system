/**
 * @file MandatoryFieldException.java
 * @brief Eccezione lanciata quando un campo obbligatorio non è valorizzato correttamente.
 */
package swe.group04.libraryms.exceptions;

/**
 * @brief Segnala la violazione di un vincolo su campi obbligatori.
 *
 * Può essere lanciata, ad esempio, quando:
 * - un campo richiesto è null o vuoto,
 * - un dato minimo necessario per completare un'operazione non è stato fornito.
 */
public class MandatoryFieldException extends RuntimeException {
    
    /**
     * @brief Crea una nuova eccezione con il messaggio specificato.
     *
     * @param message Descrizione dell'errore.
     */
    public MandatoryFieldException(String message) {
        super(message);
    }
}