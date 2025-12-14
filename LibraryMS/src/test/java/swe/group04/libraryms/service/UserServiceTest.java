/**
 * @file UserServiceTest.java
 * @ingroup TestsService
 * @brief Suite di test di unità per il servizio applicativo UserService.
 *
 * Verifica:
 * - inserimento di utenti con campi validi;
 * - gestione di campi obbligatori mancanti (MandatoryFieldException);
 * - validazione del dominio email istituzionale (@unisa.it) (InvalidEmailException);
 * - vincolo di unicità della matricola/codice utente;
 * - ricerca testuale su nome, cognome, codice ed email;
 * - vincolo di rimozione: impossibilità di eliminare utenti con prestiti attivi (UserHasActiveLoanException).
 *
 * @note I test utilizzano una persistenza "in-memory" (fake) per evitare accesso
 *       al file system e garantire isolamento e ripetibilità dei casi di prova.
 */
package swe.group04.libraryms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import swe.group04.libraryms.exceptions.InvalidEmailException;
import swe.group04.libraryms.exceptions.MandatoryFieldException;
import swe.group04.libraryms.exceptions.UserHasActiveLoanException;
import swe.group04.libraryms.models.Book;
import swe.group04.libraryms.models.LibraryArchive;
import swe.group04.libraryms.models.User;
import swe.group04.libraryms.persistence.ArchiveFileService;
import swe.group04.libraryms.persistence.FileService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @brief Test di unità per UserService.
 *
 * La classe include test per:
 * - addUser: validazioni e inserimento;
 * - searchUsers: match testuale sui campi principali;
 * - removeUser: vincolo prestiti attivi.
 */
class UserServiceTest {

     /**
     * @brief Fake di persistenza in-memory.
     *
     * Estende ArchiveFileService per compatibilità con LibraryArchiveService,
     */
    private static class InMemoryArchiveFileService extends ArchiveFileService {
        
        //  Riferimento all'archivio memorizzato (può essere null)
        private LibraryArchive stored;

        InMemoryArchiveFileService() {
            super("IGNORED.bin", new FileService());
        }

        @Override
        public LibraryArchive loadArchive() throws IOException { return stored; }

        @Override
        public void saveArchive(LibraryArchive archive) throws IOException { stored = archive; }
    }

    //  Servizio di archiviazione usato dal UserService
    private LibraryArchiveService archiveService;
        
    private UserService userService;

    /**
     * @brief Inizializza il contesto di test prima di ogni caso di prova.
     *
     * Prepara un LibraryArchiveService che usa un ArchiveFileService fittizio in-memory,
     * così nessun test accede a file reali.
     */
    @BeforeEach
    void setUp() {
        archiveService = new LibraryArchiveService(new InMemoryArchiveFileService());
        userService = new UserService(archiveService);
    }

    /* ======================================================
                                addUser
       ====================================================== */
    
    /**
     * @brief Verifica che addUser inserisca correttamente un utente valido in archivio.
     *
     * Si aggiunge un utente conforme ai vincoli (email @unisa.it e campi obbligatori valorizzati)
     * e si controlla che l'archivio lo contenga.
     */
    @Test
    @DisplayName("addUser: inserisce utente valido")
    void addUserAdds() throws Exception {
        User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        userService.addUser(u);

        assertEquals(1, archiveService.getLibraryArchive().getUsers().size());
        assertTrue(archiveService.getLibraryArchive().getUsers().contains(u));
    }

    /**
     * @brief Verifica che addUser lanci MandatoryFieldException quando il parametro user è null.
     *
     * La chiamata a addUser(null) deve produrre MandatoryFieldException.
     */
    @Test
    @DisplayName("addUser: null -> MandatoryFieldException")
    void addUserNullThrows() {
        assertThrows(MandatoryFieldException.class, () -> userService.addUser(null));
    }

    /**
     * @brief Verifica che addUser lanci InvalidEmailException se l'email non appartiene al dominio unisa.it.
     */
    @Test
    @DisplayName("addUser: email non unisa.it -> InvalidEmailException")
    void addUserInvalidEmailThrows() {
        User u = new User("Mario", "Rossi", "mario@gmail.com", "S1");
        assertThrows(InvalidEmailException.class, () -> userService.addUser(u));
    }

    /**
     * @brief Verifica che addUser lanci MandatoryFieldException se la matricola/codice è duplicata.
     *
     * L'inserimento del primo utente è ok; l'inserimento di un secondo utente con stesso code deve fallire.
     */
    @Test
    @DisplayName("addUser: matricola duplicata -> MandatoryFieldException")
    void addUserDuplicateCodeThrows() throws Exception {
        User u1 = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        User u2 = new User("Luca", "Bianchi", "l.bianchi@unisa.it", "S1");

        userService.addUser(u1);
        assertThrows(MandatoryFieldException.class, () -> userService.addUser(u2));
    }
    
