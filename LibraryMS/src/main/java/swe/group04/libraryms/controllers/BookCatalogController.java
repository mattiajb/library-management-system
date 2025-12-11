/**
 * @file BookController.java
 * @brief Controller responsabile della gestione delle operazioni sui libri.
 */

package swe.group04.libraryms.controllers;

import java.io.IOException;
import java.time.LocalDate;
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
import swe.group04.libraryms.models.Book;

/**
 * @brief Gestisce le operazioni relative alla visualizzazione, creazione,
 *        modifica e rimozione dei libri dal catalogo.
 *
 * Il controller media tra interfaccia utente e logica applicativa
 * per la gestione dei libri.
 */
public class BookCatalogController {

    @FXML
    private Button detailsButton;
    @FXML
    private Button addBookButton;
    @FXML
    private Button homeButton;
    @FXML
    private TextField searchField;
    @FXML
    private ChoiceBox<?> bookSortChoiceBox;
    @FXML
    private TableView<Book> bookTable;
    @FXML
    private TableColumn<Book, String> titleColumn;
    @FXML
    private TableColumn<Book, String> authorColumn;
    @FXML
    private TableColumn<Book, Integer> yearColumn;
    @FXML
    private TableColumn<?, ?> isbnColumn;
    @FXML
    private TableColumn<?, ?> availabilityColumn;
    


    @FXML
    public void backHome(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/swe/group04/libraryms/view/main.fxml"));

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/swe/group04/libraryms/css/style.css")
                              .toExternalForm());

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();
            stage.setTitle("Library Management System");
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
            // TODO: eventualmente Alert all'utente
        }
    }

    @FXML
    private void addBook(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/swe/group04/libraryms/view/AddBook.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/swe/group04/libraryms/css/style.css").toExternalForm());

            Stage addBookStage = new Stage();
            addBookStage.setTitle("Aggiungi libro");
            addBookStage.setScene(scene);

            Stage owner = (Stage) ((Node) event.getSource()).getScene().getWindow();
            addBookStage.initOwner(owner);

            addBookStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // TODO: eventualmente Alert
        }
    }
}
