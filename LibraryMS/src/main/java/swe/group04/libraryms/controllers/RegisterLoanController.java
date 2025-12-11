/**
 * @file RegisterLoanController.java
 * @brief Controller responsabile della registrazione di un nuovo prestito.
 */

package swe.group04.libraryms.controllers;

import java.time.LocalDate;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import swe.group04.libraryms.exceptions.MandatoryFieldException;
import swe.group04.libraryms.exceptions.MaxLoansReachedException;
import swe.group04.libraryms.exceptions.NoAvailableCopiesException;
import swe.group04.libraryms.models.Book;
import swe.group04.libraryms.models.User;
import swe.group04.libraryms.service.LoanService;
import swe.group04.libraryms.service.ServiceLocator;

/**
 * @brief Gestisce le operazioni relative alla creazione di un prestito
 *        all'interno del sistema della biblioteca.
 *
 * Il controller:
 *  - legge e valida i dati inseriti nel form (utente, libro, data di restituzione);
 *  - recupera gli oggetti User e Book a partire dagli identificativi;
 *  - delega a LoanService la logica di business (controlli su copie disponibili,
 *    numero massimo di prestiti, ecc.);
 *  - gestisce la chiusura della finestra e la visualizzazione dei messaggi
 *    di errore / conferma.
 */
public class RegisterLoanController {

    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    /// Identificativo dell'utente (es. matricola / codice)
    @FXML
    private TextField userCodeField;

    /// Identificativo del libro (es. ISBN)
    @FXML
    private TextField bookIsbnField;

    /// Data di restituzione prevista
    @FXML
    private DatePicker dueDatePicker;

    /** Servizio prestiti ottenuto tramite ServiceLocator */
    private final LoanService loanService = ServiceLocator.getLoanService();

    /**
     * @brief Inizializza lo stato della vista "Registra prestito".
     *
     * Imposta, se necessario, una data di restituzione predefinita.
     */
    @FXML
    public void initialize() {
        if (dueDatePicker != null && dueDatePicker.getValue() == null) {
            // Default: 14 giorni da oggi (modificabile in seguito)
            dueDatePicker.setValue(LocalDate.now().plusDays(14));
        }
    }

    /**
     * @brief Annulla l'operazione e chiude la finestra corrente.
     */
    @FXML
    private void cancelOperation(ActionEvent event) {
        closeStage(event);
    }

    /**
     * @brief Legge i dati dal form, valida e registra un nuovo prestito.
     *
     * Gestione errori:
     *  - campi obbligatori mancanti o non coerenti (MandatoryFieldException)
     *  - utente con troppi prestiti attivi (MaxLoansReachedException)
     *  - libro senza copie disponibili (NoAvailableCopiesException)
     *  - errori di persistenza / runtime generici (RuntimeException)
     *
     * In caso di successo:
     *  - viene mostrato un messaggio di conferma
     *  - la finestra viene chiusa
     */
    @FXML
    private void saveLoan(ActionEvent event) {
        try {
            
            // Lettura input e validazioni base
            String userCode = safeTrim(userCodeField.getText());
            String bookIsbn = safeTrim(bookIsbnField.getText());
            LocalDate dueDate = (dueDatePicker != null) ? dueDatePicker.getValue() : null;

            if (userCode.isEmpty() || bookIsbn.isEmpty() || dueDate == null) {
                showError("Tutti i campi sono obbligatori (utente, libro, data di restituzione).");
                return;
            }

            if (dueDate.isBefore(LocalDate.now())) {
                showError("La data di restituzione non può essere nel passato.");
                return;
            }

            // Recupero di User e Book a partire da userCode / ISBN
            User user = null;
            Book book = null;

            // TODO: Sostituire con la logica corretta per ottenere
            //       User e Book a partire da userCode e bookIsbn.
            //
            // Esempi possibili (da adattare al tuo progetto):
            //   user = ServiceLocator.getUserService().findByCode(userCode);
            //   book = ServiceLocator.getBookService().findByIsbn(bookIsbn);
            //
            // oppure usando direttamente LibraryArchiveService:
            //   LibraryArchive archive = ServiceLocator.getArchiveService().getLibraryArchive();
            //   user = archive.findUserByCode(userCode);
            //   book = archive.findBookByIsbn(bookIsbn);

            if (user == null) {
                showError("Utente non trovato. Verifica il codice inserito.");
                return;
            }

            if (book == null) {
                showError("Libro non trovato. Verifica l'ISBN inserito.");
                return;
            }

            // Logica di business delegata a LoanService
            loanService.registerLoan(user, book, dueDate);

            // Successo: conferma e chiusura
            showInfo("Prestito registrato correttamente.");
            closeStage(event);

        } catch (MandatoryFieldException e) {
            showError(e.getMessage());
        } catch (MaxLoansReachedException e) {
            showError(e.getMessage());
        } catch (NoAvailableCopiesException e) {
            showError(e.getMessage());
        } catch (RuntimeException e) {
            showError("Si è verificato un errore durante la registrazione del prestito.\nDettagli: "
                    + e.getMessage());
        }
    }

    /* ------------------------------------------------------------------
                          METODI DI SUPPORTO
       ------------------------------------------------------------------ */

    private void closeStage(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource())
                .getScene().getWindow();
        stage.close();
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText("Operazione non riuscita");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informazione");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}