/**
 * @file BookController.java
 * @brief Controller responsabile della gestione delle operazioni sui libri.
 */

package swe.group04.libraryms.controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import swe.group04.libraryms.models.Book;
import swe.group04.libraryms.service.BookService;
import swe.group04.libraryms.service.LibraryArchiveService;
import swe.group04.libraryms.service.ServiceLocator;

/**
 * @brief Gestisce le operazioni relative alla visualizzazione, creazione,
 *        modifica e rimozione dei libri dal catalogo.
 *
 * Il controller media tra interfaccia utente e logica applicativa
 * per la gestione dei libri.
 */
public class BookCatalogController {

    @FXML private Button detailsButton;
    @FXML private Button addBookButton;
    @FXML private Button homeButton;
    @FXML private Button searchButton;
    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> bookSortChoiceBox;
    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, Integer> yearColumn;
    @FXML private TableColumn<Book, String> isbnColumn;
    @FXML private TableColumn<Book, Integer> availabilityColumn;

    // ---------------------------------------------------------
    // SERVICE
    // ---------------------------------------------------------

    /** Acceso tramite ServiceLocator */
    private final BookService bookService = ServiceLocator.getBookService();

    /** Lista osservabile popolata dai dati del servizio */
    private ObservableList<Book> observableBooks;

       /* ============================================================================
                                     INIZIALIZZAZIONE
       ============================================================================ */

    /**
     * @brief Inizializza controller e tabella libri.
     *
     * Viene chiamato automaticamente da JavaFX dopo il caricamento del FXML.
     */
    public void initialize() {

        // Titolo
        titleColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getTitle() != null
                                ? cellData.getValue().getTitle()
                                : ""
                )
        );

        // Autore (primo autore o lista unita)
        authorColumn.setCellValueFactory(cellData -> {
            Book b = cellData.getValue();
            String authors;
            if (b.getAuthors() == null || b.getAuthors().isEmpty()) {
                authors = "";
            } else {
                authors = String.join(", ", b.getAuthors());
            }
            return new SimpleStringProperty(authors);
        });

        // Anno
        yearColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getReleaseYear()).asObject()
        );

        // ISBN
        isbnColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getIsbn() != null
                                ? cellData.getValue().getIsbn()
                                : ""
                )
        );

        // Disponibilità (copie disponibili)
        availabilityColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getAvailableCopies()).asObject()
        );

        /* -- Caricamento iniziale dati -- */
        refreshTable();

        /* -- Popolamento ChoiceBox ordinamento -- */
        bookSortChoiceBox.getItems().setAll(
                "Ordina per titolo",
                "Ordina per autore",
                "Ordina per anno di pubblicazione"
        );

        /* Listener su ordinamento */
        bookSortChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            applySort(newV);
        });

        /* Listener ricerca in tempo reale */
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            applySearch(newVal);
        });
    }


    /* ============================================================================
                               CARICAMENTO + AGGIORNAMENTO DATI
       ============================================================================ */

    /**
     * @brief Carica i libri dal servizio e li inserisce nella TableView.
     */
    private void refreshTable() {
        List<Book> books = bookService.getBooksSortedByTitle();
        observableBooks = FXCollections.observableArrayList(books);
        bookTable.setItems(observableBooks);
    }

    /**
     * @brief Applica filtro testuale ai libri.
     */
    private void applySearch(String query) {
        if (query == null || query.isBlank()) {
            refreshTable();
            return;
        }

        List<Book> filtered = bookService.searchBooks(query);
        observableBooks = FXCollections.observableArrayList(filtered);
        bookTable.setItems(observableBooks);
    }

    /**
     * @brief Applica ordinamento selezionato nel ChoiceBox.
     */
    private void applySort(String selected) {
        if (selected == null) return;

        List<Book> list;

        switch (selected) {
            case "Ordina per autore" -> {
                list = observableBooks.stream()
                        .sorted((b1, b2) -> {
                            String a1 = b1.getAuthors().isEmpty() ? "" : b1.getAuthors().get(0);
                            String a2 = b2.getAuthors().isEmpty() ? "" : b2.getAuthors().get(0);
                            return a1.compareToIgnoreCase(a2);
                        })
                        .toList();
            }
            case "Ordina per anno di pubblicazione" -> {
                list = observableBooks.stream()
                        .sorted((b1, b2) -> Integer.compare(b1.getReleaseYear(), b2.getReleaseYear()))
                        .toList();
            }
            default -> { // Ordina per titolo
                list = bookService.getBooksSortedByTitle();
            }
        }

        observableBooks = FXCollections.observableArrayList(list);
        bookTable.setItems(observableBooks);
    }


    /* ============================================================================
                                  NAVIGAZIONE
       ============================================================================ */

    /**
     * @brief Torna alla home.
     */
    @FXML
    public void backHome(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/swe/group04/libraryms/view/main.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/swe/group04/libraryms/css/style.css").toExternalForm());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Library Management System");
            stage.setScene(scene);

        } catch (IOException e) {
            showError("Errore durante il caricamento della Home.");
        }
    }

    /**
     * @brief Apre la finestra "Aggiungi Libro".
     */
    @FXML
    private void addBook(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/swe/group04/libraryms/view/AddBook.fxml"));
            Parent root = loader.load();

            // Recupero il controller della finestra "AddBook"
            AddBookController controller = loader.getController();
            // Registro una callback che ricarica la tabella al termine dell'aggiunta
            controller.setOnBookAddedCallback(this::refreshTable);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/swe/group04/libraryms/css/style.css").toExternalForm());

            Stage addBookStage = new Stage();
            addBookStage.setTitle("Aggiungi libro");
            addBookStage.setScene(scene);

            addBookStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            addBookStage.show();

        } catch (IOException e) {
            showError("Impossibile aprire la finestra di aggiunta libro.");
        }
    }


    /* ============================================================================
                                   UTILITIES
       ============================================================================ */

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void showBookDetails(ActionEvent event) {
    }
}
