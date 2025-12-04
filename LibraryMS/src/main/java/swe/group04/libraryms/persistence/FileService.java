/**
*@file FileService.java
*@brief Interfaccia per la gestione generica delle operazioni di I/O su file.
*
*Questa interfaccia fornisce metodi generici per leggere e scrivere oggetti
*su file, verificare l'esistenza di file e cancellarli. La gestione del
*formato di serializzazione è demandata all'implementazione concreta.
* 
*L'interfaccia rappresenta uno strato generico di accesso al file system,
*indipendente dal dominio dell'applicazione.
*/
package swe.group04.libraryms.persistence;

public class FileService {
    
    public <T> void writeToFile(String path, T data){
    
    }
    
    public <T> T readFromFile(String path, Class<T> type) {
        return null;
    }
}
