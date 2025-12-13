/**
 * @brief Gestisce l’interazione della GUI per creare un nuovo Loan.
 *
 * @pre Il file FXML associato inietta correttamente tutti i componenti @FXML.
 */
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
 * @file RegisterLoanController.java
 * @brief Controller JavaFX responsabile della registrazione di un nuovo prestito.
 *
 * Permette di selezionare utente e libro tramite ricerca testuale con filtro dinamico
 * e di inserire la data di restituzione prevista. Delega a LoanService la logica
 * di registrazione del prestito e a LibraryArchiveService l’accesso all’archivio.
 */
public class RegisterLoanController {

    @FXML private TextField userSearchField;
    @FXML private ComboBox<User> userComboBox;

    @FXML private TextField bookSearchField;
    @FXML private ComboBox<Book> bookComboBox;

    @FXML private DatePicker dueDatePicker;
    @FXML private Button saveLoanButton;
    @FXML private Button cancelButton;

    //  Servizio prestiti: validazione di dominio e registrazione prestiti
    private final LoanService loanService = ServiceLocator.getLoanService();

    //  Servizio archivio: accesso ai dati caricati (utenti, libri, prestiti)
    private final LibraryArchiveService archiveService = ServiceLocator.getArchiveService();

    //  Callback opzionale eseguita dopo una registrazione riuscita (es. refresh lista prestiti)
    private Runnable onLoanRegisteredCallback;
    
    private ObservableList<User> allUsers;
    private FilteredList<User>   filteredUsers;
    private ObservableList<Book> allBooks;
    private FilteredList<Book>   filteredBooks;

    /**
     * @brief Inizializza i componenti di selezione utente/libro con ricerca e filtro dinamico.
     *
     * Configura:
     * - popolamento delle ComboBox con utenti e libri dall'archivio;
     * - cell factory per la visualizzazione compatta;
     * - listener sui TextField per filtrare dinamicamente le liste (FilteredList).
     *
     * @pre archiveService != null
     * @post Se l'archivio è disponibile, userComboBox e bookComboBox risultano popolate e filtrabili.
     * @post Se l'archivio non è disponibile, l'inizializzazione termina senza configurare le liste.
     */
    @FXML
    public void initialize() {

        LibraryArchive archive = archiveService.getLibraryArchive();
        if (archive == null) {
            return;
        }

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

        //  Listener per la ricerca utenti
        userSearchField.textProperty().addListener((obs, oldText, newText) -> {
            String filter = (newText == null) ? "" : newText.trim().toLowerCase();
            User selected = userComboBox.getSelectionModel().getSelectedItem();

            filteredUsers.setPredicate(user -> {
                if (user == null) return false;

                //  Se l'utente è selezionato lo manteniamo sempre visibile,
                //  anche se non matcha più il filtro.
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

        bookSearchField.textProperty().addListener((obs, oldText, newText) -> {
            String filter = (newText == null) ? "" : newText.trim().toLowerCase();
            Book selected = bookComboBox.getSelectionModel().getSelectedItem();

            filteredBooks.setPredicate(book -> {
                if (book == null) return false;

                if (selected != null && book.equals(selected)) {
                    return true;
                }

                if (filter.isEmpty()) {
                    return true;
                }

                String composite = (book.getIsbn() + " " + book.getTitle()).toLowerCase();

                return composite.contains(filter);
            });

            if (!filter.isEmpty() && !bookComboBox.isShowing()) {
                bookComboBox.show();
            }
        });
    }
    
    /**
     * @brief Registra una callback eseguita dopo la registrazione riuscita di un prestito.
     *
     * @param callback Azione da eseguire dopo il salvataggio; può essere null.
     * @post onLoanRegisteredCallback == callback
     */
    public void setOnLoanRegisteredCallback(Runnable callback) {
        this.onLoanRegisteredCallback = callback;
    }
    
    /**
     * @brief Valida i dati del form e registra un nuovo prestito tramite LoanService.
     *
     * @param event Evento generato dal click sul pulsante di conferma.
     * @pre I componenti FXML (userComboBox, bookComboBox, dueDatePicker) sono non null.
     * @post Se l'operazione ha successo:
     *       - viene creato e registrato un nuovo Loan tramite LoanService;
     *       - viene mostrato un messaggio informativo con l'ID del prestito;
     *       - viene invocata (se presente) la callback;
     *       - la finestra corrente viene chiusa.
     *
     * Validazioni gestite dal controller:
     * - selezione di utente, libro e data non null;
     * - data di restituzione prevista non nel passato;
     * - presenza corrente di utente/libro nell'archivio (anti-stato-stantio).
     *
     * Gestione errori:
     * - MandatoryFieldException: dati obbligatori mancanti secondo la logica di dominio;
     * - NoAvailableCopiesException: nessuna copia disponibile del libro selezionato;
     * - MaxLoansReachedException: utente ha raggiunto il limite massimo di prestiti;
     * - RuntimeException: errore non previsto (es. persistenza/archivio).
     */
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

            //  Verifico che utente e libro esistano ancora in archivio
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
            showError("Si è verificato un errore durante la registrazione del prestito.\nDettagli: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * @brief Annulla l'operazione di registrazione e chiude la finestra corrente.
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
     * @brief Chiude lo Stage associato alla sorgente dell'evento. @param event Evento UI. 
     */
    private void closeStage(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
    
    /**
     * @brief Mostra un Alert di errore con il messaggio fornito.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText("Operazione non riuscita");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * @brief Mostra un Alert informativo con il messaggio fornito.
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informazione");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}