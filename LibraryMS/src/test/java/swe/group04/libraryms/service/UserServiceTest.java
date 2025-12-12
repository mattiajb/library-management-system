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

class UserServiceTest {

    private static class InMemoryArchiveFileService extends ArchiveFileService {
        private LibraryArchive stored;

        InMemoryArchiveFileService() {
            super("IGNORED.bin", new FileService());
        }

        @Override
        public LibraryArchive loadArchive() throws IOException { return stored; }

        @Override
        public void saveArchive(LibraryArchive archive) throws IOException { stored = archive; }
    }

    private LibraryArchiveService archiveService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        archiveService = new LibraryArchiveService(new InMemoryArchiveFileService());
        userService = new UserService(archiveService);
    }

    @Test
    @DisplayName("addUser: inserisce utente valido")
    void addUserAdds() throws Exception {
        User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        userService.addUser(u);

        assertEquals(1, archiveService.getLibraryArchive().getUsers().size());
        assertTrue(archiveService.getLibraryArchive().getUsers().contains(u));
    }

    @Test
    @DisplayName("addUser: null -> MandatoryFieldException")
    void addUserNullThrows() {
        assertThrows(MandatoryFieldException.class, () -> userService.addUser(null));
    }

    @Test
    @DisplayName("addUser: email non unisa.it -> InvalidEmailException")
    void addUserInvalidEmailThrows() {
        User u = new User("Mario", "Rossi", "mario@gmail.com", "S1");
        assertThrows(InvalidEmailException.class, () -> userService.addUser(u));
    }

    @Test
    @DisplayName("addUser: matricola duplicata -> MandatoryFieldException")
    void addUserDuplicateCodeThrows() throws Exception {
        User u1 = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        User u2 = new User("Luca", "Bianchi", "l.bianchi@unisa.it", "S1");

        userService.addUser(u1);
        assertThrows(MandatoryFieldException.class, () -> userService.addUser(u2));
    }

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

    @Test
    @DisplayName("removeUser: se ha prestiti attivi -> UserHasActiveLoanException")
    void removeUserThrowsIfActiveLoans() throws Exception {
        LibraryArchive a = archiveService.getLibraryArchive();

        User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        a.addUser(u);

        Book b = new Book("Titolo", List.of("Autore"), Year.now().getValue(), "1111111111", 1);
        a.addBook(b);

        // prestito attivo
        a.addLoan(u, b, LocalDate.now().plusDays(7));

        assertThrows(UserHasActiveLoanException.class, () -> userService.removeUser(u));
    }

    @Test
    @DisplayName("removeUser: senza prestiti attivi -> rimuove")
    void removeUserRemoves() throws Exception {
        User u = new User("Mario", "Rossi", "m.rossi@unisa.it", "S1");
        userService.addUser(u);

        assertDoesNotThrow(() -> userService.removeUser(u));
        assertTrue(archiveService.getLibraryArchive().getUsers().isEmpty());
    }
}