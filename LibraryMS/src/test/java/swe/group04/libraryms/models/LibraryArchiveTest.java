/**
 * @file LibraryArchiveTest.java
 * @ingroup TestsModels
 * @brief Suite di test di unità per la classe di dominio LibraryArchive.
 *
 * Verifica:
 * - corretta inizializzazione dell’archivio (liste vuote di libri, utenti e prestiti);
 * - aggiunta e rimozione di libri e utenti dall’archivio;
 * - corretta ricerca di libri e utenti tramite identificativi;
 * - generazione incrementale degli identificativi dei prestiti;
 * - creazione e registrazione dei prestiti nell’archivio;
 * - recupero dei prestiti per utente e per libro;
 * - distinzione corretta tra prestiti attivi e prestiti restituiti;
 * - corretta serializzazione e deserializzazione dell’archivio.
 */
package swe.group04.libraryms.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @brief Test di unità per LibraryArchive.
 *
 * @ingroup TestsModels
 */
class LibraryArchiveTest {

    private LibraryArchive archive;

    private Book book1;
    private Book book2;

    private User user1;
    private User user2;

    /**
     * @brief Inizializza il fixture di test.
     *
     * Crea un archivio vuoto e prepara due libri e due utenti
     * riutilizzati nei casi di test.
     */
    @BeforeEach
    void setUp() {
        archive = new LibraryArchive();

        book1 = new Book("Clean Code",
                List.of("Robert C. Martin"),
                2008,
                "ISBN-111",
                3);

        book2 = new Book("Refactoring",
                List.of("Martin Fowler"),
                1999,
                "ISBN-222",
                2);

        user1 = new User("Mario", "Rossi", "mario.rossi@example.com", "S123");
        user2 = new User("Luigi", "Bianchi", "luigi.bianchi@example.com", "S456");
    }

    // -------------------------------------------------------------------------
    //                      Inizializzazione
    // -------------------------------------------------------------------------

    /**
     * @brief Verifica che un nuovo LibraryArchive inizi con liste vuote.
     *
     */
    @Test
    @DisplayName("L'archivio parte con liste vuote e nextLoanId = 1")
    void archiveStartsEmpty() {
        assertTrue(archive.getBooks().isEmpty());
        assertTrue(archive.getUsers().isEmpty());
        assertTrue(archive.getLoans().isEmpty());
    }

    // -------------------------------------------------------------------------
    //                      Aggiunta e rimozione libri
    // -------------------------------------------------------------------------

    /**
     * @brief Verifica l’inserimento di un libro nell’archivio.
     * 
     * addBook deve aggiungere il libro e renderlo visibile tramite getBooks().
     */
    @Test
    @DisplayName("addBook aggiunge correttamente un libro")
    void addBookWorks() {
        archive.addBook(book1);
        List<Book> books = archive.getBooks();

        assertEquals(1, books.size());
        assertEquals(book1, books.get(0));
    }

    /**
     * @brief Verifica la rimozione di un libro dall’archivio.
     *
     * removeBook deve eliminare il libro precedentemente inserito.
     */
    @Test
    @DisplayName("removeBook rimuove correttamente un libro")
    void removeBookWorks() {
        archive.addBook(book1);
        archive.removeBook(book1);

        assertTrue(archive.getBooks().isEmpty());
    }

    /**
     * @brief Verifica la ricerca di un libro tramite ISBN.
     *
     * findBookByIsbn deve restituire il libro corretto e null per ISBN assente o null.
     */
    @Test
    @DisplayName("findBookByIsbn trova correttamente un libro")
    void findBookByIsbnWorks() {
        archive.addBook(book1);
        archive.addBook(book2);

        assertEquals(book1, archive.findBookByIsbn("ISBN-111"));
        assertEquals(book2, archive.findBookByIsbn("ISBN-222"));
        assertNull(archive.findBookByIsbn("NON-ESISTE"));
        assertNull(archive.findBookByIsbn(null));
    }

    // -------------------------------------------------------------------------
    //                      Aggiunta e rimozione utenti
    // -------------------------------------------------------------------------

