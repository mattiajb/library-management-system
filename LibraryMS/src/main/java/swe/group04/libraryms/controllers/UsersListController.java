/**
 * @file UserController.java
 * @brief Controller responsabile della gestione delle operazioni sugli utenti.
 */
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
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import swe.group04.libraryms.models.User;

/**
 * @brief Gestisce le operazioni relative alla visualizzazione, creazione,
 *        modifica ed eliminazione degli utenti del sistema della biblioteca.
 *
 * Il controller media tra l'interfaccia utente e la logica applicativa
 * per tutte le funzionalità riguardanti la gestione degli utenti.
 */
public class UsersListController {

    @FXML
    private Button userDetailsButton;
    @FXML
    private Button addUserButton;
    @FXML
    private Button userHomeButton;
    @FXML
    private TextField userSearchField;
    @FXML
    private ChoiceBox<?> userFilterChoiceBox;
    @FXML
    private TableView<?> userTable;
    @FXML
    private TableColumn<?, ?> codeClm;
    @FXML
    private TableColumn<?, ?> firstNameClm;
    @FXML
    private TableColumn<?, ?> lastNameClm;
    @FXML
    private TableColumn<?, ?> emailClm;
    @FXML
    private TableColumn<?, ?> activeLoansClm;

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

@FXML
public void addUser(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/swe/group04/libraryms/view/AddUser.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/swe/group04/libraryms/css/style.css").toExternalForm());

        Stage addUserStage = new Stage();
        addUserStage.setTitle("Aggiungi utente");
        addUserStage.setScene(scene);

        Stage owner = (Stage) ((Node) event.getSource()).getScene().getWindow();
        addUserStage.initOwner(owner);

        addUserStage.show();

    } catch (IOException e) {
        e.printStackTrace();
        // TODO: eventualmente Alert all'utente
    }
}
}
