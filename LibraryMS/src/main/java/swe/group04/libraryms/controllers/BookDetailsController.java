/**
 * @file BookDetailsController.java
 * @brief Controller responsabile della visualizzazione dei dettagli di un libro.
 */

package swe.group04.libraryms.controllers;

import swe.group04.libraryms.models.Book;

/**
 * @brief Gestisce l'operazione di visualizzazione dettagliata delle informazioni
 *        relative a un libro presente nel catalogo.
 *
 * L'operazione prevede il recupero dei dati del libro e la loro
 * presentazione all'utente tramite l'interfaccia grafica.
 */
public class BookDetailsController {
    
    /**
     * @brief Mostra i dettagli del libro selezionato.
     *
     * @param book Libro di cui visualizzare le informazioni.
     *
     * @pre  book != null
     * @pre  book.getIsbn() != null
     *
     * @post I dettagli del libro risultano visualizzati all'utente.
     */
    public void showDetails(Book book){
        
    }
}
