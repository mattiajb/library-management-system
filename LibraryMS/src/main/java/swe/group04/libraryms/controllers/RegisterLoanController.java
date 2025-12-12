package swe.group04.libraryms.controllers;

import java.time.LocalDate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
 * Gestisce l'operazione di registrazione di un nuovo prestito.
 *
 * - Ricerca dinamica stile "Google" su utenti e libri tramite TextField
 * - ComboBox mostrate/filtrate in base al testo inserito
 */
public class RegisterLoanController {

    /* ======================= FXML COMPONENTS ======================= */

    @FXML private TextField userSearchField;
    @FXML private ComboBox<User> userComboBox;

    @FXML private TextField bookSearchField;
    @FXML private ComboBox<Book> bookComboBox;

    @FXML private DatePicker dueDatePicker;
    @FXML private Button saveLoanButton;
    @FXML private Button cancelButton;

    /* =========================== SERVICES ========================== */

    private final LoanService loanService =
            ServiceLocator.getLoanService();
    private final LibraryArchiveService archiveService =
            ServiceLocator.getArchiveService();

    private Runnable onLoanRegisteredCallback;

    /* ====================== LISTE E FILTRI ========================= */

    private ObservableList<User> allUsers;
    private FilteredList<User>   filteredUsers;

    private ObservableList<Book> allBooks;
    private FilteredList<Book>   filteredBooks;

    /* ========================== INITIALIZE ========================= */

    @FXML
    public void initialize() {

        LibraryArchive archive = archiveService.getLibraryArchive();
        if (archive == null) {
            return;
        }

        /* --------- UTENTI --------- */
        allUsers = FXCollections.observableArrayList(archive.getUsers());
        filteredUsers = new FilteredList<>(allUsers, u -> true);

        userComboBox.setItems(filteredUsers);

        userComboBox.setCellFactory(cb -> new ListCell<>() {
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

        // Listener per la ricerca utenti
        userSearchField.textProperty().addListener((obs, oldText, newText) -> {
            String filter = (newText == null) ? "" : newText.trim().toLowerCase();
            User selected = userComboBox.getSelectionModel().getSelectedItem();

            filteredUsers.setPredicate(user -> {
                if (user == null) return false;

                // Se l'utente è selezionato lo manteniamo sempre visibile,
                // anche se non matcha più il filtro.
                if (selected != null && user.equals(selected)) {
                    return true;
                }

                if (filter.isEmpty()) {
                    return true;
                }

                String composite = (user.getCode() + " "
                        + user.getFirstName() + " "
                        + user.getLastName()).toLowerCase();

                return composite.contains(filter);
            });

            if (!filter.isEmpty() && !userComboBox.isShowing()) {
                userComboBox.show();
            }
        });

        /* --------- LIBRI --------- */
        allBooks = FXCollections.observableArrayList(archive.getBooks());
        filteredBooks = new FilteredList<>(allBooks, b -> true);

        bookComboBox.setItems(filteredBooks);

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

        // Listener per la ricerca libri
        bookSearchField.textProperty().addListener((obs, oldText, newText) -> {
            String filter = (newText == null) ? "" : newText.trim().toLowerCase();
            Book selected = bookComboBox.getSelectionModel().getSelectedItem();

            filteredBooks.setPredicate(book -> {
                if (book == null) return false;

                // Mantieni il libro selezionato sempre visibile
                if (selected != null && book.equals(selected)) {
                    return true;
                }

                if (filter.isEmpty()) {
                    return true;
                }

                String composite = (book.getIsbn() + " "
                        + book.getTitle()).toLowerCase();

                return composite.contains(filter);
            });

            if (!filter.isEmpty() && !bookComboBox.isShowing()) {
                bookComboBox.show();
            }
        });
    }

    /* =================== CALLBACK REGISTRAZIONE ==================== */

    public void setOnLoanRegisteredCallback(Runnable callback) {
        this.onLoanRegisteredCallback = callback;
    }

    /* ======================= CONFERMA PRESTITO ===================== */

    @FXML
    private void confirmLoan(ActionEvent event) {
        try {
            User selectedUser = userComboBox.getSelectionModel().getSelectedItem();
            Book selectedBook = bookComboBox.getSelectionModel().getSelectedItem();
            LocalDate due     = dueDatePicker.getValue();

            if (selectedUser == null || selectedBook == null || due == null) {
                showError("Compila tutti i campi (utente, libro, data di restituzione).");
                return;
            }

            if (due.isBefore(LocalDate.now())) {
                showError("La data di restituzione prevista non può essere nel passato.");
                return;
            }

            LibraryArchive archive = archiveService.getLibraryArchive();
            if (archive == null) {
                showError("Archivio non disponibile. Impossibile registrare il prestito.");
                return;
            }

            // Verifico che utente e libro esistano ancora in archivio
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

            Loan loan = loanService.registerLoan(user, book, due);

            showInfo("Prestito registrato correttamente.\nID prestito: " + loan.getLoanId());

            if (onLoanRegisteredCallback != null) {
                onLoanRegisteredCallback.run();
            }

            closeStage(event);

        } catch (MandatoryFieldException |
                 NoAvailableCopiesException |
                 MaxLoansReachedException ex) {

            showError(ex.getMessage());

        } catch (RuntimeException ex) {
            showError("Si è verificato un errore durante la registrazione del prestito.\nDettagli: "
                    + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /* ===================== ANNULLA / CHIUDI ======================== */

    @FXML
    private void cancelOperation(ActionEvent event) {
        closeStage(event);
    }

    private void closeStage(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource())
                .getScene().getWindow();
        stage.close();
    }

    /* ======================= UTIL PER GLI ALERT ==================== */

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