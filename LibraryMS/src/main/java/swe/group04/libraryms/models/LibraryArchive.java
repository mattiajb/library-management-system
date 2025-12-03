package swe.group04.libraryms.models;

import java.util.ArrayList;
import java.util.List;

public class LibraryArchive {

    // Liste principali dell'archivio
    private List<Book> books;
    private List<User> users;
    private List<Loan> loans;

    // Generatore ID per i prestiti (incrementale)
    private int nextLoanId = 1;

    // Costruttore
    public LibraryArchive() {
        this.books = new ArrayList<>();
        this.users = new ArrayList<>();
        this.loans = new ArrayList<>();
    }

    public List<Book> getBooks() {
        return new ArrayList<>(books);
    }

    public List<User> getUsers() {
        return new ArrayList<>(users);
    }

    public List<Loan> getLoans() {
        return new ArrayList<>(loans);
    }

    // ================================
    // BOOK MANAGEMENT
    // ================================

    public void addBook(Book book) {
        books.add(book);
    }

    public void removeBook(Book book) {
        books.remove(book);
    }

    // Dato un ISBN, trova il libro associato
    public Book findBookByIsbn(String isbn) {
        if (isbn == null) { return null; }
        for (Book book : books) {
            if (book.getIsbn().equals(isbn)) {
                return book;
            }
        }
        return null;
    }

    // ================================
    // USER MANAGEMENT
    // ================================

    public void addUser(User user) {
        users.add(user);
    }

    public void removeUser(User user) {
        users.remove(user);
    }

    // Data una matricola, trova l'utente associato
    public User findUserByCode(String code) {
        if (code == null ) { return null; }
        for (User user : users) {
            if (user.getCode().equals(code)) {
                return user;
            }
        }
        return null;
    }

    // ================================
    // LOAN MANAGEMENT
    // ================================

    // Generazione id prestito
    public int generateLoanId() {
        return nextLoanId++;
    }

    public void addLoan(Loan loan) {
        loans.add(loan);
    }

    public void removeLoan(Loan loan) {
        loans.remove(loan);
    }

    // Dato un identificativo prestito, trova il prestito associato
    public Loan findLoanById(int id) {
        for (Loan loan : loans) {
            if (loan.getLoanId() == id) {
                return loan;
            }
        }
        return null;
    }

    // Dato un utente, restituisce la lista dei prestiti associati
    public List<Loan> findLoansByUser(User user) {
        List<Loan> result = new ArrayList<>();
        for (Loan l : loans) {
            if (l.getUser().equals(user)) {
                result.add(l);
            }
        }
        return result;
    }

    // Dato un libro, restituice la lista dei prestiti associati
    public List<Loan> findLoansByBook(Book book) {
        List<Loan> result = new ArrayList<>();
        for (Loan l : loans) {
            if (l.getBook().equals(book)) {
                result.add(l);
            }
        }
        return result;
    }

    // ================================
    // UTILITY METHODS
    // ================================

    // Restituisce i prestiti attivi
    public List<Loan> getActiveLoans() {
        List<Loan> result = new ArrayList<>();
        for (Loan l : loans) {
            if (l.isActive()) {
                result.add(l);
            }
        }
        return result;
    }

    // Restituisce prestiti restutuiti
    public List<Loan> getReturnedLoans() {
        List<Loan> result = new ArrayList<>();
        for (Loan l : loans) {
            if (l.isReturned()) {
                result.add(l);
            }
        }
        return result;
    }
}
