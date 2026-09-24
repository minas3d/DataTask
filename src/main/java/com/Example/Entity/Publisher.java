package com.Example.Entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
@Entity
public class Publisher {
    @Id
    private String id;
    private String name;

    @OneToMany(mappedBy = "publisher", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Book> books = new ArrayList<>();

    public Publisher(String id, String name) {
       setId( id);
       setName( name);

    }

    public void addBook(Book book) {
        books.add(book);
        book.setPublisher(this);
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }
}
