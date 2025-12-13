/**
 * @file UserService.java
 * @brief Servizio applicativo per la gestione degli utenti.
 *
 * Incapsula la logica di business relativa agli utenti:
 * - inserimento, aggiornamento, rimozione;
 * - validazione dei campi obbligatori e del formato email;
 * - vincoli di unicità della matricola;
 * - ricerche e ordinamenti sulla lista degli utenti;
 * - persistenza delle modifiche tramite LibraryArchiveService.
 *
 * @note Il servizio opera sull'archivio corrente fornito da LibraryArchiveService.
 */
package swe.group04.libraryms.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import swe.group04.libraryms.exceptions.*;
import swe.group04.libraryms.models.LibraryArchive;
import swe.group04.libraryms.models.*;

/**
 * @brief Implementa la logica di alto livello per la gestione degli utenti.
 *
 * Coordina l'accesso a LibraryArchive e applica controlli/validazioni
 * prima di rendere persistenti le modifiche.
 */
public class UserService {

    /** Servizio per l'accesso e la persistenza dell'archivio. */
    private final LibraryArchiveService libraryArchiveService; // Servizio per la persistenza dell'archivio

    /**
     * @brief Comparatore case-insensitive per ordinare gli utenti per cognome.
     *
     * Se il cognome è null viene considerata la stringa vuota.
     */
    private static final Comparator<User> BY_LASTNAME_COMPARATOR =
            Comparator.comparing(user -> user.getLastName() == null ? "" : user.getLastName().toLowerCase());

    /**
     * @brief Costruttore del servizio.
     *
     * Tipicamente istanziato dal ServiceLocator.
     *
     * @param libraryArchiveService Servizio che fornisce accesso all'archivio e alla persistenza.
     *
     * @pre  libraryArchiveService != null
     * @post this.libraryArchiveService == libraryArchiveService
     *
     * @throws IllegalArgumentException Se libraryArchiveService è nullo.
     */
    public UserService(LibraryArchiveService libraryArchiveService) {

        if (libraryArchiveService == null) {
            throw new IllegalArgumentException("libraryArchiveService non può essere nullo");
        }

        this.libraryArchiveService = libraryArchiveService;
    }

    /**
     * @brief Restituisce l'archivio corrente gestito dal LibraryArchiveService.
     *
     * @return Istanza corrente di LibraryArchive.
     */
    private LibraryArchive getArchive() {
        return libraryArchiveService.getLibraryArchive();
    }

    /**
     * @brief Registra un nuovo utente nel sistema.
     *
     * - valida campi obbligatori;
     * - valida formato email;
     * - verifica unicità della matricola in fase di inserimento;
     * - aggiunge l'utente all'archivio;
     * - persiste le modifiche.
     *
     * @param user Utente da aggiungere.
     *
     * @pre  user != null
     * @pre  libraryArchiveService != null
     *
     * @post L'utente risulta presente nell'archivio (in assenza di eccezioni).
     *
     * @throws MandatoryFieldException Se campi obbligatori non sono validi o la matricola è duplicata.
     * @throws InvalidEmailException   Se l'email è vuota o non rispetta il formato accettato.
     * @throws RuntimeException        Se il salvataggio dell'archivio fallisce (wrapping di IOException).
     */
    public void addUser(User user) throws MandatoryFieldException, InvalidEmailException{
        validateMandatoryFields(user);
        validateEmail(user.getEmail());
        validateMatricolaUniquenessOnAdd(user.getCode());

        getArchive().addUser(user);
        persistChanges();
    }

    /**
     * @brief Aggiorna i dati di un utente esistente.
     *
     * - valida campi obbligatori;
     * - valida email;
     * - verifica che non esista un altro utente con la stessa matricola;
     * - persiste le modifiche.
     *
     * @param user Utente con i dati aggiornati.
     *
     * @pre  user != null
     * @pre  libraryArchiveService != null
     *
     * @post Le modifiche risultano persistite (in assenza di eccezioni).
     *
     * @throws MandatoryFieldException Se campi obbligatori non sono validi o la matricola collide con un altro utente.
     * @throws InvalidEmailException   Se l'email è vuota o non rispetta il formato accettato.
     * @throws RuntimeException        Se il salvataggio dell'archivio fallisce (wrapping di IOException).
     */
    public void updateUser(User user) throws MandatoryFieldException, InvalidEmailException{
        validateMandatoryFields(user);
        validateEmail(user.getEmail());
        validateMatricolaUniquenessOnUpdate(user);

        // L'istanza user è già presente nell'archivio → basta modificarla
        persistChanges();
    }

    /**
     * @brief Rimuove un utente dal sistema.
     *
     * - verifica precondizioni;
     * - controlla prestiti associati all'utente e ne impedisce la rimozione se attivi;
     * - rimuove l'utente dall'archivio;
     * - persiste le modifiche.
     *
     * @param user Utente da rimuovere.
     *
     * @pre  user != null
     * @pre  libraryArchiveService != null
     *
     * @post L'utente non è più presente nell'archivio (in assenza di eccezioni).
     *
     * @throws IllegalArgumentException     Se user è nullo.
     * @throws UserHasActiveLoanException   Se l'utente ha prestiti attivi.
     * @throws RuntimeException             Se il salvataggio dell'archivio fallisce (wrapping di IOException).
     */
    public void removeUser(User user) throws UserHasActiveLoanException{
        if (user == null) {
            throw new IllegalArgumentException("user non può essere nullo");
        }

        List<Loan> loans = getArchive().findLoansByUser(user);
        for (Loan loan : loans) {
            if (loan != null && loan.isActive()) {
                throw new UserHasActiveLoanException(
                        "Impossibile eliminare l'utente: sono presenti prestiti attivi."
                );
            }
        }

        getArchive().removeUser(user);
        persistChanges();
    }

