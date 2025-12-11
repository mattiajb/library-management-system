package swe.group04.libraryms.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import swe.group04.libraryms.models.Book;
import swe.group04.libraryms.service.BookService;
import swe.group04.libraryms.service.LoanService;
import swe.group04.libraryms.service.ServiceLocator;

import swe.group04.libraryms.exceptions.*;

import java.util.Arrays;
import java.util.List;

public class BookDetailsController {

    // --- SERVICE ---
    private final BookService bookService = ServiceLocator.getBookService();
    private final LoanService loanService = ServiceLocator.getLoanService();

    // --- MODEL ---
    private Book book;

    @FXML private Button saveButton;
    @FXML private Button deleteButton;
    @FXML private Button cancelButton;

    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField yearField;
    @FXML private TextField isbnField;
    @FXML private TextField totalCopiesField;
    @FXML private TextField availableCopiesField;


    /* ============================================================
                      INIEZIONE DEL MODELLO
       ============================================================ */

    /** Metodo chiamato dal controller chiamante */
    public void setBook(Book book) {
        this.book = book;
        loadBookData();
    }


    /* ============================================================
                         CARICAMENTO DATI
       ============================================================ */

    private void loadBookData() {
        if (book == null) return;

        titleField.setText(book.getTitle());
        authorField.setText(String.join(", ", book.getAuthors()));
        yearField.setText(String.valueOf(book.getReleaseYear()));
        isbnField.setText(book.getIsbn());
        totalCopiesField.setText(String.valueOf(book.getTotalCopies()));
        availableCopiesField.setText(String.valueOf(book.getAvailableCopies()));

        // Campi non modificabili
        isbnField.setDisable(true);
        availableCopiesField.setDisable(true);
    }


    /* ============================================================
                      SALVATAGGIO MODIFICHE
       ============================================================ */

    @FXML
    private void saveChanges(ActionEvent event) {

        if (book == null) return;

        try {
            // --- Raccolta dati aggiornati ---
            String newTitle = titleField.getText().trim();
            String newAuthorRaw = authorField.getText().trim();
            List<String> newAuthors = Arrays.stream(newAuthorRaw.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            int newYear = Integer.parseInt(yearField.getText().trim());

            int newTotalCopies = Integer.parseInt(totalCopiesField.getText().trim());

            int oldTotal = book.getTotalCopies();
            int oldAvailable = book.getAvailableCopies();

            /* Se il numero totale di copie diminuisce sotto availableCopies → ERRORE */
            if (newTotalCopies < book.getAvailableCopies()) {
                throw new MandatoryFieldException(
                        "Le copie totali non possono essere inferiori alle copie disponibili."
                );
            }

            book.setTotalCopies(newTotalCopies);

            // Se l'utente aumenta le copie totali → incrementiamo le copie disponibili
            if (newTotalCopies > oldTotal) {
                int delta = newTotalCopies - oldTotal;
                book.setAvailableCopies(oldAvailable + delta);
            }

            // --- Aggiornamento del modello ---
            book.setTitle(newTitle);
            book.setAuthors(newAuthors);
            book.setReleaseYear(newYear);

            // Persistenza tramite servizio
            bookService.updateBook(book);

            // Conferma all’utente
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Modifiche salvate");
            alert.setContentText("Le informazioni del libro sono state aggiornate correttamente.");
            alert.showAndWait();

            closeWindow(event);

        } catch (NumberFormatException e) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Errore di formato");
            alert.setContentText("Anno e copie totali devono essere numeri interi validi.");
            alert.showAndWait();

        } catch (MandatoryFieldException | InvalidIsbnException ex) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Dati non validi");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();

        } catch (RuntimeException ex) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Errore di sistema");
            alert.setContentText("Impossibile salvare le modifiche.");
            alert.showAndWait();
            ex.printStackTrace();
        }
    }


    /* ============================================================
                        ELIMINAZIONE LIBRO
       ============================================================ */

    @FXML
    private void deleteBook(ActionEvent event) {

        if (book == null) return;

        try {
            bookService.removeBook(book);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Libro eliminato");
            alert.setContentText("Il libro è stato rimosso dal catalogo.");
            alert.showAndWait();

            closeWindow(event);

        } catch (UserHasActiveLoanException ex) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Impossibile eliminare il libro");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();

        } catch (RuntimeException ex) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Errore imprevisto");
            alert.setContentText("Impossibile eliminare il libro.");
            alert.showAndWait();
            ex.printStackTrace();
        }
    }


    /* ============================================================
                        CHIUSURA FINESTRA
       ============================================================ */

    @FXML
    private void cancelOperation(ActionEvent event) {
        closeWindow(event);
    }

    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
