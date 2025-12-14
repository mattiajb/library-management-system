/**
 * @file Loan.java
 * @brief Rappresenta un prestito di un libro a un utente.
 */
package swe.group04.libraryms.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * @brief Modello di dominio per un prestito.
 *
 * Un oggetto Loan descrive il prestito di un libro a un utente
 * in un certo intervallo di tempo, identificato da un ID univoco.
 */
public class Loan implements Serializable {

    /// Spazio degli Attributi
    
    private final int loanId; ///< Identificativo univoco del prestito
    private User user; ///< Utente coinvolto
    private Book book; ///< Libro prestato

    private LocalDate loanDate; ///< Data del prestito
    private LocalDate dueDate; ///< Data di scadenza prevista
    private LocalDate returnDate; ///< Data restituzione effettiva (null = non restituito)
    private Boolean status; ///< Stato del prestito

    /**
     * @brief Crea un nuovo prestito attivo.
     *
     * Alla creazione il prestito è considerato attivo, quindi
     * la data di restituzione è impostata a null.
     *
     * @param loanId   Identificatore numerico del prestito.
     * @param user     Utente che effettua il prestito (non nullo).
     * @param book     Libro oggetto del prestito (non nullo).
     * @param loanDate Data in cui il prestito viene registrato (non nulla).
     * @param dueDate  Data entro cui il libro deve essere restituito (non nulla).
     * @param status   Rappresenta lo stato del prestito: se attivo -> true.
     *
     * @pre  user != null
     * @pre  book != null
     * @pre  loanDate != null
     * @pre  dueDate != null
     *
     * @post getLoanId() == loanId
     * @post getUser() == user
     * @post getBook() == book
     * @post getLoanDate().equals(loanDate)
     * @post getDueDate().equals(dueDate)
     * @post getReturnDate() == null
     */
    public Loan(int loanId, User user, Book book, LocalDate loanDate, LocalDate dueDate, Boolean status) {
        this.loanId = loanId;
        this.user = user;
        this.book = book;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.returnDate = null; ///< Prestito attivo alla creazione
        this.status = status;
    }

    /**
     * @brief Restituisce l'ID del prestito.
     *
     * @return Identificativo univoco del prestito.
     */
    public int getLoanId() { 
        return loanId; 
    }
    
    /**
     * @brief Restituisce l'utente coinvolto nel prestito.
     *
     * @return Utente associato al prestito.
     */
    public User getUser() { 
        return user; 
    }
    
    /**
     * @brief Restituisce il libro prestato.
     *
     * @return Libro associato al prestito.
     */
    public Book getBook() { 
        return book; 
    }
    
    /**
     * @brief Restituisce la data di apertura del prestito.
     *
     * @return Data del prestito.
     */
    public LocalDate getLoanDate() { 
        return loanDate; 
    }
    
    /**
     * @brief Restituisce la data di scadenza prevista.
     *
     * @return Data di scadenza del prestito.
     */
    public LocalDate getDueDate() { 
        return dueDate; 
    }
    
    /**
     * @brief Restituisce la data di restituzione effettiva.
     *
     * @return Data di restituzione, oppure null se il prestito non è ancora restituito.
     */
    public LocalDate getReturnDate() { 
        return returnDate; 
    }


    /**
     * @brief Restituisce lo stato del prestito
     * @return true se attivo, false se restituito
     */
    public Boolean getStatus() {
        return status;
    }
    
    /**
     * @brief Imposta l'utente associato al prestito.
     *
     * @pre  user != null
     * @post getUser() == user
     *
     * @param user Nuovo utente.
     */
    public void setUser(User user) { 
        this.user = user; 
    }
    
    /**
     * @brief Imposta il libro associato al prestito.
     *
     * @param book Nuovo libro.
     */
    public void setBook(Book book) { 
        this.book = book; 
    }
    
    /**
     * @brief Imposta la data di apertura del prestito.
     *
     * @param loanDate Nuova data del prestito.
     */
    public void setLoanDate(LocalDate loanDate) { 
        this.loanDate = loanDate; 
    }
    
    /**
     * @brief Imposta la data di scadenza del prestito.
     *
     * @param dueDate Nuova data di scadenza.
     */
    public void setDueDate(LocalDate dueDate) { 
        this.dueDate = dueDate; 
    }
    
    /**
     * @brief Imposta la data di restituzione del prestito.
     *
     * @param returnDate Data di restituzione (null se non ancora restituito).
     */
    public void setReturnDate(LocalDate returnDate) { 
        this.returnDate = returnDate; 
    }
    
    /**
     * @brief Verifica se il prestito è stato restituito.
     *
     * @return true se esiste una data di restituzione, false altrimenti.
     */
    public boolean setStatus(Boolean status) {
        this.status = status;
        return status;
    }

    /**
     * @brief Restituisce se il prestito è attivo analizzando lo stato.
     *
     * @return true se attivo, false se non.
     */
    public boolean isActive(){
        return getStatus();
    }

    /**
     * @brief Restituisce il codice hash del prestito.
     *
     * Ridefinisce hashCode usando l'identificativo del prestito.
     *
     * @return Valore hash dell'oggetto.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.loanId);
        return hash;
    }
    
    /**
     * @brief Confronta questo prestito con un altro oggetto.
     *
     * Ridefinisce equals: due prestiti sono considerati uguali
     * se hanno lo stesso identificativo.
     *
     * @param obj Oggetto con cui confrontare.
     * @return true se rappresenta lo stesso prestito, false altrimenti.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Loan other = (Loan) obj;
        return Objects.equals(this.loanId, other.loanId);
    }
    
    /**
     * @brief Restituisce una rappresentazione testuale del prestito.
     *
     * Ridefinisce toString per mostrare le principali informazioni
     * su utente, libro e date del prestito.
     *
     * @return Stringa descrittiva del prestito.
     */
    @Override
    public String toString() {
        return "Loan ID: " + loanId + "\n" +
                "User: " + user.getCode() + "\n" +
                "Book: " + book.getIsbn() + "\n" +
                "Loan Date: " + loanDate + "\n" +
                "Due Date: " + dueDate + "\n" +
                "Return Date: " + (returnDate != null ? returnDate : "Not returned") + "\n";
    }
}
