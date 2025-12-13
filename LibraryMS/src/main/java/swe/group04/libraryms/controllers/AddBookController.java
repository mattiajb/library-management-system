package swe.group04.libraryms.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import swe.group04.libraryms.exceptions.InvalidIsbnException;
import swe.group04.libraryms.exceptions.MandatoryFieldException;
import swe.group04.libraryms.models.Book;
import swe.group04.libraryms.service.BookService;
import swe.group04.libraryms.service.ServiceLocator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @file AddBookController.java
 * @brief Controller JavaFX per l'aggiunta di un nuovo libro nel catalogo.
 *
 * Legge i campi dal form, fa validazioni sintattiche (campi vuoti, parsing numerico,
 * parsing lista autori) e delega a BookService la validazione di dominio e la persistenza.
 *
 * @see BookService
 * @see ServiceLocator
 */
public class AddBookController {

    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField yearField;
    @FXML private TextField isbnField;
    @FXML private TextField totalCopiesField;

    /** Servizio libri ottenuto tramite ServiceLocator */
    private final BookService bookService = ServiceLocator.getBookService();

    /**
     * Callback opzionale richiamata dopo un inserimento andato a buon fine
     * (es. per permettere al BookCatalogController di fare refresh).
     */
    private Runnable onBookAddedCallback;

    /**
     * @brief Registra una callback eseguita dopo un'aggiunta andata a buon fine.
     *
     * @param callback Azione da eseguire dopo il salvataggio; può essere null.
     * @post onBookAddedCallback == callback
     */
    public void setOnBookAddedCallback(Runnable callback) {
        this.onBookAddedCallback = callback;
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
        closeStage(event);
    }

    /**
     * @brief Valida i dati del form e registra un nuovo libro tramite BookService.
     *
     * @param event Evento generato dal click sul pulsante di salvataggio.
     * @pre I campi FXML (titleField, authorField, yearField, isbnField, totalCopiesField) sono non null.
     * @post Se l'operazione ha successo, il libro risulta registrato tramite BookService,
     *       viene mostrato un messaggio di conferma, viene invocata (se presente) la callback
     *       e la finestra corrente viene chiusa.
     *
     * Comportamento in caso di errore:
     * - mostra un Alert di errore e l'operazione non completa il salvataggio.
     */
    @FXML
    private void saveBook(ActionEvent event) {
        try {
            String title = safeTrim(titleField.getText());
            String authorsText = safeTrim(authorField.getText());
            String yearText = safeTrim(yearField.getText());
            String isbn = safeTrim(isbnField.getText());
            String totalCopiesText = safeTrim(totalCopiesField.getText());

            if (title.isEmpty() || authorsText.isEmpty() || yearText.isEmpty() || isbn.isEmpty() || totalCopiesText.isEmpty()) {
                showError("Tutti i campi sono obbligatori.");
                return;
            }

            int year;
            int totalCopies;
            try {
                year = Integer.parseInt(yearText);
            } catch (NumberFormatException e) {
                showError("L'anno di pubblicazione deve essere un numero intero.");
                return;
            }

            try {
                totalCopies = Integer.parseInt(totalCopiesText);
            } catch (NumberFormatException e) {
                showError("Il numero di copie totali deve essere un numero intero.");
                return;
            }

            // Parsing autori (separati da virgola)
            List<String> authors = Arrays.stream(authorsText.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());

            if (authors.isEmpty()) {
                showError("Inserisci almeno un autore (separa con virgole in caso di più autori).");
                return;
            }

            Book book = new Book(title, authors, year, isbn, totalCopies);

            bookService.addBook(book);

            showInfo("Libro aggiunto correttamente al catalogo.");

            if (onBookAddedCallback != null) {
                onBookAddedCallback.run();
            }

            closeStage(event);

        } catch (MandatoryFieldException e) {
            showError(e.getMessage());
        } catch (InvalidIsbnException e) {
            showError(e.getMessage());
        } catch (RuntimeException e) {
            showError("Si è verificato un errore durante il salvataggio del libro.\nDettagli: "+ e.getMessage());
        }
    }
    
    private void closeStage(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText("Operazione non riuscita");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informazione");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
