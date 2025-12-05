/**
 * @file AddUserController.java
 * @brief Controller responsabile dell'aggiunta di un nuovo utente nel sistema.
 */

package swe.group04.libraryms.controllers;

import swe.group04.libraryms.models.User;

/**
 * @brief Gestisce l'operazione di registrazione di un nuovo utente
 *        all'interno del sistema della biblioteca.
 *
 * L'operazione prevede la validazione dell'utente fornito,
 * l'inserimento nell'archivio utenti e il salvataggio persistente dei dati.
 */
public class AddUserController {
    
    /**
     * @brief Registra un nuovo utente nel sistema.
     *
     * @param[in] user Oggetto User da registrare.
     *
     * @pre  user != null
     * @pre  user.getUserId() != null
     *
     * @post L'utente risulta presente nell'archivio utenti.
     * @post L’archivio aggiornato risulta salvato.
     */
    public void addNewUser(User user) {
        
    }    
}
