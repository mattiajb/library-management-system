package swe.group04.libraryms.service;

import org.junit.jupiter.api.*;
import swe.group04.libraryms.exceptions.InvalidEmailException;
import swe.group04.libraryms.exceptions.MandatoryFieldException;
import swe.group04.libraryms.exceptions.UserHasActiveLoanException;
import swe.group04.libraryms.models.Book;
import swe.group04.libraryms.models.LibraryArchive;
import swe.group04.libraryms.models.Loan;
import swe.group04.libraryms.models.User;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private static final String DEFAULT_ARCHIVE_FILE = "library-archive.dat";

    private LibraryArchive archive;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = ServiceLocator.getUserService();
        archive = ServiceLocator.getArchiveService().getLibraryArchive();

        clearArchiveCompletely();
    }

    @AfterEach
    void tearDown() {
        File f = new File(DEFAULT_ARCHIVE_FILE);
        if (f.exists()) {
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        }
    }

    /* --------------------------------------------------------------------- */
    /*                                Helpers                                */
    /* --------------------------------------------------------------------- */

    private void clearArchiveCompletely() {
        for (Loan l : archive.getLoans()) {
            archive.removeLoan(l);
        }
        for (Book b : archive.getBooks()) {
            archive.removeBook(b);
        }
        for (User u : archive.getUsers()) {
            archive.removeUser(u);
        }
    }

    private User mkUser(String first, String last, String email, String code) {
        return new User(first, last, email, code);
    }

    /* --------------------------------------------------------------------- */
    /*                              Constructor                               */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("Costruttore: IllegalArgumentException se LibraryArchiveService è null")
    void constructorThrowsIfArchiveServiceNull() {
        assertThrows(IllegalArgumentException.class, () -> new UserService(null));
    }

    /* --------------------------------------------------------------------- */
    /*                                 addUser                                */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("addUser: aggiunge un utente valido e lo rende trovabile per matricola")
    void addUserAddsUserSuccessfully() throws Exception {
        User u = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");

        userService.addUser(u);

        assertEquals(1, archive.getUsers().size());
        assertSame(u, archive.findUserByCode("S0001"));
    }

    @Test
    @DisplayName("addUser: MandatoryFieldException se user è null")
    void addUserThrowsIfNullUser() {
        assertThrows(MandatoryFieldException.class, () -> userService.addUser(null));
    }

    @Test
    @DisplayName("addUser: MandatoryFieldException se firstName è blank")
    void addUserThrowsIfBlankFirstName() {
        User u = mkUser("  ", "Rossi", "mario.rossi@unisa.it", "S0001");
        assertThrows(MandatoryFieldException.class, () -> userService.addUser(u));
    }

    @Test
    @DisplayName("addUser: MandatoryFieldException se lastName è blank")
    void addUserThrowsIfBlankLastName() {
        User u = mkUser("Mario", "   ", "mario.rossi@unisa.it", "S0001");
        assertThrows(MandatoryFieldException.class, () -> userService.addUser(u));
    }

    @Test
    @DisplayName("addUser: MandatoryFieldException se code (matricola) è blank")
    void addUserThrowsIfBlankCode() {
        User u = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "   ");
        assertThrows(MandatoryFieldException.class, () -> userService.addUser(u));
    }

    @Test
    @DisplayName("addUser: MandatoryFieldException se email è blank")
    void addUserThrowsIfBlankEmail() {
        User u = mkUser("Mario", "Rossi", "   ", "S0001");
        assertThrows(MandatoryFieldException.class, () -> userService.addUser(u));
    }

    @Test
    @DisplayName("addUser: InvalidEmailException se email non termina con unisa.it")
    void addUserThrowsIfEmailNotUnisaIt() {
        User u = mkUser("Mario", "Rossi", "mario.rossi@gmail.com", "S0001");
        assertThrows(InvalidEmailException.class, () -> userService.addUser(u));
    }

    @Test
    @DisplayName("addUser: InvalidEmailException se email non rispetta regex (dominio unisa.it obbligatorio)")
    void addUserThrowsIfEmailInvalidFormatEvenIfContainsUnisa() {
        User u = mkUser("Mario", "Rossi", "mario.rossi@unisa", "S0001"); // manca .it
        assertThrows(InvalidEmailException.class, () -> userService.addUser(u));
    }

    @Test
    @DisplayName("addUser: MandatoryFieldException se matricola già presente (unicità su add)")
    void addUserThrowsIfDuplicateCode() throws Exception {
        User u1 = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        User u2 = mkUser("Luigi", "Bianchi", "luigi.bianchi@unisa.it", "S0001");

        userService.addUser(u1);
        assertThrows(MandatoryFieldException.class, () -> userService.addUser(u2));
    }

    /* --------------------------------------------------------------------- */
    /*                               updateUser                               */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("updateUser: non lancia con utente valido e già presente")
    void updateUserValidDoesNotThrow() throws Exception {
        User u = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        userService.addUser(u);

        u.setLastName("Verdi");
        assertDoesNotThrow(() -> userService.updateUser(u));

        assertEquals("Verdi", archive.findUserByCode("S0001").getLastName());
    }

    @Test
    @DisplayName("updateUser: MandatoryFieldException se user è null")
    void updateUserThrowsIfNull() {
        assertThrows(MandatoryFieldException.class, () -> userService.updateUser(null));
    }

    @Test
    @DisplayName("updateUser: InvalidEmailException se email non valida")
    void updateUserThrowsIfInvalidEmail() {
        User u = mkUser("Mario", "Rossi", "mario.rossi@gmail.com", "S0001");
        assertThrows(InvalidEmailException.class, () -> userService.updateUser(u));
    }

    @Test
    @DisplayName("updateUser: MandatoryFieldException se esiste un altro utente con la stessa matricola")
    void updateUserThrowsIfAnotherUserHasSameCode() throws Exception {
        User u1 = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        User u2 = mkUser("Luigi", "Bianchi", "luigi.bianchi@unisa.it", "S0002");

        userService.addUser(u1);
        userService.addUser(u2);

        // provo a cambiare la matricola di u2 a quella di u1
        // (NB: il code è final, quindi non si può cambiare direttamente.
        // Simulo il caso creando un nuovo oggetto con stesso code -> existing != user)
        User cloneWithDuplicateCode = mkUser("Luigi", "Bianchi", "luigi.bianchi@unisa.it", "S0001");

        assertThrows(MandatoryFieldException.class, () -> userService.updateUser(cloneWithDuplicateCode));
    }

    /* --------------------------------------------------------------------- */
    /*                               removeUser                               */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("removeUser: IllegalArgumentException se user è null")
    void removeUserThrowsIfNull() {
        assertThrows(IllegalArgumentException.class, () -> userService.removeUser(null));
    }

    @Test
    @DisplayName("removeUser: rimuove utente se non ha prestiti attivi")
    void removeUserRemovesIfNoActiveLoans() throws Exception {
        User u = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        userService.addUser(u);

        assertDoesNotThrow(() -> userService.removeUser(u));
        assertNull(archive.findUserByCode("S0001"));
        assertTrue(archive.getUsers().isEmpty());
    }

    @Test
    @DisplayName("removeUser: UserHasActiveLoanException se esiste un prestito attivo per l'utente")
    void removeUserThrowsIfActiveLoanExists() throws Exception {
        User u = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        userService.addUser(u);

        Book b = new Book("Reti", List.of("Kurose"), 2020, "1234567890", 1);
        archive.addBook(b);

        // Creo prestito attivo in archivio
        archive.addLoan(u, b, LocalDate.now().plusDays(7));

        assertThrows(UserHasActiveLoanException.class, () -> userService.removeUser(u));
        assertNotNull(archive.findUserByCode("S0001"));
    }

    /* --------------------------------------------------------------------- */
    /*                        getUsersSortedByLastName                         */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("getUsersSortedByLastName: ordina per cognome case-insensitive")
    void getUsersSortedByLastNameSortsCaseInsensitive() throws Exception {
        User u1 = mkUser("Mario", "zeta", "mario.zeta@unisa.it", "S0001");
        User u2 = mkUser("Luca", "Alpha", "luca.alpha@unisa.it", "S0002");
        User u3 = mkUser("Anna", "beta", "anna.beta@unisa.it", "S0003");

        userService.addUser(u1);
        userService.addUser(u2);
        userService.addUser(u3);

        List<User> sorted = userService.getUsersSortedByLastName();
        assertEquals(List.of(u2, u3, u1), sorted);
    }

    /* --------------------------------------------------------------------- */
    /*                               searchUsers                              */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("searchUsers: IllegalArgumentException se query è null")
    void searchUsersThrowsIfNullQuery() {
        assertThrows(IllegalArgumentException.class, () -> userService.searchUsers(null));
    }

    @Test
    @DisplayName("searchUsers: query vuota => restituisce tutti gli utenti ordinati per cognome")
    void searchUsersEmptyReturnsAllSortedByLastName() throws Exception {
        User u1 = mkUser("Mario", "zeta", "mario.zeta@unisa.it", "S0001");
        User u2 = mkUser("Luca", "Alpha", "luca.alpha@unisa.it", "S0002");
        User u3 = mkUser("Anna", "beta", "anna.beta@unisa.it", "S0003");

        userService.addUser(u1);
        userService.addUser(u2);
        userService.addUser(u3);

        List<User> res = userService.searchUsers("   ");
        assertEquals(List.of(u2, u3, u1), res);
    }

    @Test
    @DisplayName("searchUsers: match su cognome (case-insensitive)")
    void searchUsersMatchesLastName() throws Exception {
        User u1 = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        User u2 = mkUser("Luca", "Bianchi", "luca.bianchi@unisa.it", "S0002");

        userService.addUser(u1);
        userService.addUser(u2);

        List<User> res = userService.searchUsers("ross");
        assertEquals(List.of(u1), res);
    }

    @Test
    @DisplayName("searchUsers: match su nome (case-insensitive)")
    void searchUsersMatchesFirstName() throws Exception {
        User u1 = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        User u2 = mkUser("Luca", "Bianchi", "luca.bianchi@unisa.it", "S0002");

        userService.addUser(u1);
        userService.addUser(u2);

        List<User> res = userService.searchUsers("luc");
        assertEquals(List.of(u2), res);
    }

    @Test
    @DisplayName("searchUsers: match su matricola (case-insensitive)")
    void searchUsersMatchesCode() throws Exception {
        User u1 = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        User u2 = mkUser("Luca", "Bianchi", "luca.bianchi@unisa.it", "S0002");

        userService.addUser(u1);
        userService.addUser(u2);

        List<User> res = userService.searchUsers("s0002");
        assertEquals(List.of(u2), res);
    }

    @Test
    @DisplayName("searchUsers: match su email (case-insensitive)")
    void searchUsersMatchesEmail() throws Exception {
        User u1 = mkUser("Mario", "Rossi", "mario.rossi@unisa.it", "S0001");
        User u2 = mkUser("Luca", "Bianchi", "luca.bianchi@unisa.it", "S0002");

        userService.addUser(u1);
        userService.addUser(u2);

        List<User> res = userService.searchUsers("mario.rossi@unisa.it");
        assertEquals(List.of(u1), res);
    }

    @Test
    @DisplayName("searchUsers: se più utenti matchano, il risultato è ordinato per cognome")
    void searchUsersMultipleMatchesAreSortedByLastName() throws Exception {
        User u1 = mkUser("Mario", "Zeta", "mario.zeta@unisa.it", "S0001");
        User u2 = mkUser("Luca", "Alpha", "luca.alpha@unisa.it", "S0002");
        User u3 = mkUser("Anna", "Beta", "anna.beta@unisa.it", "S0003");

        userService.addUser(u1);
        userService.addUser(u2);
        userService.addUser(u3);

        // "unisa.it" matcha tutti -> devono tornare ordinati per cognome
        List<User> res = userService.searchUsers("unisa.it");
        assertEquals(List.of(u2, u3, u1), res);
    }
}