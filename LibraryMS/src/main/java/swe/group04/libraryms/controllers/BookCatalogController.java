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
import swe.group04.libraryms.service.ServiceLocator;

/**
 * @file BookCatalogController.java
 * @brief Controller JavaFX responsabile della gestione del catalogo libri.
 *
 * Gestisce la visualizzazione del catalogo in una TableView, la ricerca e
 * l'ordinamento dei libri e la navigazione verso schermate di aggiunta/dettagli.
 * Le operazioni di business e accesso ai dati sono delegate a BookService.
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

    /** Servizio per operazioni su libri (logica applicativa e accesso ai dati). */
    private final BookService bookService = ServiceLocator.getBookService();

    /** Lista osservabile associata alla TableView; contiene i libri attualmente mostrati. */
    private ObservableList<Book> observableBooks;

    /** Criterio di ordinamento corrente selezionato dall'utente nel ChoiceBox. */
    private String currentSort = "Ordina per titolo";

    /**
     * @brief Inizializza la TableView, i binding delle colonne e i listener di ricerca/ordinamento.
     *
     * @pre titleColumn, authorColumn, yearColumn, isbnColumn, availabilityColumn, bookTable,
     *      bookSortChoiceBox, searchField sono non null (iniezione FXML completata).
     * @post La TableView è configurata e popolata con i libri caricati dal BookService.
     * @post Il ChoiceBox contiene i criteri di ordinamento e risulta selezionato currentSort.
     */
    public void initialize() {

        titleColumn.setCellValueFactory(cellData ->new SimpleStringProperty(cellData.getValue().getTitle() != null ? cellData.getValue().getTitle(): ""));

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

        yearColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getReleaseYear()).asObject());

        isbnColumn.setCellValueFactory(cellData ->new SimpleStringProperty(cellData.getValue().getIsbn() != null ? cellData.getValue().getIsbn(): ""));

        availabilityColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getAvailableCopies()).asObject()
        );

        refreshTable();

        bookSortChoiceBox.getItems().setAll("Ordina per titolo", "Ordina per autore", "Ordina per anno di pubblicazione");

        bookSortChoiceBox.getSelectionModel().select(currentSort);

        /* Listener su ordinamento */
        bookSortChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {applySort(newV);});

        /* Listener ricerca in tempo reale */
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {applySearch(newVal);});
    }

    /**
     * @brief Ricarica i libri dal servizio applicando l'ordinamento corrente e aggiorna la tabella.
     */
    private void refreshTable() {
        List<Book> books;

        // Applica lo stesso criterio di ordinamento usato dall’utente
        switch (currentSort) {
            case "Ordina per autore" -> books = bookService.getBooksSortedByAuthor();

            case "Ordina per anno di pubblicazione" -> books = bookService.getBooksSortedByYear();

            default -> books = bookService.getBooksSortedByTitle();
        }

        if (observableBooks == null) {
            observableBooks = FXCollections.observableArrayList(books);
            bookTable.setItems(observableBooks);
        } else {
            observableBooks.setAll(books);
        }

        bookTable.refresh();
    }

    /**
     * @brief Filtra i libri in base a una query testuale e aggiorna la tabella.
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
     * @brief Imposta il criterio di ordinamento selezionato e ricarica la tabella.
     */
    private void applySort(String selected) {
        if (selected == null) return;

        // Salva la modalità di ordinamento scelta dall’utente
        currentSort = selected;

        // Ricarica la tabella applicando l’ordinamento richiesto
        refreshTable();
    }

    /**
     * @brief Torna alla schermata principale caricando main.fxml nello Stage corrente.
     *
     * @param event Evento generato dal click sul pulsante Home/Back.
     * @pre event != null e la sorgente dell'evento appartiene a una Scene con uno Stage valido.
     * @post Se il caricamento va a buon fine, lo Stage corrente mostra la schermata main.fxml.
     * @post In caso di errore di caricamento, viene mostrato un Alert di errore.
     */
    @FXML
    public void backHome(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/swe/group04/libraryms/view/main.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/swe/group04/libraryms/css/style.css").toExternalForm());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Library Management System");
            stage.setScene(scene);

        } catch (IOException e) {
            showError("Errore durante il caricamento della Home.");
        }
    }

    /**
     * @brief Apre la finestra di inserimento di un nuovo libro.
     *
     * @param event Evento generato dal click sul pulsante "Aggiungi libro".
     * @pre event != null e la sorgente dell'evento appartiene a una Scene con uno Stage valido.
     * @post Se il caricamento va a buon fine, viene mostrato uno Stage con AddBook.fxml.
     * @post La finestra di inserimento registra una callback per aggiornare la tabella dopo l'aggiunta.
     * @post In caso di errore, viene mostrato un Alert di errore.
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
    
    /**
     * @brief Apre la finestra dei dettagli del libro selezionato nella tabella.
     *
     * @param event Evento generato dal click sul pulsante "Dettagli".
     * @pre bookTable != null
     * @post Se nessun libro è selezionato, viene mostrato un Alert di avviso e non viene aperta alcuna finestra.
     * @post Se un libro è selezionato, viene mostrato uno Stage con BookDetails.fxml e il controller riceve il Book selezionato.
     * @post Alla chiusura della finestra dettagli, la tabella viene aggiornata.
     */
    @FXML
    private void openBookDetails(ActionEvent event) {
        Book selected = bookTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Nessun libro selezionato");
            alert.setContentText("Seleziona un libro dalla tabella per visualizzarne i dettagli.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/swe/group04/libraryms/view/BookDetails.fxml")
            );

            Parent root = loader.load();

            BookDetailsController controller = loader.getController();
            controller.setBook(selected);

            Stage stage = new Stage();
            stage.setTitle("Dettagli libro");
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> refreshTable());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Errore di caricamento");
            alert.setContentText("Impossibile aprire la schermata dei dettagli.");
            alert.showAndWait();
        }
    }

    /**
     * @brief Mostra un Alert di errore con il messaggio fornito.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setContentText(message);
        alert.showAndWait();
    }

}
