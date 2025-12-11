/**
 * @file AddUserController.java
 * @brief Controller responsabile dell'aggiunta di un nuovo utente nel sistema.
 */

package swe.group04.libraryms.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import swe.group04.libraryms.models.User;

/**
 * @brief Gestisce l'operazione di registrazione di un nuovo utente
 *        all'interno del sistema della biblioteca.
 *
 * L'operazione prevede la validazione dell'utente fornito,
 * l'inserimento nell'archivio utenti e il salvataggio persistente dei dati.
 */
public class AddUserController {

    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private TextField matricolaField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField surnameField;
    @FXML
    private TextField emailField;  

    @FXML
    private void cancelOperation(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
