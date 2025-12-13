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
 * @file UsersListController.java
 * @brief Controller responsabile della gestione della lista utenti.
 *
 * Questo controller gestisce la schermata che mostra l’elenco degli utenti
 * registrati alla biblioteca e consente di:
 *  - visualizzare tutti gli utenti con le relative informazioni principali,
 *  - ordinare/filtro gli utenti per nome, cognome o stato prestiti,
 *  - cercare utenti per testo libero (nome, cognome, matricola, ecc.),
 *  - aprire la finestra di aggiunta di un nuovo utente,
 *  - aprire la finestra di dettagli/modifica di un utente selezionato,
 *  - tornare alla schermata principale del sistema.
 *
 * La logica di business (ricerca, ordinamenti, vincoli su prestiti, ecc.)
 * è delegata a UserService e LoanService. Questo controller si occupa
 * di collegare i dati alla view e reagire agli eventi dell’interfaccia utente.
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

    /** Acceso tramite ServiceLocator */
    private final UserService userService = ServiceLocator.getUserService();
    private final LoanService loanService = ServiceLocator.getLoanService();

    /** Lista osservabile popolata dai dati del servizio */
    private ObservableList<User> observableUsers;

    private String currentFilter = "Mostra tutti";

    /**
     * @brief Inizializza il controller e configura la tabella utenti.
     *
     * Viene chiamato automaticamente da JavaFX dopo il caricamento del FXML.
     *
     * Operazioni principali:
     *  - associazione delle colonne alle proprietà del modello User,
     *  - calcolo del numero di prestiti attivi per ogni utente,
     *  - caricamento iniziale della lista utenti tramite refreshTable(),
     *  - inizializzazione della ChoiceBox di filtro/ordinamento,
     *  - attivazione della ricerca in tempo reale sul campo userSearchField.
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


    /**
     * @brief Ricarica la tabella degli utenti con l’ordinamento di default.
     *
     * Recupera la lista utenti dal servizio (ordinata per cognome) e la
     * riversa nella lista osservabile collegata alla table view.
     *
     * @post userTable viene popolata con la lista restituita da UserService.
     */
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

    /**
     * @brief Applica il filtro di ricerca testuale sugli utenti.
     *
     * La logica di filtraggio è delegata a UserService.searchUsers(),
     * che ritorna gli utenti corrispondenti alla query (nome, cognome, matricola…).
     *
     * Se la query è nulla o vuota, viene ripristinata la lista completa
     * tramite refreshTable().
     *
     * @param query Testo inserito dall’utente nel campo di ricerca.
     */
    private void applySearch(String query) {
        if (query == null || query.isBlank()) {
            refreshTable();
            return;
        }

        List<User> filtered = userService.searchUsers(query);
        observableUsers.setAll(filtered);
    }

    /**
     * @brief Applica il filtro/ordinamento selezionato nella ChoiceBox.
     *
     * Filtri/ordinamenti gestiti:
     *  - "Ordina per nome"         → ordina la lista attualmente visibile per firstName;
     *  - "Ordina per cognome"      → richiede al servizio la lista ordinata per lastName;
     *  - "Solo prestiti attivi"    → mostra solo gli utenti che hanno almeno un prestito attivo;
     *  - "Solo prestiti non attivi"→ mostra gli utenti che non hanno prestiti attivi;
     *  - "Mostra tutti" (default)  → ricarica la lista completa ordinata per cognome.
     *
     * @param selected Testo della voce selezionata nella ChoiceBox (può essere null).
     *
     * @post observableUsers contiene l’elenco degli utenti coerente con il filtro scelto
     *       e la tabella viene aggiornata.
     */
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


    /**
     * @brief Apre la finestra di aggiunta di un nuovo utente.
     *
     * Flusso:
     *  - carica AddUser.fxml e il relativo controller,
     *  - registra una callback su AddUserController per aggiornare la tabella
     *    al termine dell’inserimento (onUserAddedCallback),
     *  - apre una nuova finestra per la compilazione del form.
     *
     * In caso di IOException durante il caricamento del FXML, viene mostrato
     * un messaggio di errore e la schermata corrente rimane invariata.
     *
     * @param event Evento di azione generato dal pulsante "Aggiungi utente".
     */
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

    /**
     * @brief Apre la finestra di dettagli/modifica dell’utente selezionato.
     *
     * Flusso:
     *  - recupera l’utente selezionato nella tabella; se nessuno è selezionato,
     *    mostra un messaggio di avviso e interrompe l’operazione;
     *  - carica UserDetails.fxml e il relativo controller;
     *  - inietta il modello User tramite UserDetailsController.setUser(User);
     *  - apre una nuova finestra con i dettagli dell’utente;
     *  - al closing della finestra di dettagli, richiama refreshTable()
     *    per aggiornare l’elenco con le eventuali modifiche.
     *
     * @param event Evento di azione generato dal pulsante "Visualizza dettagli".
     */
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


    /**
     * @brief Torna alla schermata principale dell’applicazione.
     *
     * Carica la main view (main.fxml) e sostituisce la scena nello Stage corrente.
     * In caso di errore di caricamento, viene solo stampato lo stack trace
     * e la schermata corrente resta invariata.
     *
     * @param event Evento di azione generato dal pulsante "Torna alla home".
     */
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


    /**
     * @brief Mostra un messaggio di errore bloccante all’utente.
     *
     * Utilizzato per segnalare problemi di caricamento schermate o altre
     * operazioni non riuscite nella gestione degli utenti.
     *
     * @param msg Testo del messaggio da mostrare.
     */
    private void showError(String msg) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * @brief Mostra un messaggio di avviso all’utente.
     *
     * Utilizzato per condizioni non eccezionali (es. nessun utente selezionato
     * in tabella quando si richiede la visualizzazione dei dettagli).
     *
     * @param msg Testo del messaggio da mostrare.
     */
    private void showWarning(String msg) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setContentText(msg);
        alert.showAndWait();
    }


}
