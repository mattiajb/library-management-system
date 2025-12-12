package swe.group04.libraryms.controllers;

import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import swe.group04.libraryms.exceptions.InvalidEmailException;
import swe.group04.libraryms.exceptions.MandatoryFieldException;
import swe.group04.libraryms.exceptions.UserHasActiveLoanException;
import swe.group04.libraryms.models.User;
import swe.group04.libraryms.service.ServiceLocator;
import swe.group04.libraryms.service.UserService;

public class UserDetailsController {

    // ---------------------------------------------------------
    // SERVICE
    // ---------------------------------------------------------

    private final UserService userService = ServiceLocator.getUserService();

    // ---------------------------------------------------------
    // MODEL
    // ---------------------------------------------------------

    private User user;

    // ---------------------------------------------------------
    // FXML
    // ---------------------------------------------------------

    @FXML private Button saveButton;
    @FXML private Button deleteButton;
    @FXML private Button cancelButton;

    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextField surnameField;
    @FXML private TextField emailField;
    @FXML private TextField activeLoansField;

    /* ============================================================
                       INIEZIONE MODELLO
       ============================================================ */

    /** Chiamato dal controller chiamante */
    public void setUser(User user) {
        this.user = user;
        loadUserData();
    }

    /* ============================================================
                         CARICAMENTO DATI
       ============================================================ */

    private void loadUserData() {
        if (user == null) return;

        codeField.setText(user.getCode());
        nameField.setText(user.getFirstName());
        surnameField.setText(user.getLastName());
        emailField.setText(user.getEmail());
        activeLoansField.setText(String.valueOf(user.getActiveLoans()));

        // Campi non modificabili
        codeField.setDisable(true);
        activeLoansField.setDisable(true);
    }

    /* ============================================================
                        SALVATAGGIO MODIFICHE
       ============================================================ */

    @FXML
    private void saveChanges(ActionEvent event) {

        if (user == null) return;

        try {
            // --- Lettura nuovi valori ---
            String newFirstName = nameField.getText().trim();
            String newLastName = surnameField.getText().trim();
            String newEmail = emailField.getText().trim();

            // --- Aggiornamento modello ---
            user.setFirstName(newFirstName);
            user.setLastName(newLastName);
            user.setEmail(newEmail);

            // --- Persistenza ---
            userService.updateUser(user);

            // --- Feedback ---
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Modifiche salvate");
            alert.setContentText("Le informazioni dell’utente sono state aggiornate.");
            alert.showAndWait();

            closeWindow(event);

        } catch (MandatoryFieldException | InvalidEmailException ex) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Dati non validi");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();

        } catch (RuntimeException ex) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Errore di sistema");
            alert.setContentText("Impossibile salvare le modifiche.");
            alert.showAndWait();
            ex.printStackTrace();
        }
    }

    /* ============================================================
                          ELIMINAZIONE UTENTE
       ============================================================ */

    @FXML
    private void deleteUser(ActionEvent event) {

        if (user == null) return;

        // 1) Dialog di conferma
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Conferma eliminazione");
        confirm.setHeaderText("Vuoi davvero eliminare questo utente?");
        confirm.setContentText("Matricola: " + user.getCode() + "\n" + "Nome: " + user.getFirstName() + " " + user.getLastName() + "\n\n" + "L'operazione non può essere annullata.");

        Optional<ButtonType> result = confirm.showAndWait();

        // Se l'utente NON preme OK, annullo l’operazione
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        // 2) Se confermato, procedo con l'eliminazione
        try {
            userService.removeUser(user);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Utente eliminato");
            alert.setHeaderText("Utente eliminato");
            alert.setContentText("L’utente è stato rimosso dal sistema.");
            alert.showAndWait();

            closeWindow(event);

        } catch (UserHasActiveLoanException ex) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Impossibile eliminare l’utente");
            alert.setHeaderText("Impossibile eliminare l’utente");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();

        } catch (RuntimeException ex) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore di sistema");
            alert.setHeaderText("Errore di sistema");
            alert.setContentText("Impossibile eliminare l’utente.");
            alert.showAndWait();
            ex.printStackTrace();
        }
    }

    /* ============================================================
                          CHIUSURA FINESTRA
       ============================================================ */

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
