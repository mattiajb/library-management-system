/**
 * @file Validatable.java
 * @brief Interfaccia per oggetti che possono essere validati.
 *
 * Le classi che implementano questa interfaccia devono fornire
 * un controllo di coerenza sul proprio stato interno.
 */
package swe.group04.libraryms.models.validation;

/**
 * @brief Definisce il contratto per oggetti che espongono
 *        un controllo di validità sul proprio stato.
 */
public interface Validatable {

    /**
     * Verifica se l'oggetto è in uno stato coerente e utilizzabile.
     *
     * @return true se tutti i vincoli interni sono rispettati,
     *         false altrimenti.
     */
    boolean isValid();
}
