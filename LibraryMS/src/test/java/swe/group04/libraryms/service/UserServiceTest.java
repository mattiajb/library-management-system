package swe.group04.libraryms.service;

import org.junit.jupiter.api.*;

import swe.group04.libraryms.exceptions.MandatoryFieldException;
import swe.group04.libraryms.exceptions.UserHasActiveLoanException;
import swe.group04.libraryms.models.Book;
import swe.group04.libraryms.models.LibraryArchive;
import swe.group04.libraryms.models.Loan;
import swe.group04.libraryms.models.User;
import swe.group04.libraryms.persistence.ArchiveFileService;
import swe.group04.libraryms.persistence.FileService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di unità per UserService.
 */
class UserServiceTest {

    private LibraryArchive archive;
    private FileService fileService;
    private ArchiveFileService archiveFileService;
    private LibraryArchiveService libraryArchiveService;
    private UserService userService;
    private String testFilePath;

    @BeforeEach
    void setUp() {
        archive = new LibraryArchive();

        fileService = new FileService();
        testFilePath = "userServiceTestArchive.bin";

        // pulizia preventiva del file di test
        File f = new File(testFilePath);
        if (f.exists()) {
            f.delete();
        }

        archiveFileService = new ArchiveFileService(testFilePath, fileService);
        libraryArchiveService = new LibraryArchiveService(archiveFileService);

        userService = new UserService(archive, libraryArchiveService);
    }

    @AfterEach
    void tearDown() {
        File f = new File(testFilePath);
        if (f.exists()) {
            f.delete();
        }
    }

    // ------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------

    private User createUser(String firstName, String lastName, String code, String email) {
        return new User(firstName, lastName, email, code);
    }

    private Book createBook() {
        return new Book(
                "Titolo",
                List.of("Autore"),
                2020,
                "1234567890",
                3
        );
    }

    // ============================================================
    // COSTRUTTORE
    // ============================================================

