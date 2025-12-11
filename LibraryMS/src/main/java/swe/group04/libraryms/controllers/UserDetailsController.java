/**
 * @file UserDetailsController.java
 * @brief Controller responsabile della visualizzazione dei dettagli di un utente.
 */
package swe.group04.libraryms.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * @brief Gestisce la visualizzazione delle informazioni dettagliate
 *        relative a un utente registrato nel sistema della biblioteca.
 *
 * Il controller fornisce le funzionalità necessarie per mostrare
 * all'utente finale i dati identificativi e amministrativi associati a un utente.
 */
public class UserDetailsController {

    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;
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
    private TextField activeLoansField;

    @FXML
    private void cancelOperation(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();   
    }
}
