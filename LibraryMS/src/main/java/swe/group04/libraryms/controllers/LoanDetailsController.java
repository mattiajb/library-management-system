package swe.group04.libraryms.controllers;

import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import swe.group04.libraryms.models.Loan;
import swe.group04.libraryms.service.LoanService;
import swe.group04.libraryms.service.ServiceLocator;

/**
 * @file LoanDetailsController.java
 * @brief Controller per la schermata di dettagli del prestito.
 *
 * Questo controller gestisce la view di dettaglio di un singolo prestito:
 *  - mostra tutte le informazioni principali (utente, libro, date, stato),
 *  - consente di registrare la restituzione del libro (UC17),
 *  - notifica il controller chiamante per aggiornare la lista prestiti.
 *
 * La logica di business (verifica ritardi, aggiornamento stato, copie disponibili, ecc.)
 * è demandata a LoanService; questo controller si limita a:
 *  - orchestrare la chiamata al servizio,
 *  - gestire i messaggi verso l’utente,
 *  - chiudere la finestra dopo le operazioni concluse.
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

    /**
     * @brief Registra la callback da invocare dopo un aggiornamento del prestito.
     *
     * Tipicamente, il chiamante passa un metodo che ricarica la TableView
     * (es. LoansListController::refreshTable).
     *
     * @param callback Azione da eseguire dopo un aggiornamento del prestito (può essere null).
     */
    public void setOnLoanUpdatedCallback(Runnable callback) {
        this.onLoanUpdatedCallback = callback;
    }

    /**
     * @brief Inietta il modello Loan e popola i campi della view.
     *
     * Il metodo:
     *  - salva il riferimento al prestito selezionato,
     *  - aggiorna tutti i campi di sola lettura con le informazioni correnti,
     *  - calcola una stringa di stato ("Attivo", "Attivo (in ritardo)", "Concluso"),
     *  - disabilita il pulsante di restituzione se il prestito è già concluso.
     *
     * @param loan Prestito da visualizzare (può essere null).
     *
     * @post se loan != null, i campi FXML riflettono lo stato del prestito.
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
     * @brief Registra la restituzione del libro per il prestito corrente (UC17).
     *
     * Flusso:
     *  - verifica che sia presente un prestito (loan != null),
     *  - verifica che il prestito sia ancora attivo (altrimenti avvisa l'utente),
     *  - mostra una finestra di conferma (Alert di tipo CONFIRMATION),
     *  - se l'utente conferma, delega a LoanService la registrazione della restituzione
     *    (che aggiorna lo stato del prestito, la data di restituzione e le copie disponibili),
     *  - mostra un messaggio informativo di successo,
     *  - invoca eventuale callback onLoanUpdatedCallback per aggiornare la lista,
     *  - chiude la finestra.
     *
     * Gestione errori:
     *  - se l'utente annulla la conferma → nessuna modifica e nessun errore,
     *  - RuntimeException → segnala un generico errore di sistema con dettagli,
     *    e lascia la finestra aperta.
     *
     * @param event Evento di azione generato dal pulsante "Registra restituzione".
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

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Conferma restituzione");
        confirm.setHeaderText("Vuoi registrare la restituzione di questo prestito?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
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
            showError("Si è verificato un errore durante la registrazione della restituzione.\nDettagli: "+ e.getMessage());
        }
    }

    /**
     * @brief Chiude la schermata di dettagli senza applicare ulteriori modifiche.
     *
     * Usato dal pulsante "Chiudi".
     *
     * @param event Evento di azione generato dal pulsante "Chiudi".
     */
    @FXML
    private void cancelOperation(ActionEvent event) {
        closeStage(event);
    }

    /* ====================== METODI DI SUPPORTO ====================== */

    /**
     * @brief Chiude lo Stage associato al nodo sorgente dell'evento.
     *
     * Metodo di utilità riutilizzato sia dopo la registrazione della restituzione,
     * sia al semplice annullamento/chiusura.
     *
     * @param event Evento proveniente da un controllo appartenente alla finestra corrente.
     *
     * @pre  event.getSource() è un Node appartenente a una Scene non nulla.
     * @post La finestra (Stage) associata è chiusa.
     */
    private void closeStage(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource())
                .getScene().getWindow();
        stage.close();
    }

    /**
     * @brief Mostra un messaggio di errore bloccante.
     *
     * Usato per segnalare problemi di sistema o vincoli violati non recuperabili
     * nella singola operazione.
     *
     * @param message Testo da mostrare all'utente.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText("Operazione non riuscita");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * @brief Mostra un messaggio di avviso non bloccante.
     *
     * Usato per segnalare all'utente condizioni particolari
     * (es. prestito già concluso) che non comportano eccezioni.
     *
     * @param message Testo da mostrare all'utente.
     */
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Avviso");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * @brief Mostra un messaggio informativo di successo.
     *
     * Usato ad esempio dopo la corretta registrazione della restituzione.
     *
     * @param message Testo da mostrare all'utente.
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informazione");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}