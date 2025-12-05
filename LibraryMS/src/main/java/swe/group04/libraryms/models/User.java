/**
 * @file User.java
 * @brief Rappresenta un utente registrato nel sistema della biblioteca.
 */
package swe.group04.libraryms.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @brief Modello di dominio per un utente.
 *
 * Un oggetto User descrive un utente identificato da un codice univoco
 * (matricola), con le principali informazioni anagrafiche e la lista
 * dei prestiti attivi.
 */
public class User {

    /// Spazio degli Attributi
    
    private String firstName;
    private String lastName;
    private String email;
    private final String code; ///< Identificativo univoco: Matricola
    private List<Loan> activeLoans; ///< Prestiti attivamente in corso

    /**
     * @brief Crea un nuovo utente con i dati specificati.
     *
     * Alla creazione la lista dei prestiti attivi è vuota.
     *
     * @pre code != null
     * @post getCode().equals(code)
     * @post getActiveLoans().isEmpty()
     * 
     * @param firstName Nome dell'utente.
     * @param lastName  Cognome dell'utente.
     * @param email     Indirizzo email dell'utente.
     * @param code      Codice univoco (matricola).
     */
    public User(String firstName, String lastName, String email, String code) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.code = code;
        this.activeLoans = new ArrayList<>();   ///< Nessun prestito attivo alla creazione
    }

    /**
     * @brief Restituisce il nome dell'utente.
     *
     * @return Nome.
     */
    public String getFirstName() { 
        return firstName; 
    }
    
    /**
     * @brief Restituisce il cognome dell'utente.
     *
     * @return Cognome.
     */
    public String getLastName() { 
        return lastName; 
    }
    
    /**
     * @brief Restituisce l'indirizzo email dell'utente.
     *
     * @return Email.
     */
    public String getEmail() { 
        return email; 
    }
    
    /**
     * @brief Restituisce il codice identificativo (matricola).
     *
     * @return Codice univoco dell'utente.
     */
    public String getCode() { 
        return code; 
    }
    
    /**
     * @brief Restituisce la lista dei prestiti attivi dell'utente.
     *
     * Viene restituita una copia della lista interna per evitare
     * modifiche dirette allo stato dell'oggetto.
     *
     * @return Copia della lista dei prestiti attivi (può essere vuota).
     */
    public List<Loan> getActiveLoans() {
        return new ArrayList<>(activeLoans);
    }

    /**
     * @brief Imposta il nome dell'utente.
     *
     * @param firstName Nuovo nome.
     */
    public void setFirstName(String firstName) { 
        this.firstName = firstName; 
    }
    
    /**
     * @brief Imposta il cognome dell'utente.
     *
     * @param lastName Nuovo cognome.
     */
    public void setLastName(String lastName) { 
        this.lastName = lastName; 
    }
    
    /**
     * @brief Imposta l'indirizzo email dell'utente.
     *
     * @param email Nuovo indirizzo email.
     */
    public void setEmail(String email) { 
        this.email = email; 
    }
    
    /**
     * @brief Sostituisce la lista dei prestiti attivi.
     *
     * Se il parametro è null, viene impostata una lista vuota.
     * La lista passata viene copiata internamente.
     *
     * @param loans Nuova lista di prestiti attivi (o null).
     */
    public void setActiveLoans(List<Loan> loans) {
        this.activeLoans = (loans != null) ? new ArrayList<>(loans) : new ArrayList<>();
    }

    /**
     * @brief Aggiunge un prestito alla lista dei prestiti attivi.
     *
     * @param loan Prestito da aggiungere.
     */
    public void addLoan(Loan loan) {
        this.activeLoans.add(loan);
    }
    
    /**
     * @brief Rimuove un prestito dalla lista dei prestiti attivi.
     *
     * Se il prestito non è presente, la lista rimane invariata.
     *
     * @param loan Prestito da rimuovere.
     */
    public void removeLoan(Loan loan) {
        this.activeLoans.remove(loan);
    }
    
    /**
     * @brief Verifica se l'utente ha prestiti attivi.
     *
     * @return true se esiste almeno un prestito attivo, false altrimenti.
     */
    public boolean hasActiveLoans() {
        return !activeLoans.isEmpty();
    }

    /**
     * @brief Restituisce il codice hash dell'utente.
     *
     * Ridefinisce hashCode usando il codice identificativo
     * come chiave principale.
     *
     * @return Valore hash dell'oggetto.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.code);
        return hash;
    }
    
    /**
     * @brief Confronta questo utente con un altro oggetto.
     *
     * Ridefinisce equals: due utenti sono considerati uguali
     * se hanno lo stesso codice identificativo.
     *
     * @param obj Oggetto con cui confrontare.
     * @return true se rappresenta lo stesso utente, false altrimenti.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final User other = (User) obj;
        return Objects.equals(this.code, other.code);
    }
    
    /**
     * @brief Restituisce una rappresentazione testuale dell'utente.
     *
     * Ridefinisce toString per mostrare i dati principali
     * e il numero di prestiti attivi.
     *
     * @return Stringa descrittiva dell'utente.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Code: ").append(code).append("\n");
        sb.append("First Name: ").append(firstName).append("\n");
        sb.append("Last Name: ").append(lastName).append("\n");
        sb.append("Email: ").append(email).append("\n");
        sb.append("Active Loans: ").append(activeLoans.size()).append("\n");
        return sb.toString();
    }
}
