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
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import swe.group04.libraryms.models.Loan;
import swe.group04.libraryms.service.LoanService;
import swe.group04.libraryms.service.ServiceLocator;

/**
 * @file LoansListController.java
 * @brief Controller responsabile della gestione della lista dei prestiti.
 *
 * Questo controller gestisce la schermata che mostra l’elenco dei prestiti
 * della biblioteca e consente di:
 *  - visualizzare tutti i prestiti o solo quelli attivi,
 *  - evidenziare graficamente i prestiti scaduti (in ritardo),
 *  - aprire la schermata di registrazione di un nuovo prestito (UC16),
 *  - aprire la schermata di dettaglio di un prestito selezionato (UC17),
 *  - tornare alla schermata principale del sistema.
 *
 * La logica di business sui prestiti (ordinamenti, filtraggio attivi, calcolo ritardo,
 * registrazione restituzione, ecc.) è demandata a LoanService. Questo controller
 * si occupa di collegare i dati alla view, reagire agli eventi UI e gestire
 * messaggi e navigazione.
 */
public class LoansListController {

    //  Pulsanti footer
    @FXML private Button loanDetailsButton;
    @FXML private Button addLoanButton;
    @FXML private Button loanHomeButton;

    //  Filtro
    @FXML private ChoiceBox<String> loanFilterChoiceBox;

    //  Tabella
    @FXML private TableView<Loan> loanTable;
    @FXML private TableColumn<Loan, String> loanIdColumn;
    @FXML private TableColumn<Loan, String> userColumn;
    @FXML private TableColumn<Loan, String> bookColumn;
    @FXML private TableColumn<Loan, String> startDateColumn;
    @FXML private TableColumn<Loan, String> dueDateColumn;
    @FXML private TableColumn<Loan, String> statusColumn;

    //  Service
    private final LoanService loanService = ServiceLocator.getLoanService();

    private ObservableList<Loan> observableLoans;

    //  Tiene traccia dell’ultimo filtro applicato.
    private String currentFilter = "Tutti i prestiti";


    /**
     * @brief Inizializza il controller e configura la tabella dei prestiti.
     *
     * Questo metodo viene chiamato automaticamente da JavaFX dopo il
     * caricamento del file FXML. Le principali operazioni sono:
     *  - associazione delle colonne alle proprietà del modello Loan,
     *  - definizione di una rowFactory per evidenziare i prestiti scaduti,
     *  - caricamento iniziale dei dati tramite refreshTable(),
     *  - configurazione del filtro tramite ChoiceBox.
     */
    @FXML
    public void initialize() {

        //  Cell value factories
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
                status = loanService.isLate(l) ? "In ritardo" : "Attivo";
            } else {
                status = "Concluso";
            }
            return new SimpleStringProperty(status);
        });

        //  Evidenziazione grafica delle righe con prestito scaduto
        loanTable.setRowFactory(tv -> new TableRow<Loan>() {
            @Override
            protected void updateItem(Loan loan, boolean empty) {
                super.updateItem(loan, empty);

                //  Prima tolgo sempre la classe, per sicurezza
                getStyleClass().remove("overdue-row");

                if (!empty && loan != null && isOverdue(loan)) {
                    if (!getStyleClass().contains("overdue-row")) {
                        getStyleClass().add("overdue-row");
                    }
                }
            }
        });

        //  Carica dati iniziali
        refreshTable();

        //  Imposta filtro
        loanFilterChoiceBox.getItems().setAll("Tutti i prestiti", "Prestiti attivi");
        loanFilterChoiceBox.getSelectionModel().select("Tutti i prestiti");
        loanFilterChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilter(newVal));
    }

    /**
     * @brief Verifica se un prestito deve essere considerato "in ritardo".
     *
     * Un prestito è considerato "overdue" se:
     *  - risulta ancora attivo (loan.isActive() == true),
     *  - LoanService.isLate(loan) restituisce true.
     *
     * @param loan Prestito da verificare (può essere null).
     * @return true se il prestito è attivo e in ritardo; false altrimenti.
     */
    private boolean isOverdue(Loan loan) {
        if (loan == null) {
            return false;
        }
        //  Uso la logica di dominio già presente nel service
        return loan.isActive() && loanService.isLate(loan);
    }

    /* ============================================================================
                            CARICAMENTO / FILTRI
       ============================================================================ */

    /**
     * @brief Ricarica i dati in tabella rispettando il filtro corrente.
     *
     * Se la ChoiceBox del filtro è disponibile e ha un valore selezionato,
     * viene utilizzato quel valore; altrimenti si ripiega su currentFilter.
     *
     * @post la TableView mostra un elenco coerente con il filtro selezionato.
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
     * @brief Applica il filtro "Tutti i prestiti" / "Prestiti attivi".
     *
     * La logica di filtraggio è delegata a LoanService:
     *  - "Prestiti attivi" → getActiveLoan()
     *  - altri valori      → getLoansSortedByDueDate()
     *
     * @param filter Testo del filtro selezionato nella ChoiceBox (può essere null).
     *
     * @post observableLoans contiene l’elenco filtrato dei prestiti,
     *       e la TableView viene aggiornata di conseguenza.
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

    /**
     * @brief Torna alla schermata principale dell’applicazione.
     *
     * Carica il file FXML della main view e lo imposta come scena
     * dello Stage corrente.
     *
     * In caso di errore di caricamento, mostra un messaggio d’errore
     * e lascia invariata la schermata corrente.
     *
     * @param event Evento di azione generato dal pulsante "Torna alla home".
     */
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
     * @brief Torna alla schermata principale dell’applicazione.
     *
     * Carica il file FXML della main view e lo imposta come scena
     * dello Stage corrente.
     *
     * In caso di errore di caricamento, mostra un messaggio d’errore
     * e lascia invariata la schermata corrente.
     *
     * @param event Evento di azione generato dal pulsante "Torna alla home".
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
     * @brief Apre la finestra di dettagli del prestito selezionato (UC17).
     *
     * Flusso:
     *  - verifica che l’utente abbia selezionato un prestito nella tabella,
     *    altrimenti mostra un messaggio di avviso;
     *  - carica LoanDetails.fxml e ne recupera il controller;
     *  - inietta il prestito selezionato tramite setLoan(Loan);
     *  - registra una callback per aggiornare la lista prestiti
     *    dopo la restituzione (setOnLoanUpdatedCallback);
     *  - apre la finestra dei dettagli e, alla chiusura, esegue refreshTable().
     *
     * In caso di problemi nel caricamento della view, viene mostrato un
     * messaggio di errore all’utente.
     *
     * @param event Evento di azione generato dal pulsante "Visualizza dettagli".
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



    /**
     * @brief Mostra un messaggio di errore bloccante.
     *
     * Usato per segnalare problemi di caricamento schermate o altre
     * operazioni non riuscite nella gestione dei prestiti.
     *
     * @param msg Testo del messaggio da mostrare.
     */
    private void showError(String msg) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText("Operazione non riuscita");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * @brief Mostra un messaggio di avviso all’utente.
     *
     * Usato per condizioni non eccezionali, come la mancata selezione
     * di un prestito nella tabella.
     *
     * @param msg Testo del messaggio da mostrare.
     */
    private void showWarning(String msg) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Avviso");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}