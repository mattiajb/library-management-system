package swe.group04.libraryms.models;

import swe.group04.libraryms.models.Loan;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {

    // Spazio degli Attributi
    private String firstName;
    private String lastName;
    private String email;
    private final String code; // Identificativo univoco: Matricola
    private List<Loan> activeLoans; // Prestiti attivamente in corso

    // Costruttore
    public User(String firstName, String lastName, String email, String code) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.code = code;
        this.activeLoans = new ArrayList<>();   // Nessun prestito attivo alla creazione
    }

    // Getter
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getCode() { return code; }

    public List<Loan> getActiveLoans() {
        return new ArrayList<>(activeLoans);
    }

    // Setter
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }

    public void setActiveLoans(List<Loan> loans) {
        this.activeLoans = (loans != null) ? new ArrayList<>(loans) : new ArrayList<>();
    }

    // Metodi di utilità
    public void addLoan(Loan loan) {
        this.activeLoans.add(loan);
    }

    public void removeLoan(Loan loan) {
        this.activeLoans.remove(loan);
    }

    public boolean hasActiveLoans() {
        return !activeLoans.isEmpty();
    }

    // equals/hashCode basati su code
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.code);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final User other = (User) obj;
        return Objects.equals(this.code, other.code);
    }

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
