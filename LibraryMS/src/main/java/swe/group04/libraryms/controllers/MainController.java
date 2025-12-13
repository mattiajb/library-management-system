/**
 * @brief Gestisce le azioni della schermata principale e la navigazione tra scene.
 *
 * @pre Il file FXML associato inietta correttamente i pulsanti annotati con @FXML.
 */
package swe.group04.libraryms.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import swe.group04.libraryms.service.ServiceLocator;

import java.io.IOException;

/**
 * @file MainController.java
 * @brief Controller JavaFX della schermata principale dell'applicazione.
 *
 * Gestisce la navigazione verso le principali funzionalità (catalogo libri,
 * lista utenti, gestione prestiti) e l'uscita dall'applicazione.
 * All'avvio tenta di caricare l'archivio tramite il servizio ottenuto da ServiceLocator.
 */
public class MainController {

    @FXML private Button exitBtn;
    @FXML private Button bookCatalogBtn;
    @FXML private Button usersListBtn;
    @FXML private Button loansListBtn;
    
    /**
     * @brief Inizializza la schermata principale caricando l'archivio applicativo.
     *
     * Viene chiamato automaticamente da JavaFX dopo il caricamento del FXML.
     *
     * @pre ServiceLocator.getArchiveService() restituisce un servizio valido.
     * @post Se il caricamento va a buon fine, i dati dell'archivio risultano disponibili in memoria.
     * @post In caso di errore, l'applicazione rimane avviata ma l'archivio potrebbe non essere disponibile.
     */
    @FXML
    public void initialize() {
        try {
            ServiceLocator.getArchiveService().loadArchive();
            System.out.println("Archivio caricato correttamente.");
        } catch (Exception e) {
            System.err.println("Impossibile caricare l'archivio: " + e.getMessage());
        }
    }
    
    /**
     * @brief Sostituisce la scena corrente caricando un file FXML e applicando il CSS.
     *
     * @param fxmlPath Percorso della risorsa FXML da caricare.
     * @param title Titolo da impostare nello Stage.
     * @param event Evento generato dall'azione utente che ha richiesto la navigazione.
     * @pre event != null e la sorgente dell'evento appartiene a una Scene con uno Stage valido.
     * @post Se il caricamento va a buon fine, lo Stage corrente mostra la nuova scena.
     * @post In caso di errore di caricamento, viene mostrato un Alert di errore.
     */
    private void switchScene(String fxmlPath, String title, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/swe/group04/libraryms/css/style.css").toExternalForm()
            );

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);

        } catch (IOException e) {
            showError("Errore durante il caricamento della schermata: " + title);
        }
    }
    
    /**
     * @brief Apre la schermata di consultazione del catalogo libri.
     *
     * @param event Evento generato dal click sul pulsante "Catalogo Libri".
     * @pre event != null e la sorgente dell'evento appartiene a una Scene con uno Stage valido.
     * @post Se il caricamento va a buon fine, la scena corrente viene sostituita con BookCatalog.fxml.
     */
    @FXML
    public void openBookCatalog(ActionEvent event) {
        switchScene("/swe/group04/libraryms/view/BookCatalog.fxml", "Catalogo Libri",event);
    }
    
    /**
     * @brief Apre la schermata di gestione/visualizzazione della lista utenti.
     *
     * @param event Evento generato dal click sul pulsante "Lista Utenti".
     * @pre event != null e la sorgente dell'evento appartiene a una Scene con uno Stage valido.
     * @post Se il caricamento va a buon fine, la scena corrente viene sostituita con UsersList.fxml.
     */
    @FXML
    public void openUsersList(ActionEvent event) {
        switchScene("/swe/group04/libraryms/view/UsersList.fxml", "Lista Utenti", event);
    }
    
    /**
     * @brief Apre la schermata di gestione dei prestiti.
     *
     * @param event Evento generato dal click sul pulsante "Gestione Prestiti".
     * @pre event != null e la sorgente dell'evento appartiene a una Scene con uno Stage valido.
     * @post Se il caricamento va a buon fine, la scena corrente viene sostituita con LoansList.fxml.
     */   
    @FXML
    public void openLoansList(ActionEvent event) {
        switchScene("/swe/group04/libraryms/view/LoansList.fxml", "Gestione Prestiti", event);
    }
    
    /**
     * @brief Chiude la finestra principale e termina l'esecuzione dell'interfaccia grafica.
     *
     * @param event Evento generato dal click sul pulsante di uscita.
     * @pre event != null e la sorgente dell'evento appartiene a una Scene con uno Stage valido.
     * @post Lo Stage corrente risulta chiuso.
     */
    @FXML
    private void exit(ActionEvent event) {
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
}