    /* ======================================================
                            updateUser
       ====================================================== */
/**
 * @brief Verifica che updateUser non lanci eccezioni con dati validi.
 */
@Test
@DisplayName("updateUser: valido -> non lancia")
void updateUserValidDoesNotThrow() throws Exception {
    //  arrange: inserisco un utente valido
    User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
    userService.addUser(u);

    //  act: modifico campi non-final (code è final)
    u.setFirstName("Maria");
    u.setLastName("Rossi2");
    u.setEmail("m.rossi2@unisa.it");

    //  assert
    assertDoesNotThrow(() -> userService.updateUser(u));
}

/**
 * @brief Verifica che updateUser lanci MandatoryFieldException quando user è null.
 */
@Test
@DisplayName("updateUser: null -> MandatoryFieldException")
void updateUserNullThrows() {
    assertThrows(MandatoryFieldException.class, () -> userService.updateUser(null));
}

/**
 * @brief Verifica che updateUser lanci InvalidEmailException se l'email non è @unisa.it.
 */
@Test
@DisplayName("updateUser: email non unisa.it -> InvalidEmailException")
void updateUserInvalidEmailThrows() throws Exception {
    User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
    userService.addUser(u);

    u.setEmail("mario@gmail.com");

    assertThrows(InvalidEmailException.class, () -> userService.updateUser(u));
}

/**
 * @brief Verifica che updateUser lanci MandatoryFieldException se un campo obbligatorio è blank.
 */
@Test
@DisplayName("updateUser: cognome blank -> MandatoryFieldException")
void updateUserBlankLastNameThrows() throws Exception {
    User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
    userService.addUser(u);

    u.setLastName("   "); // blank

    assertThrows(MandatoryFieldException.class, () -> userService.updateUser(u));
}

/**
 * @brief Verifica il vincolo di unicità in update.
 *
 * Poiché code è final, non si può "cambiare" la matricola di un utente esistente.
 * Questo test copre il caso in cui venga passato un OGGETTO DIVERSO con code già presente,
 * che deve essere rifiutato da validateMatricolaUniquenessOnUpdate.
 */
@Test
@DisplayName("updateUser: altro oggetto con stessa matricola -> MandatoryFieldException")
void updateUserDuplicateCodeThrows() throws Exception {
    User u1 = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
    userService.addUser(u1);

    // oggetto diverso ma stesso code
    User impostor = new User("Luca", "Bianchi", "l.bianchi@unisa.it", "S1");

    assertThrows(MandatoryFieldException.class, () -> userService.updateUser(impostor));
}

    /* ======================================================
                            searchBooks
       ====================================================== */
    
    /**
     * @brief Verifica che searchUsers trovi corrispondenze su nome, cognome, codice ed email.
     *
     * Inserisce due utenti e verifica che ricerche mirate producano un singolo match:
     * - match su sottostringa del cognome;
     * - match su codice utente;
     * - match su prefisso/parte dell'email.
     */
    @Test
    @DisplayName("searchUsers: match su nome/cognome/codice/email")
    void searchUsersMatches() throws Exception {
        User u1 = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        User u2 = new User("Luca", "Bianchi", "l.bianchi@unisa.it", "S2");
        userService.addUser(u1);
        userService.addUser(u2);

        assertEquals(1, userService.searchUsers("ross").size());
        assertEquals(1, userService.searchUsers("S2").size());
        assertEquals(1, userService.searchUsers("l.bianchi").size());
    }

    
    /* ======================================================
                            removeUser
       ====================================================== */
    
    /**
     * @brief Verifica che removeUser lanci UserHasActiveLoanException se l'utente ha prestiti attivi.
     *
     * Crea un prestito attivo nell'archivio per l'utente e verifica che la rimozione fallisca.
     */
    @Test
    @DisplayName("removeUser: se ha prestiti attivi -> UserHasActiveLoanException")
    void removeUserThrowsIfActiveLoans() throws Exception {
        LibraryArchive a = archiveService.getLibraryArchive();

        User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        a.addUser(u);

        Book b = new Book("Titolo", List.of("Autore"), Year.now().getValue(), "1111111111", 1);
        a.addBook(b);

        //  prestito attivo
        a.addLoan(u, b, LocalDate.now().plusDays(7));

        assertThrows(UserHasActiveLoanException.class, () -> userService.removeUser(u));
    }

    /**
     * @brief Verifica che removeUser rimuova correttamente un utente privo di prestiti attivi.
     *
     * Inserisce un utente tramite servizio, lo rimuove e controlla che l'archivio non contenga utenti.
     */
    @Test
    @DisplayName("removeUser: senza prestiti attivi -> rimuove")
    void removeUserRemoves() throws Exception {
        User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        userService.addUser(u);

        assertDoesNotThrow(() -> userService.removeUser(u));
        assertTrue(archiveService.getLibraryArchive().getUsers().isEmpty());
    }
}