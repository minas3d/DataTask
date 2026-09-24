package com.Example.Entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Author {
  private String name ;
  private int age;
  @Id
  private String id;
  @OneToMany(mappedBy ="author", cascade = CascadeType.ALL, orphanRemoval = true)
 private List<Book> books =new ArrayList<>();

  public Author(String name, int age, String id) {
        setName(name);
        setAge(age);

       setID(id);
    }

    public String getName() {
        return name;
    }
    public void addBook(Book book) {
        books.add(book);
        book.setAuthor(this);
    }

    public void removeBook(Book book) {
        books.remove(book);
        book.setAuthor(null);
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getId() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public List<Book> getBooks() {
        return books;
    }
}
