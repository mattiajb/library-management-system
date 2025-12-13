/**
 * @file LoanService.java
 * @brief Servizio applicativo per la gestione dei prestiti.
 *
 * Incapsula la logica di business relativa ai prestiti:
 * - registrazione di nuovi prestiti;
 * - registrazione delle restituzioni;
 * - interrogazione e filtraggio dei prestiti attivi;
 * - ordinamento dei prestiti per data di scadenza;
 * - verifica dello stato di ritardo.
 *
 * Il servizio delega l’accesso e la persistenza dell’archivio
 * a LibraryArchiveService.
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
 * Applica vincoli e regole di business sui prestiti, operando sull’istanza
 * corrente di LibraryArchive fornita da LibraryArchiveService.
 *
 * @note Le operazioni che modificano lo stato (registrazione prestito/restituzione)
 *       eseguono persistenza immediata tramite persistChanges().
 */
public class LoanService {

    //  Servizio per l'accesso e la persistenza dell'archivio.
    private final LibraryArchiveService libraryArchiveService; //<  Servizio per la persistenza dell'archivio

    /**
     * @brief Comparatore per ordinare i prestiti per data di scadenza (dueDate).
     *
     * Le scadenze nulle sono poste in fondo (nullsLast).
     */
    private static final Comparator<Loan> BY_DUEDATE_COMPARATOR = Comparator.comparing(Loan::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()));


    /**
     * @brief Costruttore del servizio.
     *
     * @param libraryArchiveService Servizio che fornisce accesso all'archivio e alla persistenza.
     *
     * @pre  libraryArchiveService != null
     * @post this.libraryArchiveService == libraryArchiveService
     *
     * @throws IllegalArgumentException Se libraryArchiveService è nullo.
     */
    public LoanService(LibraryArchiveService libraryArchiveService) {
        if (libraryArchiveService == null) {
            throw new IllegalArgumentException("libraryArchiveService non può essere nullo");
        }
        this.libraryArchiveService = libraryArchiveService;
    }

    /**
     * @brief Helper per ottenere l’archivio corrente.
     *
     * @return Istanza corrente di LibraryArchive gestita da LibraryArchiveService.
     */
    private LibraryArchive getArchive() {
        return libraryArchiveService.getLibraryArchive();
    }

    /* ================================================================
                             REGISTRAZIONE PRESTITO
     ================================================================= */

    /**
     * @brief Registra un nuovo prestito.
     *
     * - crea un nuovo Loan nell'archivio tramite getArchive().addLoan(...);
     * - decrementa le copie disponibili del libro (book.decrementAvailableCopies());
     * - persiste l'archivio aggiornato tramite persistChanges().
     *
     * @param user Utente che richiede il prestito.
     * @param book Libro da prestare.
     * @param dueDate Data di restituzione prevista.
     *
     * @pre  libraryArchiveService != null
     *
     * @post Il prestito è presente nell'archivio e le copie disponibili del libro risultano decrementate,
     *       in assenza di eccezioni.
     *
     * @return Il prestito creato.
     *
     * @throws MandatoryFieldException     Se user/book/dueDate sono null, oppure se dueDate è precedente a oggi.
     * @throws NoAvailableCopiesException  Se il libro non ha copie disponibili.
     * @throws MaxLoansReachedException    Se l'utente ha già raggiunto il limite di 3 prestiti attivi.
     * @throws RuntimeException            Se la persistenza fallisce (wrapping di IOException).
     */
    public Loan registerLoan(User user, Book book, LocalDate dueDate)
            throws MandatoryFieldException,
            NoAvailableCopiesException,
            MaxLoansReachedException {

        if (user == null) {
            throw new MandatoryFieldException("Utente non valido.");
        }
        if (book == null) {
            throw new MandatoryFieldException("Libro non valido.");
        }
        if (dueDate == null) {
            throw new MandatoryFieldException("La data di restituzione prevista è obbligatoria.");
        }

        //  Verifica disponibilità copie
        if (!book.hasAvailableCopies()) {
            throw new NoAvailableCopiesException("Non ci sono copie disponibili per questo libro.");
        }

        //  Verifica limite massimo prestiti attivi per utente
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
        
        //  Verifica correttezza data di scadenza
        if (dueDate.isBefore(LocalDate.now())) {
            throw new MandatoryFieldException(
                    "La data di restituzione non può essere precedente alla data odierna.");
        }

        //  Creazione effettiva del prestito
        Loan loan = getArchive().addLoan(user, book, dueDate);

        //  Aggiornamento copie disponibili del libro
        book.decrementAvailableCopies();

        //  Persistenza
        persistChanges();

        return loan;
    }


    /* ================================================================
                                RESTITUZIONE PRESTITO
       ================================================================ */

    /**
     * @brief Registra la restituzione del libro per un prestito.
     *
     * - imposta la returnDate del prestito a LocalDate.now();
     * - imposta lo stato del prestito a concluso (loan.setStatus(false));
     * - incrementa le copie disponibili del libro associato, se presente;
     * - persiste l'archivio aggiornato tramite persistChanges().
     *
     * @param loan Prestito da chiudere (restituire).
     *
     * @pre  libraryArchiveService != null
     *
     * @post Il prestito risulta chiuso, con returnDate impostata, e le copie disponibili del libro incrementate,
     *       in assenza di eccezioni.
     *
     * @throws MandatoryFieldException Se loan è nullo oppure se il prestito risulta già chiuso.
     * @throws RuntimeException        Se la persistenza fallisce (wrapping di IOException).
     */
    public void returnLoan(Loan loan) {
        if (loan == null) {
            throw new MandatoryFieldException("Il prestito non può essere nullo.");
        }

        if (!loan.isActive()) {
            throw new MandatoryFieldException("Il prestito risulta già chiuso.");
        }

        //  Aggiorna data restituzione
        loan.setReturnDate(LocalDate.now());

        //  Aggiorna stato
        loan.setStatus(false);

        //  Incrementa copie del libro
        Book book = loan.getBook();
        if (book != null) {
            book.incrementAvailableCopies();
        }

        //  Persistenza
        persistChanges();
    }

    /* ================================================================
                           PRESTITI ATTIVI E STATI
       ================================================================ */

    /**
     * @brief Restituisce tutti i prestiti attivi.
     *
     * Un prestito è considerato attivo se loan.isActive() == true.
     * L’elenco restituito viene ordinato per dueDate tramite BY_DUEDATE_COMPARATOR.
     *
     * @pre  libraryArchiveService != null
     * @post true
     *
     * @return Lista dei prestiti attualmente attivi (può essere vuota, mai null).
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
     * @brief Determina se un prestito è in ritardo.
     *
     * Un prestito è in ritardo se:
     * - è attivo (loan.isActive() == true);
     * - la data di scadenza (dueDate) è precedente alla data corrente.
     *
     * @param loan Prestito da valutare.
     *
     * @pre  true
     * @post true
     *
     * @return true se il prestito è in ritardo, false altrimenti (anche per input null o non attivi).
     */
    public boolean isLate(Loan loan) {

        if (loan == null || !loan.isActive() || loan.getDueDate() == null) {
            return false;
        }

        return loan.getDueDate().isBefore(LocalDate.now());
    }

    /**
     * @brief Restituisce tutti i prestiti ordinati per data di scadenza.
     *
     * L’ordinamento avviene su una copia della lista ottenuta dall’archivio,
     * quindi non modifica l’ordine interno mantenuto da LibraryArchive.
     *
     * @pre  libraryArchiveService != null
     * @post true
     *
     * @return Lista di prestiti ordinata per dueDate (può essere vuota, mai null).
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
     * @brief Persiste le modifiche dell'archivio tramite LibraryArchiveService.
     *
     * Converte eventuali IOException in RuntimeException, poiché un fallimento
     * della persistenza rappresenta un errore applicativo.
     *
     * @throws RuntimeException Se il salvataggio fallisce (wrapping di IOException).
     */
    private void persistChanges() {
        try {
            libraryArchiveService.saveArchive(getArchive());
        } catch (IOException e) {
            throw new RuntimeException("Errore durante il salvataggio dei prestiti.", e);
        }
    }
}