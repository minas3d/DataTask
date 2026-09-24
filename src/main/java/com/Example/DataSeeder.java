package com.Example;

import com.Example.Entity.*;
import jakarta.persistence.EntityManager;

public class DataSeeder {

    public static void seed(EntityManager em) {

        Author tolkien = new Author( "J.R.R. Tolkien", 81,"202020");
        Author rowling = new Author( "J.K. Rowling", 58,"303030");
        Author orwell = new Author("George Orwell", 46,"404040");

        Publisher houghton = new Publisher("P1", "Houghton Mifflin");
        Publisher bloomsbury = new Publisher("P2", "Bloomsbury");

        Category fantasy = new Category("C1", "Fantasy");
        Category adventure = new Category("C2", "Adventure");


        Book hobbit = new Book("B1", "The Hobbit", 310);
        Book fellowship = new Book("B2", "The Fellowship of the Ring", 423);
        Book philosophersStone = new Book("B3", "Harry Potter and the Philosopher's Stone", 223);



        tolkien.addBook(hobbit);
        tolkien.addBook(fellowship);
        rowling.addBook(philosophersStone);

        houghton.addBook(hobbit);
        houghton.addBook(fellowship);
        bloomsbury.addBook(philosophersStone);



        hobbit.addCategory(fantasy);
        hobbit.addCategory(adventure);
        fellowship.addCategory(fantasy);
        philosophersStone.addCategory(fantasy);

        Employee employee = new Employee();
        employee.setName("Mina Saad");
        employee.setAge(23);
        employee.setSalary(15000.0);

        Customer customer = new Customer();
        customer.setName("Ahmed Ali");
        customer.setAge(30);
        customer.setLoyaltyLevel("GOLD");


        em.persist(tolkien);
        em.persist(rowling);
        em.persist(orwell);
        em.persist(houghton);
        em.persist(bloomsbury);
        em.persist(fantasy);
        em.persist(adventure);
        em.persist(employee);
        em.persist(customer);
    }
}
