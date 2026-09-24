package com.Example;

import com.Example.Entity.Author;
import com.Example.Entity.Book;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.hibernate.LazyInitializationException;

import java.util.ArrayList;
import java.util.List;

public class QueryDemo {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("libraryPU");

        EntityManager seedEm = emf.createEntityManager();
        seedEm.getTransaction().begin();
        DataSeeder.seed(seedEm);
        seedEm.getTransaction().commit();
        seedEm.close();

        findBooksByAuthorName(emf, "J.R.R. Tolkien");
        findBooksByPublisher(emf, "Houghton Mifflin");
        findBookByIdPositionalParam(emf, "B1");
        fetchAuthorWithBooksJoinFetch(emf, "A1");
        countBooksPerAuthor(emf);
        findBooksByTitleCriteria(emf, "Hobbit");
        findBooksDynamicCriteria(emf, "Ring", null);
        findBooksDynamicCriteria(emf, null, "J.K. Rowling");
        findBooksDynamicCriteria(emf, null, null);
        compareLazyVsJoinFetch(emf);
        standardJpqlAndHqlNote(emf);

        emf.close();
    }


    public static List<Book> findBooksByAuthorName(EntityManagerFactory emf, String authorName) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Book> query = em.createQuery(
                    "SELECT b FROM Book b WHERE b.author.name = :authorName", Book.class);
            query.setParameter("authorName", authorName);
            List<Book> books = query.getResultList();

            System.out.println("\n--- Books by author '" + authorName + "' ---");
            books.forEach(b -> System.out.println("  " + b.getName()));
            return books;
        } finally {
            em.close();
        }
    }

    public static List<Book> findBooksByPublisher(EntityManagerFactory emf, String publisherName) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Book> query = em.createQuery(
                    "SELECT b FROM Book b WHERE b.publisher.name = :publisherName", Book.class);
            query.setParameter("publisherName", publisherName);
            List<Book> books = query.getResultList();

            System.out.println("\n--- Books published by '" + publisherName + "' ---");
            books.forEach(b -> System.out.println("  " + b.getName()));
            return books;
        } finally {
            em.close();
        }
    }

    public static Book findBookByIdPositionalParam(EntityManagerFactory emf, String id) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Book> query = em.createQuery(
                    "SELECT b FROM Book b WHERE b.id = ?1", Book.class);
            query.setParameter(1, id);
            Book book = query.getSingleResult();

            System.out.println("\n--- Book with id '" + id + "' (positional param) ---");
            System.out.println("  " + book.getName());
            return book;
        } finally {
            em.close();
        }
    }

    public static Author fetchAuthorWithBooksJoinFetch(EntityManagerFactory emf, String authorId) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Author> query = em.createQuery(
                    "SELECT DISTINCT a FROM Author a JOIN FETCH a.books WHERE a.id = :id", Author.class);
            query.setParameter("id", authorId);
            Author author = query.getSingleResult();

            System.out.println("\n--- Author + books via JOIN FETCH ---");
            System.out.println("  " + author.getName() + " -> " + author.getBooks().size() + " book(s)");
            return author;
        } finally {
            em.close();
        }
    }


    public static void countBooksPerAuthor(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Object[]> query = em.createQuery(
                    "SELECT a.name, COUNT(b) FROM Author a LEFT JOIN a.books b GROUP BY a.name",
                    Object[].class);
            List<Object[]> rows = query.getResultList();

            System.out.println("\n--- Book count per author ---");
            for (Object[] row : rows) {
                System.out.println("  " + row[0] + ": " + row[1] + " book(s)");
            }
        } finally {
            em.close();
        }
    }

    public static List<Book> findBooksByTitleCriteria(EntityManagerFactory emf, String titleFragment) {
        EntityManager em = emf.createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Book> cq = cb.createQuery(Book.class);
            Root<Book> book = cq.from(Book.class);

            cq.select(book).where(cb.like(book.get("name"), "%" + titleFragment + "%"));

            List<Book> books = em.createQuery(cq).getResultList();

            System.out.println("\n--- Criteria API: books with title containing '" + titleFragment + "' ---");
            books.forEach(b -> System.out.println("  " + b.getName()));
            return books;
        } finally {
            em.close();
        }
    }


    public static List<Book> findBooksDynamicCriteria(EntityManagerFactory emf, String titleFragment, String authorName) {
        EntityManager em = emf.createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Book> cq = cb.createQuery(Book.class);
            Root<Book> book = cq.from(Book.class);

            List<Predicate> predicates = new ArrayList<>();

            if (titleFragment != null && !titleFragment.isBlank()) {
                predicates.add(cb.like(book.get("name"), "%" + titleFragment + "%"));
            }
            if (authorName != null && !authorName.isBlank()) {
                Join<Object, Object> author = book.join("author", JoinType.INNER);
                predicates.add(cb.equal(author.get("name"), authorName));
            }

            cq.select(book).where(cb.and(predicates.toArray(new Predicate[0])));

            List<Book> books = em.createQuery(cq).getResultList();

            System.out.println("\n--- Criteria API dynamic (title=" + titleFragment + ", author=" + authorName + ") ---");
            books.forEach(b -> System.out.println("  " + b.getName()));
            return books;
        } finally {
            em.close();
        }
    }


    public static void compareLazyVsJoinFetch(EntityManagerFactory emf) {
        System.out.println("\n--- LAZY vs JOIN FETCH ---");


        EntityManager lazyEm = emf.createEntityManager();
        Author lazyAuthor = lazyEm.find(Author.class, "A1");
        lazyEm.close(); // session closed BEFORE books is ever touched

        try {
            lazyAuthor.getBooks().size(); // triggers lazy init -> no session left
            System.out.println("  LAZY: unexpectedly succeeded (no LazyInitializationException)");
        } catch (LazyInitializationException e) {
            System.out.println("  LAZY: LazyInitializationException as expected - "
                    + "\"" + e.getMessage() + "\"");
        }

        EntityManager fetchEm = emf.createEntityManager();
        Author fetchedAuthor = fetchEm.createQuery(
                        "SELECT DISTINCT a FROM Author a JOIN FETCH a.books WHERE a.id = :id", Author.class)
                .setParameter("id", "A1")
                .getSingleResult();
        fetchEm.close();

        System.out.println("  JOIN FETCH: " + fetchedAuthor.getBooks().size()
                + " book(s) read after close() - no exception, already loaded.");
    }

    public static void standardJpqlAndHqlNote(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Book> query = em.createQuery(
                    "SELECT b FROM Book b WHERE b.numOfPage > :minPages ORDER BY b.numOfPage DESC",
                    Book.class);
            query.setParameter("minPages", 200);
            List<Book> books = query.getResultList();

            System.out.println("\n--- Standard JPQL: books with more than 200 pages ---");
            books.forEach(b -> System.out.println("  " + b.getName() + " (" + b.getNumOfPage() + " pages)"));


            System.out.println("\n--- HQL-only feature note ---");
            System.out.println("  See the comment above: HQL's bulk `INSERT INTO ... SELECT`" +
                    " has no equivalent in standard JPQL.");
        } finally {
            em.close();
        }
    }
}
