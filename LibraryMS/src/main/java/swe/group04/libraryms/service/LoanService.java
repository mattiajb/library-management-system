/**
 * @file LoanService.java
 * @brief Servizio applicativo per la gestione dei prestiti.
 *
 * Questa classe incapsula la logica di business relativa ai prestiti:
 * - registrazione di nuovi prestiti
 * - registrazione delle restituzioni
 * - interrogazione e filtraggio dei prestiti attivi
 */
package swe.group04.libraryms.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import swe.group04.libraryms.exceptions.*;
import swe.group04.libraryms.models.Book;
import swe.group04.libraryms.models.LibraryArchive;
import swe.group04.libraryms.models.Loan;
import swe.group04.libraryms.models.User;

/**
 * @brief Implementa la logica di alto livello per la gestione dei prestiti.
 *
 * Utilizza l'archivio centrale della biblioteca e, se necessario,
 * i servizi di persistenza per mantenere allineato lo stato dei prestiti.
 */
public class LoanService {

    private final LibraryArchiveService libraryArchiveService; // Servizio per la persistenza dell'archivio

    // Comparatore per data di restituzione prevista
    private static final Comparator<Loan> BY_DUEDATE_COMPARATOR =
            Comparator.comparing(Loan::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()));


    /**
     * Costruttore utilizzato dal ServiceLocator.
     */
    public LoanService(LibraryArchiveService libraryArchiveService) {
        if (libraryArchiveService == null) {
            throw new IllegalArgumentException("libraryArchiveService non può essere nullo");
        }
        this.libraryArchiveService = libraryArchiveService;
    }

    /** Helper: accede sempre all'archivio aggiornato */
    private LibraryArchive getArchive() {
        return libraryArchiveService.getLibraryArchive();
    }

    /* ================================================================
                             REGISTRAZIONE PRESTITO
     ================================================================= */

    /**
     * @brief Registra un nuovo prestito.
     *
     * Vincoli applicati:
     *  - user != null, book != null, dueDate != null,
     *  - il libro deve avere copie disponibili,
     *  - l'utente deve avere meno di 3 prestiti attivi,
     *  - la data di scadenza non può essere precedente alla data corrente.
     *
     * @return Il prestito creato.
     */
    public Loan registerLoan(User user, Book book, LocalDate dueDate)
            throws MandatoryFieldException,
            NoAvailableCopiesException,
            MaxLoansReachedException {

        /* --- Controlli di base (campi obbligatori) --- */

        if (user == null) {
            throw new MandatoryFieldException("Utente non valido.");
        }
        if (book == null) {
            throw new MandatoryFieldException("Libro non valido.");
        }
        if (dueDate == null) {
            throw new MandatoryFieldException("La data di restituzione prevista è obbligatoria.");
        }

        /* --- Verifica disponibilità copie --- */

        if (!book.hasAvailableCopies()) {
            throw new NoAvailableCopiesException("Non ci sono copie disponibili per questo libro.");
        }

        /* --- Verifica limite massimo prestiti attivi per utente --- */

        int activeLoans = 0;
        List<Loan> loansByUser = getArchive().findLoansByUser(user);
        for(Loan loan : loansByUser) {
            if(loan.isActive()) {
                activeLoans++;
            }
        }
        if (activeLoans >= 3) {
            throw new MaxLoansReachedException("L'utente ha già raggiunto il limite di 3 prestiti attivi.");
        }

        /* --- Verifica correttezza data di scadenza --- */

        if (dueDate.isBefore(LocalDate.now())) {
            throw new MandatoryFieldException(
                    "La data di restituzione non può essere precedente alla data odierna.");
        }

        /* --- Creazione effettiva del prestito --- */

        Loan loan = getArchive().addLoan(user, book, dueDate);

        /* --- Aggiornamento copie disponibili del libro --- */

        book.decrementAvailableCopies();

        /* --- Persistenza --- */

        persistChanges();

        return loan;
    }


    /* ================================================================
                                RESTITUZIONE PRESTITO
       ================================================================ */

    /**
     * @brief Registra la restituzione del libro per un prestito.
     *
     * Effetti:
     *  - imposta la returnDate del prestito,
     *  - incrementa le copie disponibili del libro,
     *  - salva l'archivio aggiornato.
     *
     * Vincoli:
     *  - il prestito deve essere attivo,
     *  - non deve essere già restituito.
     */
    public void returnLoan(Loan loan) {
        if (loan == null) {
            throw new MandatoryFieldException("Il prestito non può essere nullo.");
        }

        if (!loan.isActive()) {
            throw new MandatoryFieldException("Il prestito risulta già chiuso.");
        }

        /* --- Aggiorna data restituzione --- */

        loan.setReturnDate(LocalDate.now());

        /* --- Aggiorna stato --- */

        loan.setStatus(false);

        /* --- Incrementa copie del libro --- */

        Book book = loan.getBook();
        if (book != null) {
            book.incrementAvailableCopies();
        }

        /* --- Persistenza --- */

        persistChanges();
    }

    /* ================================================================
                           PRESTITI ATTIVI E STATI
       ================================================================ */

    /**
     * @brief Restituisce tutti i prestiti attivi.
     *
     * @pre  libraryArchive != null
     *
     * @return Lista dei prestiti attualmente attivi (può essere vuota),
     *         oppure null finché il metodo non viene implementato.
     */
    public List<Loan> getActiveLoan() {

        List<Loan> result = new ArrayList<>();

        for (Loan loan : getArchive().getLoans()) {
            if (loan != null && loan.isActive()) {
                result.add(loan);
            }
        }

        result.sort(BY_DUEDATE_COMPARATOR);
        return result;
    }

    /**
     * @brief Indica se un prestito è in ritardo.
     *
     * Un prestito è in ritardo se:
     *  - è ancora attivo,
     *  - la data dovuta è precedente a oggi.
     */
    public boolean isLate(Loan loan) {

        if (loan == null || !loan.isActive() || loan.getDueDate() == null) {
            return false;
        }

        return loan.getDueDate().isBefore(LocalDate.now());
    }

    /**
     * @brief Restituisce tutti i prestiti ordinati per data di scadenza.
     */
    public List<Loan> getLoansSortedByDueDate() {
        List<Loan> list = new ArrayList<>(getArchive().getLoans());
        list.sort(BY_DUEDATE_COMPARATOR);
        return list;
    }

    /* ================================================================
                             METODI INTERNI
       ================================================================ */

    /**
     * Salva l'archivio aggiornato tramite LibraryArchiveService.
     */
    private void persistChanges() {
        try {
            libraryArchiveService.saveArchive(getArchive());
        } catch (IOException e) {
            throw new RuntimeException("Errore durante il salvataggio dei prestiti.", e);
        }
    }
}
