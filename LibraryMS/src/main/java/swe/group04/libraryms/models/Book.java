package swe.group04.libraryms.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Book {

    // Spazio degli Attributi
    private String title;
    private List<String> authors;
    private int releaseYear;
    private final String isbn; // Attributo identificativo => Non modificabile
    private int totalCopies;
    private int availableCopies;

    // Costruttore
    public Book(String title, List<String> authors, int releaseYear, String isbn, int totalCopies) {
        this.title = title;
        this.authors = new ArrayList<>(authors);
        this.releaseYear = releaseYear;
        this.isbn = isbn;
        this.totalCopies = totalCopies;
        availableCopies = totalCopies; // Al momento della creazione, non ci sono prestiti attivi
    }

    // Spazio dei metodi

    public String getTitle() { return title; }
    public List<String> getAuthors() { return new ArrayList<>(authors); }
    public int getReleaseYear() { return releaseYear; }
    public String getIsbn() { return isbn; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }

    public void setTitle(String title) { this.title = title; }
    public void setAuthors(List<String> authors) { this.authors = new ArrayList<>(authors); }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }

    public boolean hasAvailableCopies() {
        return availableCopies > 0;
    }

    // equals/hashcode basati su isbn
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.isbn);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        final Book other = (Book)obj;
        if (!Objects.equals(this.isbn, other.isbn)) { return false; }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Title: " + title + "\n");
        sb.append("Authors: " + authors + "\n");
        sb.append("ReleaseYear: " + releaseYear + "\n");
        sb.append("ISBN: " + isbn + "\n");
        sb.append("Total Copies: " + totalCopies + "\n");
        sb.append("Available Copies: " + availableCopies + "\n");
        return sb.toString();
    }

}