    @Test
    @DisplayName("Costruttore: libraryArchive null -> IllegalArgumentException")
    void constructorNullArchiveThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new UserService(null, libraryArchiveService));
    }

    @Test
    @DisplayName("Costruttore: libraryArchiveService null -> IllegalArgumentException")
    void constructorNullArchiveServiceThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new UserService(archive, null));
    }

    // ============================================================
    // addUser
    // ============================================================

    @Test
    @DisplayName("addUser: utente valido -> aggiunto e archivio salvato")
    void addUserValidUserAddedAndPersisted() throws IOException, ClassNotFoundException {
        User user = createUser("Mario", "Rossi", "S123", "mario.rossi@example.com");

        userService.addUser(user);

        // in memoria
        assertTrue(archive.getUsers().contains(user));

        // su file
        Object read = fileService.readFromFile(testFilePath);
        assertNotNull(read);
        assertTrue(read instanceof LibraryArchive);

        LibraryArchive loaded = (LibraryArchive) read;
        assertEquals(1, loaded.getUsers().size());
        assertEquals("S123", loaded.getUsers().get(0).getCode());
    }

    @Test
    @DisplayName("addUser: utente null -> MandatoryFieldException")
    void addUserNullUserThrowsMandatoryFieldException() {
        MandatoryFieldException ex = assertThrows(MandatoryFieldException.class,
                () -> userService.addUser(null));

        assertTrue(ex.getMessage().contains("L'utente non può essere nullo"));
    }

    @Test
    @DisplayName("addUser: nome vuoto -> MandatoryFieldException")
    void addUserBlankFirstNameThrowsMandatoryFieldException() {
        User user = createUser("   ", "Rossi", "S123", "mario.rossi@example.com");

        MandatoryFieldException ex = assertThrows(MandatoryFieldException.class,
                () -> userService.addUser(user));

        assertTrue(ex.getMessage().contains("Il nome è obbligatorio"));
    }

    @Test
    @DisplayName("addUser: cognome vuoto -> MandatoryFieldException")
    void addUserBlankLastNameThrowsMandatoryFieldException() {
        User user = createUser("Mario", "   ", "S123", "mario.rossi@example.com");

        MandatoryFieldException ex = assertThrows(MandatoryFieldException.class,
                () -> userService.addUser(user));

        assertTrue(ex.getMessage().contains("Il cognome è obbligatorio"));
    }

    @Test
    @DisplayName("addUser: matricola vuota -> MandatoryFieldException")
    void addUserBlankCodeThrowsMandatoryFieldException() {
        User user = createUser("Mario", "Rossi", "   ", "mario.rossi@example.com");

        MandatoryFieldException ex = assertThrows(MandatoryFieldException.class,
                () -> userService.addUser(user));

        assertTrue(ex.getMessage().contains("La matricola è obbligatoria"));
    }

    @Test
    @DisplayName("addUser: email nulla o vuota -> MandatoryFieldException (controllo campi obbligatori)")
    void addUserNullOrBlankEmailThrowsMandatoryFieldException() {
        User userNullEmail = createUser("Mario", "Rossi", "S123", null);
        MandatoryFieldException ex1 = assertThrows(MandatoryFieldException.class,
                () -> userService.addUser(userNullEmail));
        assertTrue(ex1.getMessage().contains("L'email è obbligatoria"));

        User userBlankEmail = createUser("Mario", "Rossi", "S123", "   ");
        MandatoryFieldException ex2 = assertThrows(MandatoryFieldException.class,
                () -> userService.addUser(userBlankEmail));
        assertTrue(ex2.getMessage().contains("L'email è obbligatoria"));
    }

    @Test
    @DisplayName("addUser: matricola duplicata -> MandatoryFieldException")
    void addUserDuplicateMatricolaThrowsMandatoryFieldException() throws MandatoryFieldException {
        User existing = createUser("Mario", "Rossi", "S123", "mario.rossi@example.com");
        archive.addUser(existing);

        User duplicate = createUser("Luigi", "Bianchi", "S123", "luigi.bianchi@example.com");

        MandatoryFieldException ex = assertThrows(MandatoryFieldException.class,
                () -> userService.addUser(duplicate));

        assertTrue(ex.getMessage().contains("Esiste già un utente registrato con la stessa matricola"));
    }

    @Test
    @DisplayName("addUser: errore di persistenza -> RuntimeException e utente comunque aggiunto in memoria")
    void addUserPersistFailureThrowsRuntimeException() {
        User user = createUser("Mario", "Rossi", "S123", "mario.rossi@example.com");

        FailingLibraryArchiveService failingService =
                new FailingLibraryArchiveService(archiveFileService);
        failingService.setFailOnSave(true);

        UserService failingUserService = new UserService(archive, failingService);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> failingUserService.addUser(user));

        assertTrue(ex.getMessage().contains("Errore durante il salvataggio dell'archivio utenti"));
        // l'utente è stato comunque aggiunto all'archivio in memoria
        assertTrue(archive.getUsers().contains(user));
    }

    // ============================================================
    // updateUser
    // ============================================================

    @Test
    @DisplayName("updateUser: utente null -> MandatoryFieldException")
    void updateUserNullUserThrowsMandatoryFieldException() {
        MandatoryFieldException ex = assertThrows(MandatoryFieldException.class,
                () -> userService.updateUser(null));

        assertTrue(ex.getMessage().contains("L'utente non può essere nullo"));
    }

    @Test
    @DisplayName("updateUser: nome vuoto -> MandatoryFieldException")
    void updateUserBlankFirstNameThrowsMandatoryFieldException() {
        User user = createUser("Mario", "Rossi", "S123", "mario.rossi@example.com");
        archive.addUser(user);

        user.setFirstName("   ");

        MandatoryFieldException ex = assertThrows(MandatoryFieldException.class,
                () -> userService.updateUser(user));

        assertTrue(ex.getMessage().contains("Il nome è obbligatorio"));
    }

    @Test
    @DisplayName("updateUser: cognome vuoto -> MandatoryFieldException")
    void updateUserBlankLastNameThrowsMandatoryFieldException() {
        User user = createUser("Mario", "Rossi", "S123", "mario.rossi@example.com");
        archive.addUser(user);

        user.setLastName("   ");

        MandatoryFieldException ex = assertThrows(MandatoryFieldException.class,
                () -> userService.updateUser(user));

        assertTrue(ex.getMessage().contains("Il cognome è obbligatorio"));
    }

    @Test
    @DisplayName("updateUser: email nulla o vuota -> MandatoryFieldException (controllo campi obbligatori)")
    void updateUserNullOrBlankEmailThrowsMandatoryFieldException() {
        User user = createUser("Mario", "Rossi", "S123", "mario.rossi@example.com");
        archive.addUser(user);

        user.setEmail(null);
        MandatoryFieldException ex1 = assertThrows(MandatoryFieldException.class,
                () -> userService.updateUser(user));
        assertTrue(ex1.getMessage().contains("L'email è obbligatoria"));

        user.setEmail("   ");
        MandatoryFieldException ex2 = assertThrows(MandatoryFieldException.class,
                () -> userService.updateUser(user));
        assertTrue(ex2.getMessage().contains("L'email è obbligatoria"));
    }

    @Test
    @DisplayName("updateUser: matricola duplicata con altro utente -> MandatoryFieldException")
    void updateUserDuplicateCodeWithOtherUserThrowsMandatoryFieldException() {
        // user1 e user2 hanno la STESSA matricola "S123" ma sono istanze diverse
        User user1 = createUser("Mario", "Rossi", "S123", "mario.rossi@example.com");
        User user2 = createUser("Luigi", "Bianchi", "S123", "luigi.bianchi@example.com");

        archive.addUser(user1);
        archive.addUser(user2);

        MandatoryFieldException ex = assertThrows(MandatoryFieldException.class,
                () -> userService.updateUser(user2));

        assertTrue(ex.getMessage().contains("Esiste già un altro utente con la stessa matricola"));
    }

    @Test
    @DisplayName("updateUser: dati validi -> persistChanges chiamato senza eccezioni")
    void updateUserValidUserPersists() throws IOException, ClassNotFoundException {
        User user = createUser("Mario", "Rossi", "S123", "mario.rossi@example.com");
        archive.addUser(user);

        user.setEmail("mario.rossi2@example.com");

        userService.updateUser(user);

        Object read = fileService.readFromFile(testFilePath);
        LibraryArchive loaded = (LibraryArchive) read;
        assertEquals(1, loaded.getUsers().size());
        assertEquals("mario.rossi2@example.com", loaded.getUsers().get(0).getEmail());
    }

    @Test
    @DisplayName("updateUser: errore di persistenza -> RuntimeException e modifiche in memoria mantenute")
    void updateUserPersistFailureThrowsRuntimeException() {
        User user = createUser("Mario", "Rossi", "S123", "mario.rossi@example.com");
        archive.addUser(user);

        user.setEmail("nuova.email@example.com");

        FailingLibraryArchiveService failingService =
                new FailingLibraryArchiveService(archiveFileService);
        failingService.setFailOnSave(true);

        UserService failingUserService = new UserService(archive, failingService);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> failingUserService.updateUser(user));

        assertTrue(ex.getMessage().contains("Errore durante il salvataggio dell'archivio utenti"));
        // la modifica in memoria NON viene annullata
        assertEquals("nuova.email@example.com", user.getEmail());
    }

    // ============================================================
    // removeUser
    // ============================================================

    @Test
    @DisplayName("removeUser: utente null -> IllegalArgumentException")
    void removeUserNullThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.removeUser(null));
    }

    @Test
    @DisplayName("removeUser: utente con prestito attivo -> UserHasActiveLoanException")
    void removeUserUserWithActiveLoanThrowsUserHasActiveLoanException() {
        User user = createUser("Mario", "Rossi", "S123", "mario.rossi@example.com");
        archive.addUser(user);

        Book book = createBook();
        archive.addBook(book);

        // creo un prestito attivo
        archive.addLoan(user, book, LocalDate.now().plusDays(7));

        UserHasActiveLoanException ex = assertThrows(UserHasActiveLoanException.class,
                () -> userService.removeUser(user));

        assertTrue(ex.getMessage().contains("prestiti attivi"));
        // l'utente non deve essere rimosso
        assertTrue(archive.getUsers().contains(user));
    }

    @Test
    @DisplayName("removeUser: utente senza prestiti -> rimosso e persistenza OK")
    void removeUserUserWithoutLoansRemovedAndPersisted()
            throws IOException, ClassNotFoundException, UserHasActiveLoanException {
        User user = createUser("Mario", "Rossi", "S123", "mario.rossi@example.com");
        archive.addUser(user);

        userService.removeUser(user);

        assertFalse(archive.getUsers().contains(user));

        Object read = fileService.readFromFile(testFilePath);
        LibraryArchive loaded = (LibraryArchive) read;
        assertEquals(0, loaded.getUsers().size());
    }

    @Test
    @DisplayName("removeUser: errore di persistenza -> RuntimeException e utente comunque rimosso in memoria")
    void removeUserPersistFailureThrowsRuntimeException() {
        User user = createUser("Mario", "Rossi", "S123", "mario.rossi@example.com");
        archive.addUser(user);

        FailingLibraryArchiveService failingService =
                new FailingLibraryArchiveService(archiveFileService);
        failingService.setFailOnSave(true);

        UserService failingUserService = new UserService(archive, failingService);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> failingUserService.removeUser(user));

        assertTrue(ex.getMessage().contains("Errore durante il salvataggio dell'archivio utenti"));
        // l'utente è stato comunque rimosso dall'archivio in memoria
        assertFalse(archive.getUsers().contains(user));
    }

    // ============================================================
    // getUsersSortedByLastName
    // ============================================================

    @Test
    @DisplayName("getUsersSortedByLastName: restituisce utenti ordinati per cognome")
    void getUsersSortedByLastNameReturnsSortedList() {
        User u1 = createUser("Mario", "Rossi", "S1", "mario.rossi@example.com");
        User u2 = createUser("Luigi", "Bianchi", "S2", "luigi.bianchi@example.com");
        User u3 = createUser("Anna", "Verdi", "S3", "anna.verdi@example.com");

        archive.addUser(u1);
        archive.addUser(u2);
        archive.addUser(u3);

        List<User> sorted = userService.getUsersSortedByLastName();

        assertEquals(List.of(u2, u1, u3), sorted);
    }

    // ============================================================
    // searchUsers
    // ============================================================

    @Test
    @DisplayName("searchUsers: query null -> IllegalArgumentException")
    void searchUsersNullQueryThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.searchUsers(null));
    }

    @Test
    @DisplayName("searchUsers: query vuota -> restituisce lista ordinata per cognome")
    void searchUsersEmptyQueryReturnsSortedList() {
        User u1 = createUser("Mario", "Rossi", "S1", "mario.rossi@example.com");
        User u2 = createUser("Luigi", "Bianchi", "S2", "luigi.bianchi@example.com");
        User u3 = createUser("Anna", "Verdi", "S3", "anna.verdi@example.com");

        archive.addUser(u1);
        archive.addUser(u2);
        archive.addUser(u3);

        List<User> result = userService.searchUsers("   ");

        assertEquals(List.of(u2, u1, u3), result);
    }

    @Test
    @DisplayName("searchUsers: match su cognome")
    void searchUsersMatchLastName() {
        User u1 = createUser("Mario", "Rossi", "S1", "mario.rossi@example.com");
        User u2 = createUser("Luigi", "Bianchi", "S2", "luigi.bianchi@example.com");
        archive.addUser(u1);
        archive.addUser(u2);

        List<User> result = userService.searchUsers("ross");

        assertEquals(1, result.size());
        assertEquals(u1, result.get(0));
    }

    @Test
    @DisplayName("searchUsers: match su nome")
    void searchUsersMatchFirstName() {
        User u1 = createUser("Mario", "Rossi", "S1", "mario.rossi@example.com");
        User u2 = createUser("Luigi", "Bianchi", "S2", "luigi.bianchi@example.com");
        archive.addUser(u1);
        archive.addUser(u2);

        List<User> result = userService.searchUsers("luig");

        assertEquals(1, result.size());
        assertEquals(u2, result.get(0));
    }

    @Test
    @DisplayName("searchUsers: match su matricola")
    void searchUsersMatchCode() {
        User u1 = createUser("Mario", "Rossi", "ABC123", "mario.rossi@example.com");
        User u2 = createUser("Luigi", "Bianchi", "XYZ999", "luigi.bianchi@example.com");
        archive.addUser(u1);
        archive.addUser(u2);

        List<User> result = userService.searchUsers("xyz");

        assertEquals(1, result.size());
        assertEquals(u2, result.get(0));
    }

    @Test
    @DisplayName("searchUsers: match su email")
    void searchUsersMatchEmail() {
        User u1 = createUser("Mario", "Rossi", "S1", "mario.rossi@example.com");
        User u2 = createUser("Luigi", "Bianchi", "S2", "luigi.bianchi@domain.com");
        archive.addUser(u1);
        archive.addUser(u2);

        List<User> result = userService.searchUsers("domain.com");

        assertEquals(1, result.size());
        assertEquals(u2, result.get(0));
    }

    // ============================================================
    // Classe di supporto per simulare errore di I/O
    // ============================================================

    private static class FailingLibraryArchiveService extends LibraryArchiveService {

        private boolean failOnSave = false;

        public FailingLibraryArchiveService(ArchiveFileService archiveFileService) {
            super(archiveFileService);
        }

        public void setFailOnSave(boolean failOnSave) {
            this.failOnSave = failOnSave;
        }

        @Override
        public void saveArchive(LibraryArchive archive) throws IOException {
            if (failOnSave) {
                throw new IOException("Simulated IO error");
            }
            super.saveArchive(archive);
        }
    }
}