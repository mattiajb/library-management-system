/**
 * @file UserService.java
 * @brief Servizio applicativo per la gestione degli utenti.
 *
 * Questa classe incapsula la logica di business relativa agli utenti:
 * - registrazione e aggiornamento dei dati utente
 * - rimozione dal sistema
 * - ricerche e ordinamenti sulla lista degli utenti
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
 * Utilizza l'archivio centrale della biblioteca e, se necessario,
 * i servizi di persistenza per mantenere allineato lo stato degli utenti.
 */
public class UserService {

    private final LibraryArchiveService libraryArchiveService; // Servizio per la persistenza dell'archivio

    // Comparatore per cognome
    private static final Comparator<User> BY_LASTNAME_COMPARATOR = Comparator.comparing(user -> user.getLastName() == null ? "" : user.getLastName().toLowerCase());

    /**
     * Costruttore usato dal ServiceLocator.
     */
    public UserService(LibraryArchiveService libraryArchiveService) {

        if (libraryArchiveService == null) {
            throw new IllegalArgumentException("libraryArchiveService non può essere nullo");
        }

        this.libraryArchiveService = libraryArchiveService;
    }

    /**
     * Restituisce sempre l’archivio aggiornato.
     */
    private LibraryArchive getArchive() {
        return libraryArchiveService.getLibraryArchive();
    }

    /**
     * @brief Registra un nuovo utente nel sistema.
     *
     * Esegue i controlli sui campi obbligatori e sulla validità dell'email.
     *
     * @pre  user != null
     * @pre  libraryArchive != null
     *
     * @param user Utente da aggiungere.
     *
     * @throws MandatoryFieldException Se uno o più campi obbligatori non sono validi.
     * @throws InvalidEmailException   Se l'indirizzo email non rispetta il formato atteso
     *                                 o viola vincoli di unicità.
     *
     * @note Metodo ancora da implementare: il corpo è vuoto.
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
     * @pre  user != null
     * @pre  libraryArchive != null
     * @pre  L'utente esiste già nell'archivio.
     *
     * @param user Utente con i dati aggiornati.
     *
     * @throws MandatoryFieldException Se i nuovi dati violano vincoli di obbligatorietà.
     * @throws InvalidEmailException   Se l'email aggiornata non è valida o crea duplicati.
     *
     * @note Metodo ancora da implementare: il corpo è vuoto.
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
     * Prima della rimozione possono essere verificati eventuali prestiti attivi.
     *
     * @pre  user != null
     * @pre  libraryArchive != null
     *
     * @param user Utente da rimuovere.
     *
     * @throws UserHasActiveLoanException Se l'utente ha ancora prestiti attivi.
     *
     * @note Metodo ancora da implementare: il corpo è vuoto.
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
     * @pre  libraryArchive != null
     *
     * @return Lista di utenti ordinata alfabeticamente per cognome
     *         (può essere vuota), oppure null finché il metodo
     *         non viene implementato.
     */
    public List<User> getUsersSortedByLastName() {
        List<User> list = new ArrayList<>(getArchive().getUsers());
        list.sort(BY_LASTNAME_COMPARATOR);
        return list;
    }
    
    /**
     * @brief Ricerca utenti in base a una stringa di query.
     *
     * La query può essere interpretata come parte del nome, cognome,
     * codice utente o email, a seconda della logica implementata.
     *
     * @pre  query != null
     * @pre  libraryArchive != null
     *
     * @param query Testo inserito dall'operatore.
     *
     * @return Sottoinsieme degli utenti che corrispondono ai criteri di ricerca
     *         (può essere vuoto), oppure null finché il metodo non viene implementato.
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

    private void validateEmail(String email) throws InvalidEmailException {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidEmailException("L'email non può essere vuota.");
        }
        
        if(!email.trim().matches("^[A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\\.)*unisa\\.it$")) {
            throw new InvalidEmailException("Email non valida. Sono ammessi solo indirizzi che terminano con 'unisa.it'.");
        }
    }

    private void validateMatricolaUniquenessOnAdd(String code) throws MandatoryFieldException {

        User existing = getArchive().findUserByCode(code);

        if (existing != null) {
            throw new MandatoryFieldException("Esiste già un utente registrato con la stessa matricola.");
        }
    }

    private void validateMatricolaUniquenessOnUpdate(User user) throws MandatoryFieldException {

        User existing = getArchive().findUserByCode(user.getCode());

        if (existing != null && existing != user) {
            throw new MandatoryFieldException("Esiste già un altro utente con la stessa matricola.");
        }
    }

    /* ---------------------------------------------------------------------- */
    /*                Metodi di supporto e persistenza                         */
    /* ---------------------------------------------------------------------- */

    private boolean containsIgnoreCase(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void persistChanges() {
        try {
            libraryArchiveService.saveArchive(getArchive());
        } catch (IOException e) {
            throw new RuntimeException(
                    "Errore durante il salvataggio dell'archivio utenti.",e);
        }
    }
}