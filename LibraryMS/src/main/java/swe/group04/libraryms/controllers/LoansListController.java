package swe.group04.libraryms.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import swe.group04.libraryms.models.Loan;

/**
 * @brief Gestisce le operazioni relative alla lista dei prestiti.
 */
public class LoansListController {

    /// Pulsanti del footer
    @FXML
    private Button loanDetailsButton;
    @FXML
    private Button returnLoanButton;
    @FXML
    private Button addLoanButton;
    @FXML
    private Button loanHomeButton;

    /// Controllo di filtro (Tutti i prestiti / Prestiti attivi)
    @FXML
    private ChoiceBox<String> loanFilterChoiceBox;

    /// Tabella e colonne
    @FXML
    private TableView<Loan> loanTable;
    @FXML
    private TableColumn<Loan, String> loanIdColumn;
    @FXML
    private TableColumn<Loan, String> userColumn;
    @FXML
    private TableColumn<Loan, String> bookColumn;
    @FXML
    private TableColumn<Loan, String> startDateColumn;
    @FXML
    private TableColumn<Loan, String> dueDateColumn;
    @FXML
    private TableColumn<Loan, String> statusColumn;

    @FXML
    public void initialize() {
        if (loanFilterChoiceBox != null && !loanFilterChoiceBox.getItems().isEmpty()) {
            loanFilterChoiceBox.getItems().stream()
                    .filter(s -> "Tutti i prestiti".equals(s))
                    .findFirst()
                    .ifPresent(loanFilterChoiceBox::setValue);
        }

    }

    @FXML
    private void backHome(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/swe/group04/libraryms/view/main.fxml"));

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/swe/group04/libraryms/css/style.css")
                              .toExternalForm());

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();
            stage.setTitle("Library Management System");
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
            // TODO: eventualmente Alert all'utente
        }
    }

    /**
     * @brief Avvia il flusso di registrazione di un nuovo prestito (UC14).
     *
     * Apre una nuova finestra "Registra prestito" (RegisterLoan.fxml)
     * controllata da RegisterLoanController.
     */
    @FXML
    private void registerLoan(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/swe/group04/libraryms/view/RegisterLoan.fxml"));
            Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/swe/group04/libraryms/css/style.css").toExternalForm());

        Stage registerLoanStage = new Stage();
        registerLoanStage.setTitle("Registra prestito");
        registerLoanStage.setScene(scene);

        Stage owner = (Stage) ((Node) event.getSource()).getScene().getWindow();
        registerLoanStage.initOwner(owner);

        registerLoanStage.show();

    } catch (IOException e) {
        e.printStackTrace();
        // TODO: eventualmente Alert all'utente
    }
}

    @FXML
    private void registerReturn(ActionEvent event) {
        // TODO: implementare UC17 (restituzione)
    }

    /**
     * @brief Metodo di utilità richiamabile dal controller figlio dopo
     *        l'inserimento di un nuovo prestito per aggiornare la tabella.
     */
    public void refreshLoansTable() {
        // TODO: ricaricare i prestiti dal LoanService / ArchiveService
        // e aggiornare loanTable.setItems(...)
    }
}