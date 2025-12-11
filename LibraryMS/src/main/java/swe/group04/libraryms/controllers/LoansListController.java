package swe.group04.libraryms.controllers;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
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
import javafx.scene.control.*;
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
    private String currentFilter = "Tutti i prestiti";

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /* ==================== INIZIALIZZAZIONE ==================== */

    @FXML
    public void initialize() {

        loanIdColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().getLoanId())));

        userColumn.setCellValueFactory(cell -> {
            var u = cell.getValue().getUser();
            String text = (u == null) ? "-" : (u.getFirstName() + " " + u.getLastName());
            return new SimpleStringProperty(text);
        });

        bookColumn.setCellValueFactory(cell -> {
            var b = cell.getValue().getBook();
            String text = (b == null) ? "-" : b.getTitle();
            return new SimpleStringProperty(text);
        });

        startDateColumn.setCellValueFactory(cell -> {
            var d = cell.getValue().getLoanDate();
            String text = (d == null) ? "-" : d.format(DATE_FMT);
            return new SimpleStringProperty(text);
        });

        dueDateColumn.setCellValueFactory(cell -> {
            var d = cell.getValue().getDueDate();
            String text = (d == null) ? "-" : d.format(DATE_FMT);
            return new SimpleStringProperty(text);
        });

        statusColumn.setCellValueFactory(cell -> {
            Loan l = cell.getValue();
            String status;
            if (!l.isActive()) {
                status = "Concluso";
            } else if (loanService.isLate(l)) {
                status = "Scaduto";
            } else {
                status = "Attivo";
            }
            return new SimpleStringProperty(status);
        });

        refreshTable();

        loanFilterChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldV, newV) -> applyFilter(newV));

        if (!loanFilterChoiceBox.getItems().isEmpty()) {
            loanFilterChoiceBox.setValue("Tutti i prestiti");
        }
    }

    /* ==================== DATI + FILTRO ==================== */

    private void refreshTable() {
        List<Loan> loans = loanService.getLoansSortedByDueDate();

        if (observableLoans == null) {
            observableLoans = FXCollections.observableArrayList(loans);
            loanTable.setItems(observableLoans);
        } else {
            observableLoans.setAll(loans);
        }

        applyFilter(currentFilter);
    }

    private void applyFilter(String selected) {
        if (selected == null) return;
        currentFilter = selected;

        List<Loan> base = loanService.getLoansSortedByDueDate();
        List<Loan> filtered;

        switch (selected) {
            case "Prestiti attivi" ->
                    filtered = base.stream()
                            .filter(Loan::isActive)
                            .toList();

            default ->  // "Tutti i prestiti"
                    filtered = base;
        }

        observableLoans.setAll(filtered);
        loanTable.refresh();
    }

    /* ==================== NAVIGAZIONE ==================== */

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

    /* ==================== AZIONI BOTTONI ==================== */

    @FXML
    private void showLoanDetails(ActionEvent event) {
        Loan selected = loanTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Seleziona un prestito dalla tabella.");
            return;
        }

        // Per ora solo stub: più avanti collegheremo LoanDetails.fxml
        showInfo("Dettagli prestito non ancora implementati.\nID: "
                + selected.getLoanId());
    }

    @FXML
    private void registerLoan(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/swe/group04/libraryms/view/RegisterLoan.fxml"));

            Parent root = loader.load();

            // Recupero il controller della finestra figlia
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

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Errore durante il caricamento della schermata di registrazione prestito.");
        }
    }

    /* ==================== UTILITIES UI ==================== */

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText("Operazione non riuscita");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showWarning(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attenzione");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informazione");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}