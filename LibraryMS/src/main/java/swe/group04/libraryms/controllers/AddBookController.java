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
 * @brief Controller responsabile dell'aggiunta di un nuovo libro nel catalogo.
 *
 * Gestisce:
 *  - lettura e validazione dei dati inseriti nel form
 *  - creazione dell'oggetto Book
 *  - delega al BookService per l'inserimento e la persistenza
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
     * @brief Permette al chiamante (BookCatalogController) di registrare
     *        una callback da eseguire quando un libro viene aggiunto
     *        con successo.
     */
    public void setOnBookAddedCallback(Runnable callback) {
        this.onBookAddedCallback = callback;
    }

    /**
     * @brief Annulla l'operazione e chiude la finestra corrente.
     */
    @FXML
    private void cancelOperation(ActionEvent event) {
        closeStage(event);
    }

    /**
     * @brief Legge i dati dal form, valida e registra un nuovo libro.
     *
     * Gestione errori:
     *  - errori di parsing (anno/copied totali non numerici)
     *  - campi obbligatori mancanti (MandatoryFieldException)
     *  - ISBN non valido o duplicato (InvalidIsbnException)
     *  - errori di persistenza (RuntimeException da BookService)
     *
     * In caso di successo:
     *  - viene mostrato un messaggio di conferma
     *  - viene eventualmente invocata la callback di refresh
     *  - la finestra viene chiusa
     */
    @FXML
    private void saveBook(ActionEvent event) {
        try {
            // ------------------------------
            // Lettura e validazione base
            // ------------------------------
            String title = safeTrim(titleField.getText());
            String authorsText = safeTrim(authorField.getText());
            String yearText = safeTrim(yearField.getText());
            String isbn = safeTrim(isbnField.getText());
            String totalCopiesText = safeTrim(totalCopiesField.getText());

            if (title.isEmpty() || authorsText.isEmpty() || yearText.isEmpty()
                    || isbn.isEmpty() || totalCopiesText.isEmpty()) {
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
            List<String> authors = Arrays.stream(authorsText.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            if (authors.isEmpty()) {
                showError("Inserisci almeno un autore (separa con virgole in caso di più autori).");
                return;
            }

            // ------------------------------
            // Creazione del modello Book
            // ------------------------------
            Book book = new Book(title, authors, year, isbn, totalCopies);

            // ------------------------------
            // Delego a BookService la logica di business
            // (validazioni avanzate + persistenza)
            // ------------------------------
            bookService.addBook(book);

            // ------------------------------
            // Successo: notifico eventuale callback e chiudo
            // ------------------------------
            showInfo("Libro aggiunto correttamente al catalogo.");

            if (onBookAddedCallback != null) {
                onBookAddedCallback.run();
            }

            closeStage(event);

        } catch (MandatoryFieldException e) {
            // Violazione campi obbligatori / invarianti del modello
            showError(e.getMessage());
        } catch (InvalidIsbnException e) {
            // ISBN non valido o duplicato
            showError(e.getMessage());
        } catch (RuntimeException e) {
            // Errori di persistenza o altri problemi non previsti
            showError("Si è verificato un errore durante il salvataggio del libro.\nDettagli: "
                    + e.getMessage());
        }
    }

    /* ------------------------------------------------------------------
                          METODI DI SUPPORTO
       ------------------------------------------------------------------ */

    private void closeStage(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource())
                .getScene().getWindow();
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
