package swe.group04.libraryms.service;

import java.util.List;
import swe.group04.libraryms.exceptions.*;
import swe.group04.libraryms.models.LibraryArchive;
import swe.group04.libraryms.models.User;

public class UserService {
    
    private LibraryArchive libraryArchive;
    private LibraryArchiveService libraryArchiveService;
    
    public void addUser(User user) throws MandatoryFieldException, InvalidEmailException{
      
    }
    
    public void updateUser(User user) throws MandatoryFieldException, InvalidEmailException{
      
    }
    
    public void removeUser(User user) throws UserHasActiveLoanException{
      
    }
    
    public List<User> getUsersSortedByLastName() {
        return null;
    }
    
    public List<User> searchUsers(String query) {
        return null;
    }
}
