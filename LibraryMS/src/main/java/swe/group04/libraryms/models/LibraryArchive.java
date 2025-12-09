/**
 * @file LibraryArchive.java
 * @brief Rappresenta l'archivio centrale della biblioteca.
 *
 * L'archivio contiene le collezioni principali:
 * - elenco dei libri
 * - elenco degli utenti
 * - elenco dei prestiti
 *
 * Questa classe funge da "aggregate root" per il modello di dominio
 * e fornisce metodi di utilità per aggiungere/rimuovere elementi e
 * effettuare ricerche basilari.
 */
package swe.group04.libraryms.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @brief Archivio contenente i dati principali del sistema (libri, utenti, prestiti).
 *
 * Mantiene:
 * - il catalogo dei libri
 * - il registro degli utenti
 * - l'elenco dei prestiti
 *
 * Rappresenta il punto di accesso principale ai dati della biblioteca.
 *
 * @invariant books != null
 * @invariant users != null
 * @invariant loans != null
 * @invariant nextLoanId > 0
 */
public class LibraryArchive implements Serializable {

    private List<Book> books;
    private List<User> users;
    private List<Loan> loans;

    private int nextLoanId = 1;

    /**
     * @brief Crea un archivio vuoto.
     *
     * @post getBooks().isEmpty()
     * @post getUsers().isEmpty()
     * @post getLoans().isEmpty()
     */
    public LibraryArchive() {
        this.books = new ArrayList<>();
        this.users = new ArrayList<>();
        this.loans = new ArrayList<>();
    }
    
    /**
     * @brief Restituisce i libri gestiti dall'archivio.
     *
     * Viene restituita una **copia** della lista interna, in modo da preservare
     * l'incapsulamento: modificare la lista restituita non modifica lo stato
     * dell'archivio.
     *
     * @return Copia della lista dei libri attualmente presenti in archivio.
     */
    public List<Book> getBooks() {
        return new ArrayList<>(books);
    }

    /**
     * @brief Restituisce la lista completa degli utenti.
     *
     * @return Lista degli utenti registrati.
     */
    public List<User> getUsers() {
        return new ArrayList<>(users);
    }
    
    /**
     * @brief Restituisce la lista completa dei prestiti.
     *
     * @return Lista di tutti i prestiti registrati.
     */
    public List<Loan> getLoans() {
        return new ArrayList<>(loans);
    }

    /**
     * @brief Aggiunge un libro al catalogo.
     *
     * @pre  book != null
     * @post books.contains(book)
     *
     * @param book [in] Libro da inserire.
     */
    public void addBook(Book book) {
        books.add(book);
    }
    
    /**
     * @brief Rimuove un libro dal catalogo.
     *
     * @pre  book != null
     * @post !books.contains(book)
     *
     * @param book [in] Libro da rimuovere.
     */
    public void removeBook(Book book) {
        books.remove(book);
    }

    /**
     * @brief Cerca un libro tramite ISBN.
     *
     * @param isbn [in] Codice ISBN del libro da cercare.
     * @return Il libro corrispondente oppure null se non trovato.
     */
    public Book findBookByIsbn(String isbn) {
        if (isbn == null) { return null; }
        for (Book book : books) {
            if (book.getIsbn().equals(isbn)) {
                return book;
            }
        }
        return null;
    }

    /**
     * @brief Aggiunge un utente all'archivio.
     *
     * @pre  user != null
     * @post users.contains(user)
     *
     * @param user [in] Utente da aggiungere.
     */
    public void addUser(User user) {
        users.add(user);
    }
    
    /**
     * @brief Rimuove un utente dall'archivio.
     *
     * @pre  user != null
     * @post !users.contains(user)
     *
     * @param user [in] Utente da rimuovere.
     */
    public void removeUser(User user) {
        users.remove(user);
    }

    /**
     * @brief Trova un utente tramite il codice identificativo (matricola).
     *
     * @param code [in] Matricola dell'utente.
     * @return L'utente corrispondente oppure null se non esiste.
     */
    public User findUserByCode(String code) {
        if (code == null ) { return null; }
        for (User user : users) {
            if (user.getCode().equals(code)) {
                return user;
            }
        }
        return null;
    }

     /**
     * @brief Genera un nuovo ID univoco per un prestito.
     *
     * @return Numero intero incrementale da usare come ID prestito.
     */
    public int generateLoanId() {
        return nextLoanId++;
    }
    
    /**
     * @brief Aggiunge un prestito all'archivio.
     *
     * @pre  loan != null
     * @post loans.contains(loan)
     *
     * @param loan [in] Prestito da aggiungere.
     */
    public void addLoan(Loan loan) {
        loans.add(loan);
    }

    /**
     * @brief Rimuove un prestito dall'archivio.
     *
     * @pre  loan != null
     * @post !loans.contains(loan)
     *
     * @param loan [in] Prestito da rimuovere.
     */
    public void removeLoan(Loan loan) {
        loans.remove(loan);
    }
    
    /**
     * @brief Trova un prestito a partire dal suo ID numerico.
     *
     * @param id [in] Identificativo del prestito.
     * @return Il prestito corrispondente oppure null se non esiste.
     */
    public Loan findLoanById(int id) {
        for (Loan loan : loans) {
            if (loan.getLoanId() == id) {
                return loan;
            }
        }
        return null;
    }
    
    /**
     * @brief Restituisce tutti i prestiti effettuati da un determinato utente.
     *
     * @param user [in] Utente di cui cercare i prestiti.
     * @return Lista dei prestiti appartenenti all'utente.
     */
    public List<Loan> findLoansByUser(User user) {
        List<Loan> result = new ArrayList<>();
        for (Loan l : loans) {
            if (l.getUser().equals(user)) {
                result.add(l);
            }
        }
        return result;
    }
    
    /**
     * @brief Restituisce tutti i prestiti relativi a un determinato libro.
     *
     * @param book [in] Libro di cui cercare i prestiti.
     * @return Lista dei prestiti associati al libro.
     */
    public List<Loan> findLoansByBook(Book book) {
        List<Loan> result = new ArrayList<>();
        for (Loan l : loans) {
            if (l.getBook().equals(book)) {
                result.add(l);
            }
        }
        return result;
    }
    
    /**
     * @brief Restituisce la lista dei prestiti attualmente attivi.
     *
     * @return Lista dei prestiti per i quali non è ancora registrata la restituzione.
     */
    public List<Loan> getActiveLoans() {
        List<Loan> result = new ArrayList<>();
        for (Loan l : loans) {
            if (l.isActive()) {
                result.add(l);
            }
        }
        return result;
    }
    
    /**
     * @brief Restituisce la lista dei prestiti già restituiti.
     *
     * @return Lista dei prestiti per cui è impostata una returnDate.
     */
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
