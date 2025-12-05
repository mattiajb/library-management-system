/**
 * @file MaxLoansReachedException.java
 * @brief Eccezione lanciata quando un utente ha raggiunto il numero massimo di prestiti consentiti.
 */
package swe.group04.libraryms.exceptions;

/**
 * @brief Segnala che non è possibile aprire nuovi prestiti per un utente.
 *
 * Viene tipicamente lanciata quando:
 * - l'utente ha già raggiunto il limite massimo di prestiti attivi previsto
 *   dal regolamento della biblioteca.
 */
public class MaxLoansReachedException extends RuntimeException {
    
    /**
     * @brief Crea una nuova eccezione con il messaggio specificato.
     *
     * @param message Descrizione dell'errore.
     */
    public MaxLoansReachedException(String message) {
        super(message);
    }
}