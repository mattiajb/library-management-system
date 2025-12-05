/**
 * @file UserController.java
 * @brief Controller responsabile della gestione delle operazioni sugli utenti.
 */
package swe.group04.libraryms.controllers;

import swe.group04.libraryms.models.User;

/**
 * @brief Gestisce le operazioni relative alla visualizzazione, creazione,
 *        modifica ed eliminazione degli utenti del sistema della biblioteca.
 *
 * Il controller media tra l'interfaccia utente e la logica applicativa
 * per tutte le funzionalità riguardanti la gestione degli utenti.
 */
public class UserController {
    
    /**
     * @brief Mostra l’elenco degli utenti registrati nel sistema.
     *
     * @post La lista degli utenti risulta visualizzata all’utente.
     */
    public void showUserList(){
    
    }
    
    /**
     * @brief Apre la finestra di inserimento di un nuovo utente.
     *
     * @post Il form di inserimento risulta mostrato all’utente.
     */
    public void openAddUserForm() {
    
    }
    
    /**
     * @brief Apre la finestra di modifica dei dati di un utente esistente.
     *
     * @param user Utente da modificare.
     *
     * @pre  user != null
     * @pre  user.getUserId() != null
     *
     * @post Il form di modifica risulta mostrato con i dati dell'utente precompilati.
     */
    public void openEditUserForm(User user) {
        
    }
    
    /**
     * @brief Elimina un utente dal sistema.
     *
     * @param user Utente da eliminare.
     *
     * @pre  user != null
     * @pre  user.getUserId() != null
     *
     * @post L'utente risulta rimosso dall’archivio.
     * @post L’archivio aggiornato risulta salvato.
     */
    public void deleteUser(User user){
    
    }
}
