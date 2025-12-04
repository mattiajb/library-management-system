package swe.group04.libraryms.persistence;

import java.io.IOException;
import swe.group04.libraryms.models.LibraryArchive;

public class ArchiveFileService {
    
    private String archiveFilePath;
    private FileService fileService;
    
    public void setArchiveFilePath(String path) {
        this.archiveFilePath = path;
    }
    
    public String getArchiveFilePath(){
        return this.archiveFilePath;
    }
    
    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }
    
    public LibraryArchive loadArchive() throws IOException {
        return null;
    }
    
    public void saveArchive(LibraryArchive archive) throws IOException{
  
    }
}