    /**
     * @brief Verifica l’inserimento di un utente nell’archivio.
     *
     * addUser deve aggiungere l’utente e renderlo visibile tramite getUsers().
     */
    @Test
    @DisplayName("addUser aggiunge correttamente un utente")
    void addUserWorks() {
        archive.addUser(user1);
        assertEquals(1, archive.getUsers().size());
    }

    /**
     * @brief Verifica la rimozione di un utente dall’archivio.
     *
     * removeUser deve eliminare l’utente precedentemente inserito.
     */
    @Test
    @DisplayName("removeUser rimuove correttamente un utente")
    void removeUserWorks() {
        archive.addUser(user1);
        archive.removeUser(user1);
        assertTrue(archive.getUsers().isEmpty());
    }

    /**
     * @brief Verifica la ricerca di un utente tramite matricola/codice.
     *
     * findUserByCode deve restituire l’utente corretto e null per codice assente o null.
     */
    @Test
    @DisplayName("findUserByCode trova correttamente un utente")
    void findUserByCodeWorks() {
        archive.addUser(user1);
        archive.addUser(user2);

        assertEquals(user1, archive.findUserByCode("S123"));
        assertEquals(user2, archive.findUserByCode("S456"));
        assertNull(archive.findUserByCode("NOPE"));
        assertNull(archive.findUserByCode(null));
    }

    // -------------------------------------------------------------------------
    //                  Prestiti: generazione ID e aggiunta
    // -------------------------------------------------------------------------

    /**
     * @brief Verifica che la generazione degli ID prestito sia incrementale.
     *
     * generateLoanId deve produrre ID progressivi (id_{k+1} = id_k + 1).
     */
    @Test
    @DisplayName("generateLoanId produce ID incrementali")
    void generateLoanIdIncrements() {
        int id1 = archive.generateLoanId();
        int id2 = archive.generateLoanId();
        int id3 = archive.generateLoanId();

        assertEquals(id1 + 1, id2);
        assertEquals(id2 + 1, id3);
    }

    /**
     * @brief Verifica la creazione e registrazione di un prestito in archivio.
     *
     * addLoan deve creare un Loan coerente con user, book e dueDate e aggiungerlo alla lista loans.
     */
    @Test
    @DisplayName("addLoan crea un prestito valido e lo registra in archivio")
    void addLoanWorks() {
        archive.addUser(user1);
        archive.addBook(book1);

        LocalDate due = LocalDate.now().plusDays(30);

        Loan loan = archive.addLoan(user1, book1, due);

        List<Loan> loans = archive.getLoans();

        assertEquals(1, loans.size());
        assertEquals(loan, loans.get(0));
        assertEquals(user1, loan.getUser());
        assertEquals(book1, loan.getBook());
        assertEquals(due, loan.getDueDate());
        assertTrue(loan.isActive());
    }

    // -------------------------------------------------------------------------
    //                      Ricerche sui prestiti
    // -------------------------------------------------------------------------

    /**
     * @brief Verifica la ricerca di un prestito tramite ID.
     *
     * findLoanById deve restituire il prestito corretto e null per ID inesistente.
     */
    @Test
    @DisplayName("findLoanById trova il prestito corretto")
    void findLoanByIdWorks() {
        archive.addUser(user1);
        archive.addBook(book1);

        Loan l1 = archive.addLoan(user1, book1, LocalDate.now().plusDays(10));
        Loan l2 = archive.addLoan(user1, book1, LocalDate.now().plusDays(20));

        assertEquals(l1, archive.findLoanById(l1.getLoanId()));
        assertEquals(l2, archive.findLoanById(l2.getLoanId()));
        assertNull(archive.findLoanById(9999));
    }

    /**
     * @brief Verifica la ricerca dei prestiti associati a uno specifico utente.
     *
     * findLoansByUser deve restituire tutti e soli i prestiti dell’utente richiesto.
     */
    @Test
    @DisplayName("findLoansByUser restituisce tutti i prestiti dell'utente")
    void findLoansByUserWorks() {
        archive.addUser(user1);
        archive.addUser(user2);
        archive.addBook(book1);

        Loan l1 = archive.addLoan(user1, book1, LocalDate.now().plusDays(10));
        Loan l2 = archive.addLoan(user1, book1, LocalDate.now().plusDays(20));
        archive.addLoan(user2, book1, LocalDate.now().plusDays(30));

        List<Loan> loansUser1 = archive.findLoansByUser(user1);

        assertEquals(2, loansUser1.size());
        assertTrue(loansUser1.contains(l1));
        assertTrue(loansUser1.contains(l2));
    }

