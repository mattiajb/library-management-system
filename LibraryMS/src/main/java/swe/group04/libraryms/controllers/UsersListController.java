/**
 * @file UserController.java
 * @brief Controller responsabile della gestione delle operazioni sugli utenti.
 */
package swe.group04.libraryms.controllers;

import java.io.IOException;
import java.util.List;

import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import swe.group04.libraryms.models.Book;
import swe.group04.libraryms.models.User;
import swe.group04.libraryms.service.BookService;
import swe.group04.libraryms.service.LoanService;
import swe.group04.libraryms.service.ServiceLocator;
import swe.group04.libraryms.service.UserService;

/**
 * @brief Gestisce le operazioni relative alla visualizzazione, creazione,
 *        modifica ed eliminazione degli utenti del sistema della biblioteca.
 *
 * Il controller media tra l'interfaccia utente e la logica applicativa
 * per tutte le funzionalità riguardanti la gestione degli utenti.
 */
public class UsersListController {

    @FXML private Button userDetailsButton;
    @FXML private Button addUserButton;
    @FXML private Button userHomeButton;
    @FXML private TextField userSearchField;
    @FXML private ChoiceBox<String> userFilterChoiceBox;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> codeClm;
    @FXML private TableColumn<User, String> firstNameClm;
    @FXML private TableColumn<User, String> lastNameClm;
    @FXML private TableColumn<User, String> emailClm;
    @FXML private TableColumn<User, Integer> activeLoansClm;

    // ---------------------------------------------------------
    // SERVICE
    // ---------------------------------------------------------

    /** Acceso tramite ServiceLocator */
    private final UserService userService = ServiceLocator.getUserService();
    private final LoanService loanService = ServiceLocator.getLoanService();

    /** Lista osservabile popolata dai dati del servizio */
    private ObservableList<User> observableUsers;

    private String currentFilter = "Mostra tutti";

    /* ============================================================================
                              INIZIALIZZAZIONE
    ============================================================================ */
    /**
     * @brief Inizializza controller e tabella libri.
     *
     * Viene chiamato automaticamente da JavaFX dopo il caricamento del FXML.
     */
    public void initialize() {

        codeClm.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getCode()));

        firstNameClm.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getFirstName()));

        lastNameClm.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getLastName()));

        emailClm.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getEmail()));

        activeLoansClm.setCellValueFactory(cell -> {
            int count = loanService
                    .getActiveLoan()
                    .stream()
                    .filter(l -> l.getUser().equals(cell.getValue()))
                    .toList()
                    .size();

            return new SimpleIntegerProperty(count).asObject();
        });

        refreshTable();

        userFilterChoiceBox.getItems().setAll(
                "Ordina per nome",
                "Ordina per cognome",
                "Solo prestiti attivi",
                "Solo prestiti non attivi",
                "Mostra tutti"
        );

        userFilterChoiceBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, o, n) -> applyFilter(n));

        userSearchField.textProperty()
                .addListener((obs, o, n) -> applySearch(n));

    }


    /* ============================================================================
                               CARICAMENTO + AGGIORNAMENTO DATI
    ============================================================================ */

    private void refreshTable() {
        List<User> users = userService.getUsersSortedByLastName();

        if (observableUsers == null) {
            observableUsers = FXCollections.observableArrayList(users);
            userTable.setItems(observableUsers);
        } else {
            observableUsers.setAll(users);
        }

        userTable.refresh();
    }

    private void applySearch(String query) {
        if (query == null || query.isBlank()) {
            refreshTable();
            return;
        }

        List<User> filtered = userService.searchUsers(query);
        observableUsers.setAll(filtered);
    }

    private void applyFilter(String selected) {

        if (selected == null) return;

        currentFilter = selected;

        List<User> list;

        switch (selected) {
            case "Ordina per nome" ->
                    list = observableUsers.stream()
                            .sorted((u1, u2) ->
                                    u1.getFirstName()
                                            .compareToIgnoreCase(u2.getFirstName()))
                            .toList();

            case "Ordina per cognome" ->
                    list = userService.getUsersSortedByLastName();

            case "Solo prestiti attivi" ->
                    list = observableUsers.stream()
                            .filter(u ->
                                    loanService.getActiveLoan()
                                            .stream()
                                            .anyMatch(l -> l.getUser().equals(u)))
                            .toList();

            case "Solo prestiti non attivi" ->
                    list = observableUsers.stream()
                            .filter(u ->
                                    loanService.getActiveLoan()
                                            .stream()
                                            .noneMatch(l -> l.getUser().equals(u)))
                            .toList();

            default -> // Mostra tutti
                    list = userService.getUsersSortedByLastName();
        }

        observableUsers.setAll(list);
        userTable.refresh();
    }

        /* ============================================================================
                                  NAVIGAZIONE
       ============================================================================ */


    @FXML
    public void addUser(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/swe/group04/libraryms/view/AddUser.fxml"));

            Parent root = loader.load();
            AddUserController controller = loader.getController();
            controller.setOnUserAddedCallback(this::refreshTable);

            Stage stage = new Stage();
            stage.setTitle("Aggiungi utente");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            showError("Impossibile aprire la finestra di aggiunta utente.");
        }
    }

    @FXML
    public void showUserDetails(ActionEvent event) {
        // 1) Recupero l'utente selezionato
        User selected = userTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Seleziona un utente dalla tabella.");
            return;
        }

        try {
            // 2) Carico lo UserDetails.fxml
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/swe/group04/libraryms/view/UserDetails.fxml"));

            Parent root = loader.load();

            // 3) Recupero il controller e inietto il modello
            UserDetailsController controller = loader.getController();
            controller.setUser(selected);

            // 4) Apro una nuova finestra
            Stage stage = new Stage();
            stage.setTitle("Dettagli utente");
            stage.setScene(new Scene(root));

            // Quando chiudo la finestra → ricarico la tabella
            stage.setOnHidden(e -> refreshTable());

            // Imposto il proprietario (finestra lista utenti)
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossibile aprire la finestra dei dettagli utente.");
        }
    }


    @FXML
    private void backHome(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/swe/group04/libraryms/view/main.fxml"));

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/swe/group04/libraryms/css/style.css").toExternalForm());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Library Management System");
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
            // TODO: eventualmente Alert all'utente
        }
    }


    /* ============================================================================
                                   UTILITIES
       ============================================================================ */

    private void showError(String msg) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showWarning(String msg) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setContentText(msg);
        alert.showAndWait();
    }


}
