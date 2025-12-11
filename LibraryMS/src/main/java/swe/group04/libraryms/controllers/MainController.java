package swe.group04.libraryms.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML private Button exitBtn;
    @FXML private Button bookCatalogBtn;
    @FXML private Button usersListBtn;
    @FXML private Button loansListBtn;

    // ---------------------------------------------------------
    // NAVIGAZIONE - METODO GENERALE
    // ---------------------------------------------------------
    private void switchScene(String fxmlPath, String title, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/swe/group04/libraryms/css/style.css").toExternalForm()
            );

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);

        } catch (IOException e) {
            showError("Errore durante il caricamento della schermata: " + title);
        }
    }

    // ---------------------------------------------------------
    // APRI SCHERMATE
    // ---------------------------------------------------------

    @FXML
    public void openBookCatalog(ActionEvent event) {
        switchScene(
                "/swe/group04/libraryms/view/BookCatalog.fxml",
                "Catalogo Libri",
                event
        );
    }

    @FXML
    public void openUsersList(ActionEvent event) {
        switchScene(
                "/swe/group04/libraryms/view/UsersList.fxml",
                "Lista Utenti",
                event
        );
    }

    @FXML
    public void openLoansList(ActionEvent event) {
        switchScene(
                "/swe/group04/libraryms/view/LoansList.fxml",
                "Gestione Prestiti",
                event
        );
    }

    // ---------------------------------------------------------
    // USCITA
    // ---------------------------------------------------------

    @FXML
    private void exit(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    // ---------------------------------------------------------
    // GESTIONE ERRORI
    // ---------------------------------------------------------

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText("Operazione non riuscita");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
