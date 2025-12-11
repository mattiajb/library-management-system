package swe.group04.libraryms.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import swe.group04.libraryms.exceptions.InvalidEmailException;
import swe.group04.libraryms.exceptions.MandatoryFieldException;
import swe.group04.libraryms.models.User;
import swe.group04.libraryms.service.ServiceLocator;
import swe.group04.libraryms.service.UserService;

public class AddUserController {

    // ---------------------------------------------------------
    // SERVICE
    // ---------------------------------------------------------

    private final UserService userService = ServiceLocator.getUserService();

    // Callback verso il controller chiamante
    private Runnable onUserAddedCallback;

    // ---------------------------------------------------------
    // FXML
    // ---------------------------------------------------------

    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextField surnameField;
    @FXML private TextField emailField;

    // ---------------------------------------------------------
    // CALLBACK REGISTRATION
    // ---------------------------------------------------------

    /** Registrata dal controller chiamante (UsersListController) */
    public void setOnUserAddedCallback(Runnable callback) {
        this.onUserAddedCallback = callback;
    }

    // ---------------------------------------------------------
    // SAVE USER
    // ---------------------------------------------------------

    @FXML
    private void saveUser(ActionEvent event) {

        try {
            // --- Lettura campi ---
            String code = codeField.getText().trim();
            String firstName = nameField.getText().trim();
            String lastName = surnameField.getText().trim();
            String email = emailField.getText().trim();

            // --- Creazione modello ---
            User user = new User(firstName, lastName, email, code);

            // --- Business logic ---
            userService.addUser(user);

            // --- Feedback positivo ---
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Utente aggiunto");
            alert.setContentText("L’utente è stato registrato correttamente.");
            alert.showAndWait();

            // --- Callback verso lista utenti ---
            if (onUserAddedCallback != null) {
                onUserAddedCallback.run();
            }

            closeWindow(event);

        } catch (MandatoryFieldException | InvalidEmailException ex) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Dati non validi");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();

        } catch (RuntimeException ex) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Errore di sistema");
            alert.setContentText("Impossibile aggiungere l’utente.");
            alert.showAndWait();
            ex.printStackTrace();
        }
    }

    // ---------------------------------------------------------
    // CANCEL
    // ---------------------------------------------------------

    @FXML
    private void cancelOperation(ActionEvent event) {
        closeWindow(event);
    }

    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();
        stage.close();
    }
}
