package swe.group04.libraryms.models;

import java.time.LocalDate;
import java.util.Objects;

public class Loan {

    // Spazio degli Attributi
    private final String loanId; // Identificativo univoco del prestito
    private User user; // Utente coinvolto
    private Book book; // Libro prestato

    private LocalDate loanDate; // Data del prestito
    private LocalDate dueDate; // Data di scadenza prevista
    private LocalDate returnDate; // Data restituzione effettiva (null = non restituito)

    // Costruttore
    public Loan(String loanId, User user, Book book, LocalDate loanDate, LocalDate dueDate) {
        this.loanId = loanId;
        this.user = user;
        this.book = book;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.returnDate = null; // Prestito attivo alla creazione
    }

    // Getter
    public String getLoanId() { return loanId; }
    public User getUser() { return user; }
    public Book getBook() { return book; }
    public LocalDate getLoanDate() { return loanDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }

    // Setter
    public void setUser(User user) { this.user = user; }
    public void setBook(Book book) { this.book = book; }
    public void setLoanDate(LocalDate loanDate) { this.loanDate = loanDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    // Metodi utili
    public boolean isReturned() {
        return returnDate != null;
    }

    public boolean isActive() {
        return returnDate == null;
    }

    // equals/hashCode basati su loanId
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.loanId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Loan other = (Loan) obj;
        return Objects.equals(this.loanId, other.loanId);
    }

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
