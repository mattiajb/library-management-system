/**
 * @file BookController.java
 * @brief Controller responsabile della gestione delle operazioni sui libri.
 */

package swe.group04.libraryms.controllers;

import swe.group04.libraryms.models.Book;

/**
 * @brief Gestisce le operazioni relative alla visualizzazione, creazione,
 *        modifica e rimozione dei libri dal catalogo.
 *
 * Il controller media tra interfaccia utente e logica applicativa
 * per la gestione dei libri.
 */
public class BookController {
    
    /**
     * @brief Mostra l’elenco dei libri presenti nel catalogo.
     *
     * @post La lista dei libri risulta visualizzata all'utente.
     */
    public void showBookList() {
    }
    
    /**
     * @brief Apre la finestra di inserimento di un nuovo libro.
     *
     * @post Il form di inserimento risulta mostrato all'utente.
     */
    public void openAddBookForm() {
        
    }
    
    /**
     * @brief Apre la finestra di modifica di un libro esistente.
     *
     * @param book Libro da modificare.
     *
     * @pre  book != null
     * @pre  book.getIsbn() != null
     *
     * @post Il form di modifica risulta mostrato
     *       con i campi precompilati secondo book.
     */
    public void openEditBookForm(Book book) {
        
    }
    
    /**
     * @brief Conferma la creazione di un nuovo libro.
     *
     * @param book Libro da registrare.
     *
     * @pre  book != null
     * @pre  book.getIsbn() != null
     *
     * @post Il libro risulta inserito nel catalogo.
     * @post L’archivio risulta salvato.
     */
    public void confirmAddBook(Book book) {
        
    }
    
    /**
     * @brief Conferma la modifica dei dati di un libro esistente.
     *
     * @param book Libro aggiornato.
     *
     * @pre  book != null
     * @pre  book.getIsbn() != null
     *
     * @post I dati del libro risultano aggiornati nel catalogo.
     * @post L’archivio aggiornato risulta salvato.
     */
    public void confirmEditBook(Book book) {
        
    }
    
    /**
     * @brief Elimina un libro dal catalogo.
     *
     * @param book Libro da eliminare.
     *
     * @pre  book != null
     * @pre  book.getIsbn() != null
     *
     * @post Il libro risulta rimosso dal catalogo.
     * @post L’archivio aggiornato risulta salvato.
     */
    public void deleteBook(Book book) {
    
    }
}
