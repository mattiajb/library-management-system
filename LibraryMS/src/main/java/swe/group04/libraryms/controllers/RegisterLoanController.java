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
 *  - legge i dati inseriti (utente da tendina, libro da tendina, data restituzione),
 *  - recupera gli oggetti dominio dall'archivio,
 *  - delega a LoanService la registrazione del prestito,
 *  - gestisce errori e messaggi all'utente.
 */
public class RegisterLoanController {

    /* ============================================================
                           FXML COMPONENTS
       ============================================================ */

    /** Utente selezionato (Matricola → Nome Cognome). */
    @FXML private ComboBox<User> userComboBox;

    /** Libro selezionato (ISBN → Titolo). */
    @FXML private ComboBox<Book> bookComboBox;

    /** Data di restituzione prevista. */
    @FXML private DatePicker dueDatePicker;

    @FXML private Button saveLoanButton;
    @FXML private Button cancelButton;

    /* ============================================================
                           SERVICES
       ============================================================ */

    /** Servizio applicativo per la logica di prestito. */
    private final LoanService loanService =
            ServiceLocator.getLoanService();

    /** Accesso all’archivio (libri + utenti). */
    private final LibraryArchiveService archiveService =
            ServiceLocator.getArchiveService();

    /** Callback opzionale per aggiornare la lista prestiti dopo l'inserimento. */
    private Runnable onLoanRegisteredCallback;

    /* ============================================================
                             INITIALIZE
       ============================================================ */

    /**
     * Inizializza le ComboBox caricando utenti e libri dall'archivio
     * e configurando la visualizzazione "user: Matricola → Nome Cognome"
     * e "book: ISBN → Titolo".
     *
     * NB: se l'archivio non è disponibile, le combo restano vuote; l'errore
     * verrà gestito in confirmLoan().
     */
    @FXML
    public void initialize() {

        LibraryArchive archive = archiveService.getLibraryArchive();
        if (archive == null) {
            // Nessun archivio caricato: le combo resteranno vuote.
            // In confirmLoan() verrà mostrato un messaggio di errore chiaro.
            return;
        }

        /* --------- POPOLAMENTO COMBO UTENTI --------- */
        userComboBox.setItems(FXCollections.observableArrayList(archive.getUsers()));

        // Come si presentano gli elementi nel menu a discesa
        userComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.getCode() + " \u2192 "   // matricola →
                            + user.getFirstName() + " " + user.getLastName());
                }
            }
        });

        // Come appare il valore selezionato nel "bottone" della ComboBox
        userComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.getCode() + " \u2192 "
                            + user.getFirstName() + " " + user.getLastName());
                }
            }
        });

        /* --------- POPOLAMENTO COMBO LIBRI --------- */
        bookComboBox.setItems(FXCollections.observableArrayList(archive.getBooks()));

        bookComboBox.setCellFactory(cb -> new ListCell<>() {
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

    /* ============================================================
                         CALLBACK REGISTRAZIONE
       ============================================================ */

    /**
     * Permette al chiamante (LoansListController) di registrare una callback
     * che verrà eseguita dopo la registrazione corretta del prestito.
     */
    public void setOnLoanRegisteredCallback(Runnable callback) {
        this.onLoanRegisteredCallback = callback;
    }

    /* ============================================================
                           CONFERMA PRESTITO
       ============================================================ */

    /**
     * @brief Conferma la registrazione del prestito.
     *
     * Collegato al bottone "Conferma" (onAction="#confirmLoan").
     */
    @FXML
    private void confirmLoan(ActionEvent event) {
        try {
            User selectedUser   = userComboBox.getValue();
            Book selectedBook   = bookComboBox.getValue();
            LocalDate due       = dueDatePicker.getValue();

            // ---- Controlli base sui campi della form ----
            if (selectedUser == null || selectedBook == null || due == null) {
                showError("Compila tutti i campi (utente, libro, data di restituzione).");
                return;
            }

            // (Opzionale) Controllo minimo di coerenza sulla data:
            // se la data è nel passato, probabilmente è un errore di inserimento.
            if (due.isBefore(LocalDate.now())) {
                showError("La data di restituzione prevista non può essere nel passato.");
                return;
            }

            // Recupero archivio corrente per sicurezza/coerenza
            LibraryArchive archive = archiveService.getLibraryArchive();
            if (archive == null) {
                showError("Archivio non disponibile. Impossibile registrare il prestito.");
                return;
            }

            // Verifico che utente e libro siano ancora presenti in archivio
            User user = archive.findUserByCode(selectedUser.getCode());
            if (user == null) {
                showError("L'utente selezionato non è più presente in archivio.");
                return;
            }

            Book book = archive.findBookByIsbn(selectedBook.getIsbn());
            if (book == null) {
                showError("Il libro selezionato non è più presente in archivio.");
                return;
            }

            // ---- Logica di business delegata al servizio ----
            Loan loan = loanService.registerLoan(user, book, due);

            showInfo("Prestito registrato correttamente.\nID prestito: "
                    + loan.getLoanId());

            // Notifica verso la lista prestiti (se il chiamante ha registrato la callback)
            if (onLoanRegisteredCallback != null) {
                onLoanRegisteredCallback.run();
            }

            // Chiudo la finestra
            closeStage(event);

        } catch (MandatoryFieldException |
                 NoAvailableCopiesException |
                 MaxLoansReachedException ex) {

            // Errori controllati di dominio (vincoli violati):
            // messaggio chiaro e specifico all'utente.
            showError(ex.getMessage());

        } catch (RuntimeException ex) {

            // Qualsiasi altro problema inatteso (es. I/O durante il salvataggio)
            showError("Si è verificato un errore durante la registrazione del prestito.\nDettagli: "
                    + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /* ============================================================
                           ANNULLA / CHIUDI
       ============================================================ */

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
