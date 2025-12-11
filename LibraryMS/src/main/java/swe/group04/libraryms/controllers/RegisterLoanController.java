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
import swe.group04.libraryms.models.LibraryArchive;
import swe.group04.libraryms.models.Loan;
import swe.group04.libraryms.models.User;
import swe.group04.libraryms.service.LibraryArchiveService;
import swe.group04.libraryms.service.LoanService;
import swe.group04.libraryms.service.ServiceLocator;

/**
 * @brief Gestisce l'operazione di registrazione di un nuovo prestito
 *        all'interno del sistema della biblioteca.
 *
 * Il controller:
 *  - legge i dati inseriti (matricola utente, ISBN, data restituzione),
 *  - recupera utente e libro dall'archivio,
 *  - delega a LoanService la registrazione del prestito,
 *  - gestisce errori e messaggi all'utente.
 */
public class RegisterLoanController {

    @FXML private TextField userCodeField;
    @FXML private TextField bookIsbnField;
    @FXML private DatePicker dueDatePicker;
    @FXML private Button saveLoanButton;
    @FXML private Button cancelButton;

    /** Servizi applicativi */
    private final LoanService loanService =
            ServiceLocator.getLoanService();
    private final LibraryArchiveService archiveService =
            ServiceLocator.getArchiveService();

    /** Callback opzionale per aggiornare la lista prestiti dopo l'inserimento. */
    private Runnable onLoanRegisteredCallback;

    /**
     * Permette al chiamante (LoansListController) di registrare una callback
     * che verrà eseguita dopo la registrazione corretta del prestito.
     */
    public void setOnLoanRegisteredCallback(Runnable callback) {
        this.onLoanRegisteredCallback = callback;
    }

    /**
     * @brief Conferma la registrazione del prestito.
     *
     * Collegato al bottone "Conferma" (onAction="#confirmLoan").
     */
    @FXML
    private void confirmLoan(ActionEvent event) {
        try {
            String userCode = safeTrim(userCodeField.getText());
            String isbn     = safeTrim(bookIsbnField.getText());
            LocalDate due   = dueDatePicker.getValue();

            if (userCode.isEmpty() || isbn.isEmpty() || due == null) {
                showError("Compila tutti i campi (utente, libro, data di restituzione).");
                return;
            }

            LibraryArchive archive = archiveService.getLibraryArchive();
            if (archive == null) {
                showError("Archivio non disponibile. Impossibile registrare il prestito.");
                return;
            }

            User user = archive.findUserByCode(userCode);
            if (user == null) {
                showError("Utente non trovato. Controlla la matricola inserita.");
                return;
            }

            Book book = archive.findBookByIsbn(isbn);
            if (book == null) {
                showError("Libro non trovato. Controlla l'ISBN inserito.");
                return;
            }

            Loan loan = loanService.registerLoan(user, book, due);

            showInfo("Prestito registrato correttamente.\nID prestito: "
                    + loan.getLoanId());

            if (onLoanRegisteredCallback != null) {
                onLoanRegisteredCallback.run();
            }

            closeStage(event);

        } catch (MandatoryFieldException |
                 NoAvailableCopiesException |
                 MaxLoansReachedException ex) {
            showError(ex.getMessage());
        } catch (RuntimeException ex) {
            showError("Si è verificato un errore durante la registrazione del prestito.\nDettagli: "
                    + ex.getMessage());
        }
    }

    /**
     * @brief Annulla l'operazione e chiude la finestra.
     */
    @FXML
    private void cancelOperation(ActionEvent event) {
        closeStage(event);
    }

    /* ====================== METODI DI SUPPORTO ====================== */

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