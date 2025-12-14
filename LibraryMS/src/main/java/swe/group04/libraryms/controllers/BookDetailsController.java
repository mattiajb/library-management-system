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
import java.util.Optional;
import javafx.scene.control.ButtonType;

/**
 * @file BookDetailsController.java
 * @brief Controller responsabile della visualizzazione e modifica dei dettagli di un libro.
 *
 * Questo controller gestisce la schermata dei dettagli di un singolo libro:
 *  - carica i dati del libro selezionato nei campi della form,
 *  - consente la modifica di titolo, autori, anno e copie totali,
 *  - aggiorna coerentemente le copie disponibili quando le copie totali aumentano,
 *  - permette l'eliminazione del libro dal catalogo se non ci sono vincoli bloccanti,
 *  - gestisce la chiusura della finestra di dettaglio.
 *
 * Interagisce con BookService per l'aggiornamento e la rimozione del libro,
 * e con LoanService per vincoli legati ai prestiti attivi
 * (tramite le eccezioni di dominio sollevate dal servizio).
 */

public class BookDetailsController {

    //  --- SERVICE ---
    private final BookService bookService = ServiceLocator.getBookService();
    private final LoanService loanService = ServiceLocator.getLoanService();

    //  --- MODEL ---
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

    /**
     * @brief Inietta il libro da visualizzare/modificare nella schermata.
     *
     * Viene chiamato dal controller chiamante (BookCatalogController) subito dopo
     * il caricamento del file FXML. Il metodo salva il riferimento al modello
     * e popola i campi dell'interfaccia utente con i dati correnti del libro.
     *
     * @param book Libro selezionato nel catalogo (non null).
     *
     * @pre  book != null
     * @post this.book == book
     * @post I campi di testo riflettono lo stato corrente del libro passato.
     */
    public void setBook(Book book) {
        this.book = book;
        loadBookData();
    }

    /**
     * @brief Carica i dati del libro nei campi della form.
     *
     * Se il modello book non è stato ancora impostato, il metodo termina
     * senza effettuare alcuna operazione.
     *
     * Campi non modificabili:
     *  - ISBN (identificativo univoco del libro),
     *  - copie disponibili (derivano da logica di prestito/restizione).
     *
     * @post I campi FXML sono popolati coerentemente con i valori del libro.
     */
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

    /**
     * @brief Salva le modifiche apportate ai dati del libro.
     *
     * Il metodo:
     *  - legge i nuovi valori dai campi di input (titolo, autori, anno, copie totali),
     *  - valida che anno e copie totali siano numeri interi,
     *  - verifica che il nuovo numero di copie totali non sia inferiore
     *    alle copie attualmente disponibili (vincolo di coerenza),
     *  - se le copie totali aumentano, incrementa di conseguenza le copie disponibili,
     *  - aggiorna il modello Book e delega a BookService la persistenza delle modifiche,
     *  - mostra un messaggio di conferma e chiude la finestra in caso di successo.
     *
     * Gestione errori:
     *  - NumberFormatException → alert di errore di formato (anno / copie non validi),
     *  - MandatoryFieldException / InvalidIsbnException → alert di dati non validi,
     *  - RuntimeException generiche → alert di "errore di sistema" e stampa dello stack trace.
     *
     * @param event Evento di azione generato dal pulsante "Salva modifiche".
     */
    @FXML
    private void saveChanges(ActionEvent event) {

        if (book == null) return;

        try {
            // --- Raccolta dati aggiornati ---
            String newTitle = titleField.getText().trim();
            String newAuthorRaw = authorField.getText().trim();
            List<String> newAuthors = Arrays.stream(newAuthorRaw.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();

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

            //  --- Aggiornamento del modello ---
            book.setTitle(newTitle);
            book.setAuthors(newAuthors);
            book.setReleaseYear(newYear);

            //  Persistenza tramite servizio
            bookService.updateBook(book);

            //  Conferma all’utente
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


    /**
     * @brief Elimina il libro dal catalogo, previa conferma dell'utente.
     *
     * Il metodo:
     *  - mostra una finestra di conferma (Alert di tipo CONFIRMATION) con
     *    titolo e ISBN del libro,
     *  - se l'utente conferma, delega a BookService la rimozione del libro,
     *  - in caso di successo, mostra un messaggio informativo e chiude la finestra.
     *
     * Gestione errori:
     *  - UserHasActiveLoanException → l'utente viene avvisato che il libro
     *    non può essere eliminato perché associato a prestiti attivi,
     *  - RuntimeException generiche → alert di errore imprevisto e stampa dello stack trace.
     *
     * Se l'utente annulla la conferma o chiude la dialog di conferma,
     * l'operazione viene semplicemente interrotta senza effetti sul modello.
     *
     * @param event Evento di azione generato dal pulsante "Elimina libro".
     */
    @FXML
    private void deleteBook(ActionEvent event) {

        if (book == null) return;

        //  1) Dialog di conferma
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Conferma eliminazione");
        confirm.setHeaderText("Vuoi davvero eliminare questo libro?");
        confirm.setContentText("Titolo: " + book.getTitle() + "\n" + "ISBN: " + book.getIsbn() + "\n\n" + "L'operazione non può essere annullata.");

        Optional<ButtonType> result = confirm.showAndWait();

        //  Se l'utente NON preme OK, annullo l'operazione
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        //  2) Se l'utente ha confermato, procedo con l'eliminazione
        try {
            bookService.removeBook(book);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Libro eliminato");
            alert.setHeaderText("Libro eliminato");
            alert.setContentText("Il libro è stato rimosso dal catalogo.");
            alert.showAndWait();

            closeWindow(event);

        } catch (UserHasActiveLoanException ex) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Impossibile eliminare il libro");
            alert.setHeaderText("Impossibile eliminare il libro");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();

        } catch (RuntimeException ex) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore imprevisto");
            alert.setHeaderText("Errore imprevisto");
            alert.setContentText("Impossibile eliminare il libro.");
            alert.showAndWait();
            ex.printStackTrace();
        }
    }


    /**
     * @brief Annulla l'operazione corrente e chiude la finestra dei dettagli.
     *
     * Non effettua alcun salvataggio: viene usato come "Chiudi" o "Annulla".
     *
     * @param event Evento di azione generato dal pulsante "Chiudi".
     */
    @FXML
    private void cancelOperation(ActionEvent event) {
        closeWindow(event);
    }

    /**
     * @brief Chiude lo Stage associato al nodo che ha generato l'evento.
     *
     * Metodo di utilità riutilizzato sia da saveChanges (dopo il salvataggio)
     * sia da cancelOperation e deleteBook (dopo l'eliminazione).
     *
     * @param event Evento proveniente da un controllo appartenente alla finestra corrente.
     *
     * @pre  event.getSource() è un Node appartenente a una Scene non null.
     * @post La finestra (Stage) associata è chiusa.
     */
    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}