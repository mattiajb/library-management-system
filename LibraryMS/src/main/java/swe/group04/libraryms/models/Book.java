/**
 * @file Book.java
 * @brief Rappresenta un libro nel catalogo della biblioteca.
 */
package swe.group04.libraryms.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @brief Modello di dominio per un libro.
 *
 * Un oggetto Book descrive un libro presente nel catalogo,
 * identificato univocamente dal suo ISBN.
 *
 * @invariant isbn != null
 * @invariant totalCopies >= 0
 * @invariant 0 <= availableCopies && availableCopies <= totalCopies
 */
public class Book {

    /// Spazio degli Attributi
  
    private String title;
    private List<String> authors;
    private int releaseYear;
    private final String isbn; ///< Attributo identificativo => Non modificabile
    private int totalCopies;
    private int availableCopies;

    /**
     * @brief Crea un nuovo libro con le informazioni specificate.
     *
     * Alla creazione le copie disponibili coincidono con le copie totali.
     *
     * @pre  title != null
     * @pre  authors != null
     * @pre  isbn != null
     * @pre  totalCopies >= 0
     *
     * @post getTitle().equals(title)
     * @post getAuthors().equals(authors) a parità di contenuto
     * @post getIsbn().equals(isbn)
     * @post getTotalCopies() == totalCopies
     * @post getAvailableCopies() == totalCopies
     */
    public Book(String title, List<String> authors, int releaseYear, String isbn, int totalCopies) {
        this.title = title;
        this.authors = new ArrayList<>(authors);
        this.releaseYear = releaseYear;
        this.isbn = isbn;
        this.totalCopies = totalCopies;
        availableCopies = totalCopies; ///< Al momento della creazione, non ci sono prestiti attivi
    }
   
    /**
     * @brief Restituisce il titolo del libro.
     *
     * @return Titolo del libro.
     */
    public String getTitle() { 
        return title; 
    }
    
    /**
     * @brief Restituisce la lista degli autori.
     *
     * @return Copia della lista degli autori.
     */
    public List<String> getAuthors() { 
        return new ArrayList<>(authors); 
    }
    
    /**
     * @brief Restituisce l'anno di pubblicazione.
     *
     * @return Anno di pubblicazione.
     */
    public int getReleaseYear() { 
        return releaseYear; 
    }
    
    /**
     * @brief Restituisce l'ISBN del libro.
     *
     * @return Codice ISBN.
     */
    public String getIsbn() { 
        return isbn; 
    }
    
    /**
     * @brief Restituisce il numero totale di copie.
     *
     * @return Numero totale di copie (>= 0).
     */
    public int getTotalCopies() { 
        return totalCopies; 
    }
    
    /**
     * @brief Restituisce il numero di copie disponibili.
     *
     * @return Numero di copie attualmente disponibili (>= 0).
     */
    public int getAvailableCopies() { 
        return availableCopies; 
    }
    
    /**
     * @brief Imposta il titolo del libro.
     *
     * @param title Nuovo titolo.
     */
    public void setTitle(String title) { 
        this.title = title; 
    }
    
    /**
     * @brief Sostituisce l'elenco degli autori.
     *
     * @param authors Nuova lista di autori.
     */
    public void setAuthors(List<String> authors) { 
        this.authors = new ArrayList<>(authors); 
    }
    
    /**
     * @brief Imposta l'anno di pubblicazione.
     *
     * @param releaseYear Nuovo anno di pubblicazione.
     */
    public void setReleaseYear(int releaseYear) { 
        this.releaseYear = releaseYear; 
    }
    
    /**
     * @brief Imposta il numero totale di copie del libro.
     *
     * @param totalCopies Nuovo numero totale di copie (>= 0).
     */
    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies; 
    }
    
    /**
     * @brief Imposta il numero di copie disponibili.
     *
     * @param availableCopies Nuovo numero di copie disponibili (>= 0).
     */
    public void setAvailableCopies(int availableCopies) { 
        this.availableCopies = availableCopies; 
    }
    
    /**
     * @brief Verifica se esistono copie disponibili per il prestito.
     *
     * @return true se esiste almeno una copia disponibile, false altrimenti.
     */
    public boolean hasAvailableCopies() {
        return availableCopies > 0;
    }

    /**
     * @brief Restituisce il codice hash del libro.
     *
     * Ridefinisce hashCode usando solo l'ISBN come chiave,
     * in modo coerente con equals.
     *
     * @return Valore hash dell'oggetto.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.isbn);
        return hash;
    }
      
    /**
     * @brief Confronta questo libro con un altro oggetto.
     *
     * Ridefinisce equals: due libri sono considerati uguali
     * se hanno lo stesso ISBN.
     *
     * @param obj Oggetto con cui confrontare.
     * @return true se rappresenta lo stesso libro, false altrimenti.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        final Book other = (Book)obj;
        if (!Objects.equals(this.isbn, other.isbn)) { return false; }
        return true;
    }
    
    /**
     * @brief Restituisce una rappresentazione testuale del libro.
     *
     * Ridefinisce toString per mostrare le principali informazioni
     * su titolo, autori, anno, ISBN e copie.
     *
     * @return Stringa descrittiva del libro.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Title: ").append(title).append("\n");
        sb.append("Authors: ").append(authors).append("\n");
        sb.append("ReleaseYear: ").append(releaseYear).append("\n");
        sb.append("ISBN: ").append(isbn).append("\n");
        sb.append("Total Copies: ").append(totalCopies).append("\n");
        sb.append("Available Copies: ").append(availableCopies).append("\n");
        return sb.toString();
    }
}