    /**
     * @brief Verifica la ricerca dei prestiti associati a uno specifico libro.
     *
     * findLoansByBook deve restituire tutti e soli i prestiti del libro richiesto.
     */
    @Test
    @DisplayName("findLoansByBook restituisce tutti i prestiti del libro")
    void findLoansByBookWorks() {
        archive.addUser(user1);
        archive.addUser(user2);
        archive.addBook(book1);
        archive.addBook(book2);

        Loan l1 = archive.addLoan(user1, book1, LocalDate.now().plusDays(5));
        Loan l2 = archive.addLoan(user2, book1, LocalDate.now().plusDays(10));
        archive.addLoan(user1, book2, LocalDate.now().plusDays(15));

        List<Loan> loansOfBook1 = archive.findLoansByBook(book1);

        assertEquals(2, loansOfBook1.size());
        assertTrue(loansOfBook1.contains(l1));
        assertTrue(loansOfBook1.contains(l2));
    }

    // -------------------------------------------------------------------------
    //                  Prestiti attivi e restituiti
    // -------------------------------------------------------------------------

    /**
     * @brief Verifica l’estrazione dei soli prestiti attivi.
     *
     * getActiveLoans deve includere solo prestiti con status attivo.
     */
    @Test
    @DisplayName("getActiveLoans restituisce solo prestiti attivi")
    void getActiveLoansWorks() {
        archive.addUser(user1);
        archive.addBook(book1);

        Loan l1 = archive.addLoan(user1, book1, LocalDate.now().plusDays(10));
        Loan l2 = archive.addLoan(user1, book1, LocalDate.now().plusDays(20));

        //  chiusura uno dei due
        l1.setStatus(false);

        List<Loan> active = archive.getActiveLoans();

        assertEquals(1, active.size());
        assertSame(l2, active.get(0));
    }

    /**
     * @brief Verifica l’estrazione dei soli prestiti chiusi.
     *
     * getReturnedLoans deve includere solo prestiti con status non attivo.
     */
    @Test
    @DisplayName("getReturnedLoans restituisce solo prestiti chiusi")
    void getReturnedLoansWorks() {
        archive.addUser(user1);
        archive.addBook(book1);

        Loan l1 = archive.addLoan(user1, book1, LocalDate.now().plusDays(10));
        Loan l2 = archive.addLoan(user1, book1, LocalDate.now().plusDays(20));

        l2.setStatus(false);

        List<Loan> returned = archive.getReturnedLoans();

        assertEquals(1, returned.size());
        assertSame(l2, returned.get(0));
    }

    // -------------------------------------------------------------------------
    //                      Serializzazione
    // -------------------------------------------------------------------------

    /**
     * @brief Verifica la serializzazione e deserializzazione dell’archivio.
     *
     * Dopo round-trip di serializzazione, l’archivio deve mantenere libri, utenti e prestiti registrati.
     *
     * @throws Exception In caso di fallimento della serializzazione/deserializzazione.
     */
    @Test
    @DisplayName("LibraryArchive si serializza e deserializza correttamente")
    void serializationWorks() throws Exception {
        archive.addBook(book1);
        archive.addUser(user1);
        Loan l = archive.addLoan(user1, book1, LocalDate.now().plusDays(5));

        // serializzo
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(archive);

        // deserializzo
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bis);
        LibraryArchive restored = (LibraryArchive) in.readObject();

        // verifico contenuto
        assertEquals(1, restored.getBooks().size());
        assertEquals(1, restored.getUsers().size());
        assertEquals(1, restored.getLoans().size());

        assertEquals(book1, restored.findBookByIsbn("ISBN-111"));
        assertEquals(user1, restored.findUserByCode("S123"));
        assertNotNull(restored.findLoanById(l.getLoanId()));
    }
}