    /**
     * @brief Restituisce la lista degli utenti ordinata per cognome.
     *
     * L'ordinamento è effettuato su una copia della lista restituita dall'archivio.
     *
     * @pre  libraryArchiveService != null
     * @post true
     *
     * @return Lista di utenti ordinata per cognome (mai null).
     */
    public List<User> getUsersSortedByLastName() {
        List<User> list = new ArrayList<>(getArchive().getUsers());
        list.sort(BY_LASTNAME_COMPARATOR);
        return list;
    }

    /**
     * @brief Ricerca utenti nel sistema tramite query testuale.
     *
     * La query viene normalizzata in lower-case e confrontata tramite
     * matching per sottostringa su:
     * - cognome;
     * - nome;
     * - matricola/codice;
     * - email.
     *
     * Se la query è vuota (dopo trim), restituisce tutti gli utenti ordinati per cognome.
     *
     * @param query Testo inserito dall'operatore.
     *
     * @pre  query != null
     * @pre  libraryArchiveService != null
     *
     * @post true
     *
     * @return Lista di utenti che soddisfano il criterio di ricerca (mai null).
     *
     * @throws IllegalArgumentException Se query è null.
     */
    public List<User> searchUsers(String query) {
        if (query == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        String q = query.trim().toLowerCase();
        if (q.isEmpty()) {
            return getUsersSortedByLastName();
        }

        List<User> result = new ArrayList<>();
        for (User u : getArchive().getUsers()) {

            if (u == null) continue;

            boolean match =
                    containsIgnoreCase(u.getLastName(), q) ||
                            containsIgnoreCase(u.getFirstName(), q) ||
                            containsIgnoreCase(u.getCode(), q) ||
                            containsIgnoreCase(u.getEmail(), q);

            if (match) {
                result.add(u);
            }
        }

        result.sort(BY_LASTNAME_COMPARATOR);
        return result;
    }

    /* ---------------------------------------------------------------------- */
    /*                    Metodi di validazione interna                        */
    /* ---------------------------------------------------------------------- */

    /**
     * @brief Verifica la presenza dei campi obbligatori dell'utente.
     */
    private void validateMandatoryFields(User user) throws MandatoryFieldException {

        if (user == null) {
            throw new MandatoryFieldException("L'utente non può essere nullo.");
        }

        if (isBlank(user.getFirstName())) {
            throw new MandatoryFieldException("Il nome è obbligatorio.");
        }

        if (isBlank(user.getLastName())) {
            throw new MandatoryFieldException("Il cognome è obbligatorio.");
        }

        if (isBlank(user.getCode())) {
            throw new MandatoryFieldException("La matricola è obbligatoria.");
        }

        if (isBlank(user.getEmail())) {
            throw new MandatoryFieldException("L'email è obbligatoria.");
        }
    }

    /**
     * @brief Valida l'indirizzo email dell'utente.
     */
    private void validateEmail(String email) throws InvalidEmailException {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidEmailException("L'email non può essere vuota.");
        }

        if(!email.trim().matches("^[A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\\.)*unisa\\.it$")) {
            throw new InvalidEmailException("Email non valida. Sono ammessi solo indirizzi che terminano con 'unisa.it'.");
        }
    }

    /**
     * @brief Verifica unicità della matricola in fase di inserimento.
     */
    private void validateMatricolaUniquenessOnAdd(String code) throws MandatoryFieldException {

        User existing = getArchive().findUserByCode(code);

        if (existing != null) {
            throw new MandatoryFieldException("Esiste già un utente registrato con la stessa matricola.");
        }
    }

    /**
     * @brief Verifica unicità della matricola in fase di aggiornamento.
     */
    private void validateMatricolaUniquenessOnUpdate(User user) throws MandatoryFieldException {

        User existing = getArchive().findUserByCode(user.getCode());

        if (existing != null && existing != user) {
            throw new MandatoryFieldException("Esiste già un altro utente con la stessa matricola.");
        }
    }

    /* ---------------------------------------------------------------------- */
    /*                Metodi di supporto e persistenza                         */
    /* ---------------------------------------------------------------------- */

    /**
     * @brief Verifica se un campo contiene la query (case-insensitive).
     */
    private boolean containsIgnoreCase(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    /**
     * @brief Verifica se una stringa è nulla o composta solo da spazi.
     */
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * @brief Persiste le modifiche dell'archivio tramite LibraryArchiveService.
     */
    private void persistChanges() {
        try {
            libraryArchiveService.saveArchive(getArchive());
        } catch (IOException e) {
            throw new RuntimeException(
                    "Errore durante il salvataggio dell'archivio utenti.",e);
        }
    }
}