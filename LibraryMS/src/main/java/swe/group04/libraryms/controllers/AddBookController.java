/**
 * @file AddBookController.java
 * @brief Controller responsabile dell'aggiunta di un nuovo libro nel catalogo.
 */

package swe.group04.libraryms.controllers;

import swe.group04.libraryms.models.Book;

/**
 * @brief Gestisce l'operazione di registrazione di un nuovo libro
 *        all'interno del catalogo della biblioteca.
 *
 * L'operazione prevede la validazione del libro fornito,
 * l'inserimento nel catalogo e il salvataggio dell'archivio aggiornato.
 */
public class AddBookController {
    
    /**
     * @brief Registra un nuovo libro nel sistema.
     *
     * Riceve un oggetto Book già inizializzato, ne controlla la validità
     * e lo aggiunge al catalogo della biblioteca. In seguito, richiede
     * il salvataggio dell'archivio aggiornato.
     *
     * @param book oggetto Book contenente tutti i dati del libro da registrare.
     *
     * @pre  book != null  
     *       Il chiamante deve fornire un'istanza di Book valida e già inizializzata
     *       con tutte le informazioni obbligatorie (titolo, autori, anno di
     *       pubblicazione, identificativo, numero di copie, ecc.).
     *
     * @post Se la validazione ha esito positivo:
     *       - il libro risulta inserito nel catalogo della biblioteca;
     *       - lo stato dell'archivio è stato aggiornato e salvato in modo persistente.
     */
    public void addNewBook(Book book) {
        
    }
}

