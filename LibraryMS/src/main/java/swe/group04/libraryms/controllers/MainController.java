/**
 * @file MainController.java
 * @brief Controller principale per l'inizializzazione del sistema.
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
import javafx.stage.Stage;

/**
 * @brief Gestisce le operazioni iniziali necessarie all'avvio
 *        del sistema della biblioteca.
 *
 * Questo controller coordina il caricamento dell'archivio e
 * la preparazione dei componenti software necessari all'esecuzione.
 */
public class MainController {

    @FXML
    private Button exitBtn;
    @FXML
    private Button bookCatalogBtn;
    @FXML
    private Button usersListBtn;
    @FXML
    private Button loansListBtn;
    
    @FXML
    public void openBookCatalog(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/swe/group04/libraryms/view/BookCatalog.fxml"));

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/swe/group04/libraryms/css/style.css").toExternalForm());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Catalogo Libri");
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
            // TODO: eventualmente Alert all'utente
        }
    }

    @FXML
    public void openUsersList(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/swe/group04/libraryms/view/UsersList.fxml"));

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/swe/group04/libraryms/css/style.css") .toExternalForm());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Lista Utenti");
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
            // TODO: eventualmente Alert all'utente
        }
    }

    @FXML
    private void exit(ActionEvent event) {
    }

    @FXML
    private void openLoansList(ActionEvent event) {
    }
    
}
