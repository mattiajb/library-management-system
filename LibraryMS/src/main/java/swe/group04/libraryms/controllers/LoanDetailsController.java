package swe.group04.libraryms.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import swe.group04.libraryms.models.Loan;
import swe.group04.libraryms.service.LoanService;
import swe.group04.libraryms.service.ServiceLocator;

/**
 * @brief Controller per la schermata di dettagli del prestito.
 *
 * Mostra tutte le informazioni del prestito selezionato e permette
 * di registrare la restituzione (UC17).
 */
public class LoanDetailsController {

    // CAMPI FORM
    @FXML private TextField loanIdField;
    @FXML private TextField userCodeField;
    @FXML private TextField userNameField;
    @FXML private TextField bookIsbnField;
    @FXML private TextField bookTitleField;
    @FXML private TextField loanDateField;
    @FXML private TextField dueDateField;
    @FXML private TextField returnDateField;
    @FXML private TextField statusField;

    @FXML private Button registerReturnButton;
    @FXML private Button closeButton;

    private final LoanService loanService = ServiceLocator.getLoanService();

    private Loan loan;

    /** Callback opzionale per chiedere al chiamante di fare refresh. */
    private Runnable onLoanUpdatedCallback;

    public void setOnLoanUpdatedCallback(Runnable callback) {
        this.onLoanUpdatedCallback = callback;
    }

    /**
     * @brief Inietta il modello Loan e popola i campi della view.
     */
    public void setLoan(Loan loan) {
        this.loan = loan;
        if (loan == null) {
            return;
        }

        loanIdField.setText(String.valueOf(loan.getLoanId()));

        if (loan.getUser() != null) {
            userCodeField.setText(loan.getUser().getCode());
            userNameField.setText(
                    loan.getUser().getFirstName() + " " + loan.getUser().getLastName()
            );
        } else {
            userCodeField.setText("");
            userNameField.setText("");
        }

        if (loan.getBook() != null) {
            bookIsbnField.setText(loan.getBook().getIsbn());
            bookTitleField.setText(loan.getBook().getTitle());
        } else {
            bookIsbnField.setText("");
            bookTitleField.setText("");
        }

        loanDateField.setText(
                loan.getLoanDate() != null ? loan.getLoanDate().toString() : ""
        );
        dueDateField.setText(
                loan.getDueDate() != null ? loan.getDueDate().toString() : ""
        );
        returnDateField.setText(
                loan.getReturnDate() != null ? loan.getReturnDate().toString() : "-"
        );

        String status;
        if (loan.isActive()) {
            status = loanService.isLate(loan) ? "Attivo (in ritardo)" : "Attivo";
        } else {
            status = "Concluso";
        }
        statusField.setText(status);

        // Se il prestito è già concluso, disabilito il bottone
        registerReturnButton.setDisable(!loan.isActive());
    }

    /**
     * @brief Registra la restituzione del libro (UC17).
     */
    @FXML
    private void registerReturn(ActionEvent event) {
        if (loan == null) {
            showError("Nessun prestito selezionato.");
            return;
        }

        if (!loan.isActive()) {
            showWarning("Il prestito risulta già concluso.");
            return;
        }

        try {
            loanService.returnLoan(loan);

            showInfo("Restituzione registrata correttamente.");

            if (onLoanUpdatedCallback != null) {
                onLoanUpdatedCallback.run();
            }

            closeStage(event);

        } catch (RuntimeException e) {
            e.printStackTrace();
            showError("Si è verificato un errore durante la registrazione della restituzione.\nDettagli: "
                    + e.getMessage());
        }
    }

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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText("Operazione non riuscita");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Avviso");
        alert.setHeaderText(null);
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