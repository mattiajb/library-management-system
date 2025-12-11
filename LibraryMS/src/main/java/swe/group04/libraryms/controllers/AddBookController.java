/**
 * @file AddBookController.java
 * @brief Controller responsabile dell'aggiunta di un nuovo libro nel catalogo.
 */

package swe.group04.libraryms.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import swe.group04.libraryms.models.Book;

/**
 * @brief Gestisce l'operazione di registrazione di un nuovo libro
 *        all'interno del catalogo della biblioteca.
 *
 * L'operazione prevede la validazione del libro fornito,
 * l'inserimento nel catalogo e il salvataggio dell'archivio aggiornato.
 */
public class AddBookController {

    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private TextField titleField;
    @FXML
    private TextField authorField;
    @FXML
    private TextField yearField;
    @FXML
    private TextField isbnField;
    @FXML
    private TextField totalCopiesField;
    
    @FXML
    private void cancelOperation(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}

