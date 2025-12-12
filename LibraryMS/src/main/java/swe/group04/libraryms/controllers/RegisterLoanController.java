package swe.group04.libraryms.controllers;

import java.time.LocalDate;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import swe.group04.libraryms.exceptions.MandatoryFieldException;
import swe.group04.libraryms.exceptions.MaxLoansReachedException;
import swe.group04.libraryms.exceptions.NoAvailableCopiesException;
import swe.group04.libraryms.models.Book;
import swe.group04.libraryms.models.LibraryArchive;
import swe.group04.libraryms.models.Loan;
import swe.group04.libraryms.models.User;
import swe.group04.libraryms.service.LibraryArchiveService;
import swe.group04.libraryms.service.LoanService;
import swe.group04.libraryms.service.ServiceLocator;

/**
 * @brief Gestisce l'operazione di registrazione di un nuovo prestito
 *        all'interno del sistema della biblioteca.
 *
 * Il controller:
 *  - legge i dati inseriti (matricola utente, libro da tendina, data restituzione),
 *  - recupera utente dall'archivio,
 *  - delega a LoanService la registrazione del prestito,
 *  - gestisce errori e messaggi all'utente.
 */
public class RegisterLoanController {

    @FXML private TextField userCodeField;
    @FXML private ComboBox<Book> bookComboBox;
    @FXML private DatePicker dueDatePicker;
    @FXML private Button saveLoanButton;
    @FXML private Button cancelButton;

    /** Servizi applicativi */
    private final LoanService loanService =
            ServiceLocator.getLoanService();
    private final LibraryArchiveService archiveService =
            ServiceLocator.getArchiveService();

    /** Callback opzionale per aggiornare la lista prestiti dopo l'inserimento. */
    private Runnable onLoanRegisteredCallback;

    /* ============================ INITIALIZE ============================ */

    @FXML
    public void initialize() {
        // Popolo la tendina dei libri usando l'archivio
        LibraryArchive archive = archiveService.getLibraryArchive();
        if (archive == null) {
            // Lascio la combo vuota: l'errore verrà gestito in confirmLoan()
            return;
        }

        bookComboBox.setItems(FXCollections.observableArrayList(archive.getBooks()));

        // Visualizzazione "ISBN → Titolo" nella lista
        bookComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Book book, boolean empty) {
                super.updateItem(book, empty);
                if (empty || book == null) {
                    setText(null);
                } else {
                    setText(book.getIsbn() + " \u2192 " + book.getTitle()); // → simbolo freccia
                }
            }
        });

        // Stessa visualizzazione anche sul bottone "chiuso" della ComboBox
        bookComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Book book, boolean empty) {
                super.updateItem(book, empty);
                if (empty || book == null) {
                    setText(null);
                } else {
                    setText(book.getIsbn() + " \u2192 " + book.getTitle());
                }
            }
        });
    }

    /**
     * Permette al chiamante (LoansListController) di registrare una callback
     * che verrà eseguita dopo la registrazione corretta del prestito.
     */
    public void setOnLoanRegisteredCallback(Runnable callback) {
        this.onLoanRegisteredCallback = callback;
    }

    /**
     * @brief Conferma la registrazione del prestito.
     *
     * Collegato al bottone "Conferma" (onAction="#confirmLoan").
     */
    @FXML
    private void confirmLoan(ActionEvent event) {
        try {
            String userCode = safeTrim(userCodeField.getText());
            LocalDate due   = dueDatePicker.getValue();
            Book selectedBook = bookComboBox.getValue();

            // Controlli di base sui campi
            if (userCode.isEmpty() || due == null || selectedBook == null) {
                showError("Compila tutti i campi (matricola utente, libro, data di restituzione).");
                return;
            }

            LibraryArchive archive = archiveService.getLibraryArchive();
            if (archive == null) {
                showError("Archivio non disponibile. Impossibile registrare il prestito.");
                return;
            }

            // Recupero utente dall'archivio
            User user = archive.findUserByCode(userCode);
            if (user == null) {
                showError("Utente non trovato. Controlla la matricola inserita.");
                return;
            }

            // Il libro è già stato scelto dalla ComboBox → nessuna ricerca per ISBN
            Book book = selectedBook;

            // Delego la logica di business a LoanService
            Loan loan = loanService.registerLoan(user, book, due);

            showInfo("Prestito registrato correttamente.\nID prestito: "
                    + loan.getLoanId());

            // Notifica verso la lista prestiti
            if (onLoanRegisteredCallback != null) {
                onLoanRegisteredCallback.run();
            }

            // Chiudo la finestra
            closeStage(event);

        } catch (MandatoryFieldException |
                 NoAvailableCopiesException |
                 MaxLoansReachedException ex) {

            // Errori di business (vincoli violati) → messaggio chiaro all'utente
            showError(ex.getMessage());

        } catch (RuntimeException ex) {

            // Qualsiasi altro problema inatteso (es. I/O durante il salvataggio)
            showError("Si è verificato un errore durante la registrazione del prestito.\nDettagli: "
                    + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * @brief Annulla l'operazione e chiude la finestra.
     */
    @FXML
    private void cancelOperation(ActionEvent event) {
        closeStage(event);
    }

    /* ====================== METODI DI SUPPORTO ====================== */

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
