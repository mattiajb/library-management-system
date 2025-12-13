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

/**
 * @file AddUserController.java
 * @brief Controller JavaFX responsabile della registrazione di un nuovo utente.
 *
 * Legge i dati inseriti nel form, crea un oggetto User e delega a UserService
 * la validazione di dominio e la persistenza. In caso di successo mostra un messaggio,
 * invoca una callback opzionale e chiude la finestra.
 */
public class AddUserController {

    private final UserService userService = ServiceLocator.getUserService();

    // Callback verso il controller chiamante
    private Runnable onUserAddedCallback;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextField surnameField;
    @FXML private TextField emailField;

    /**
     * @brief Registra una callback eseguita dopo l'aggiunta riuscita di un utente.
     *
     * @param callback Azione da eseguire dopo il salvataggio; può essere null.
     * @post onUserAddedCallback == callback
     */
    public void setOnUserAddedCallback(Runnable callback) {
        this.onUserAddedCallback = callback;
    }
    
    /**
     * @brief Registra un nuovo utente tramite UserService.
     *
     * @param event Evento generato dal click sul pulsante di salvataggio.
     * @pre I campi FXML (codeField, nameField, surnameField, emailField) sono non null.
     * @post Se l'operazione ha successo, l'utente risulta registrato tramite UserService,
     *       viene mostrato un messaggio di conferma, viene invocata (se presente) la callback
     *       e la finestra corrente viene chiusa.
     *
     * Gestione errori:
     * - MandatoryFieldException / InvalidEmailException: mostra un avviso e non completa l'inserimento.
     * - RuntimeException: mostra un errore di sistema e non completa l'inserimento.
     */
    @FXML
    private void saveUser(ActionEvent event) {

        try {
            String code = codeField.getText().trim();
            String firstName = nameField.getText().trim();
            String lastName = surnameField.getText().trim();
            String email = emailField.getText().trim();

            User user = new User(firstName, lastName, email, code);

            userService.addUser(user);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Utente aggiunto");
            alert.setContentText("L’utente è stato registrato correttamente.");
            alert.showAndWait();

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

    /**
     * @brief Annulla l'operazione di inserimento e chiude la finestra corrente.
     *
     * @param event Evento generato dal click sul pulsante di annullamento.
     * @pre event != null e la sorgente dell'evento appartiene a una Scene con uno Stage valido.
     * @post Lo Stage della finestra corrente risulta chiuso.
     */
    @FXML
    private void cancelOperation(ActionEvent event) {
        closeWindow(event);
    }

    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
