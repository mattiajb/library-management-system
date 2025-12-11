package swe.group04.libraryms.service;

import swe.group04.libraryms.models.LibraryArchive;

public class ServiceLocator {

    private static final LibraryArchive archive = new LibraryArchive();

    private static final LibraryArchiveService archiveService =
            new LibraryArchiveService(archive);

    private static final BookService bookService =
            new BookService(archive);

    private static final UserService userService =
            new UserService(archive);

    private static final LoanService loanService =
            new LoanService(archive);

    public static LibraryArchiveService getArchiveService() {
        return archiveService;
    }

    public static BookService getBookService() {
        return bookService;
    }

    public static UserService getUserService() {
        return userService;
    }

    public static LoanService getLoanService() {
        return loanService;
    }
}
