/**
 * @file BookDetailsController.java
 * @brief Controller responsabile della visualizzazione dei dettagli di un libro.
 */

package swe.group04.libraryms.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import swe.group04.libraryms.models.Book;

/**
 * @brief Gestisce l'operazione di visualizzazione dettagliata delle informazioni
 *        relative a un libro presente nel catalogo.
 *
 * L'operazione prevede il recupero dei dati del libro e la loro
 * presentazione all'utente tramite l'interfaccia grafica.
 */
public class BookDetailsController {

    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;
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
    private TextField availableCopiesField;

    @FXML
    private void cancelOperation(ActionEvent event) {
    }
}
