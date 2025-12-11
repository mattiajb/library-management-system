package swe.group04.libraryms.service;

import swe.group04.libraryms.models.LibraryArchive;

public class ServiceLocator {

    private static final LibraryArchiveService archiveService =
            new LibraryArchiveService(new LibraryArchive());

    private static final BookService bookService =
            new BookService(archiveService);

    private static final UserService userService =
            new UserService(archiveService);

    private static final LoanService loanService =
            new LoanService(archiveService);

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

