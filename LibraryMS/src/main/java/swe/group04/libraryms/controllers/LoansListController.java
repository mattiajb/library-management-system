package swe.group04.libraryms.controllers;

import java.io.IOException;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import swe.group04.libraryms.models.Loan;
import swe.group04.libraryms.service.LoanService;
import swe.group04.libraryms.service.ServiceLocator;

/**
 * @brief Gestisce le operazioni relative alla lista dei prestiti.
 */
public class LoansListController {

    // Pulsanti footer
    @FXML private Button loanDetailsButton;
    @FXML private Button addLoanButton;
    @FXML private Button loanHomeButton;

    // Filtro
    @FXML private ChoiceBox<String> loanFilterChoiceBox;

    // Tabella
    @FXML private TableView<Loan> loanTable;
    @FXML private TableColumn<Loan, String> loanIdColumn;
    @FXML private TableColumn<Loan, String> userColumn;
    @FXML private TableColumn<Loan, String> bookColumn;
    @FXML private TableColumn<Loan, String> startDateColumn;
    @FXML private TableColumn<Loan, String> dueDateColumn;
    @FXML private TableColumn<Loan, String> statusColumn;

    // Service
    private final LoanService loanService = ServiceLocator.getLoanService();

    private ObservableList<Loan> observableLoans;

    // Tiene traccia dell’ultimo filtro applicato.
    private String currentFilter = "Tutti i prestiti";

    /* ============================================================================
                                  INITIALIZE
       ============================================================================ */

    @FXML
    public void initialize() {

        // Cell value factories
        loanIdColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().getLoanId())));

        userColumn.setCellValueFactory(cell -> {
            String code = cell.getValue().getUser() != null
                    ? cell.getValue().getUser().getCode()
                    : "";
            return new SimpleStringProperty(code);
        });

        bookColumn.setCellValueFactory(cell -> {
            String title = cell.getValue().getBook() != null
                    ? cell.getValue().getBook().getTitle()
                    : "";
            return new SimpleStringProperty(title);
        });

        startDateColumn.setCellValueFactory(cell -> {
            String date = cell.getValue().getLoanDate() != null
                    ? cell.getValue().getLoanDate().toString()
                    : "";
            return new SimpleStringProperty(date);
        });

        dueDateColumn.setCellValueFactory(cell -> {
            String date = cell.getValue().getDueDate() != null
                    ? cell.getValue().getDueDate().toString()
                    : "";
            return new SimpleStringProperty(date);
        });

        statusColumn.setCellValueFactory(cell -> {
            Loan l = cell.getValue();
            String status;
            if (l.isActive()) {
                status = loanService.isLate(l) ? "Attivo (in ritardo)" : "Attivo";
            } else {
                status = "Concluso";
            }
            return new SimpleStringProperty(status);
        });

        // Carica dati iniziali
        refreshTable();

        // Imposta filtro
        loanFilterChoiceBox.getItems().setAll("Tutti i prestiti", "Prestiti attivi");
        loanFilterChoiceBox.getSelectionModel().select("Tutti i prestiti");
        loanFilterChoiceBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> applyFilter(newVal));
    }

    /* ============================================================================
                            CARICAMENTO / FILTRI
       ============================================================================ */

    /**
     * Ricarica i dati rispettando il filtro attualmente selezionato.
     */
    private void refreshTable() {
        String selected =
                (loanFilterChoiceBox != null
                        && loanFilterChoiceBox.getSelectionModel().getSelectedItem() != null)
                        ? loanFilterChoiceBox.getSelectionModel().getSelectedItem()
                        : currentFilter;

        applyFilter(selected);
    }

    /**
     * Applica il filtro "Tutti i prestiti" / "Prestiti attivi".
     */
    private void applyFilter(String filter) {
        if (filter == null) {
            filter = "Tutti i prestiti";
        }

        currentFilter = filter;

        List<Loan> list;

        switch (filter) {
            case "Prestiti attivi" -> list = loanService.getActiveLoan();
            default -> list = loanService.getLoansSortedByDueDate();
        }

        if (observableLoans == null) {
            observableLoans = FXCollections.observableArrayList(list);
            loanTable.setItems(observableLoans);
        } else {
            observableLoans.setAll(list);
        }

        loanTable.refresh();
    }

    /* ============================================================================
                               NAVIGAZIONE / AZIONI
       ============================================================================ */

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
            showError("Impossibile tornare alla home.");
        }
    }

    /**
     * @brief Apre la finestra di registrazione di un nuovo prestito.
     */
    @FXML
    private void registerLoan(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/swe/group04/libraryms/view/RegisterLoan.fxml"));
            Parent root = loader.load();

            RegisterLoanController controller = loader.getController();
            controller.setOnLoanRegisteredCallback(this::refreshTable);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/swe/group04/libraryms/css/style.css")
                              .toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Registra prestito");
            stage.setScene(scene);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());

            // in ogni caso, al closing ricarico
            stage.setOnHidden(e -> refreshTable());

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossibile aprire la schermata di registrazione prestito.");
        }
    }

    /**
     * @brief Apre la finestra di dettagli del prestito selezionato.
     */
    @FXML
    private void showLoanDetails(ActionEvent event) {
        Loan selected = loanTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Seleziona un prestito dalla tabella.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/swe/group04/libraryms/view/LoanDetails.fxml"));
            Parent root = loader.load();

            LoanDetailsController controller = loader.getController();
            controller.setLoan(selected);
            controller.setOnLoanUpdatedCallback(this::refreshTable);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/swe/group04/libraryms/css/style.css")
                              .toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Dettagli prestito");
            stage.setScene(scene);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            stage.setOnHidden(e -> refreshTable());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossibile aprire la finestra dei dettagli prestito.");
        }
    }

    /* ============================================================================
                                   UTILITIES
       ============================================================================ */

    private void showError(String msg) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText("Operazione non riuscita");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showWarning(String msg) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Avviso");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}