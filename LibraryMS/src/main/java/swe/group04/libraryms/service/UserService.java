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

import java.util.List;
import swe.group04.libraryms.exceptions.*;
import swe.group04.libraryms.models.LibraryArchive;
import swe.group04.libraryms.models.User;

/**
 * @brief Implementa la logica di alto livello per la gestione degli utenti.
 *
 * Utilizza l'archivio centrale della biblioteca e, se necessario,
 * i servizi di persistenza per mantenere allineato lo stato degli utenti.
 */
public class UserService {
    
    private LibraryArchive libraryArchive;
    private LibraryArchiveService libraryArchiveService;
    
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
        return null;
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
        return null;
    }
}
