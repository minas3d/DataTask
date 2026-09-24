package com.Example;

import com.Example.Entity.Author;
import jakarta.persistence.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

public class Main {

    public static void main(String[] args) throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("libraryPU");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();
        DataSeeder.seed(em);
        tx.commit();


        em.clear();

        Author reloaded = em.find(Author.class, "A1");
        System.out.println("\n--- Lazy loading check ---");
        System.out.println(reloaded.getName() + " has " + reloaded.getBooks().size()
                + " book(s), fetched lazily on first access.");

        em.close();

        printSchema();

        emf.close();
    }

    private static void printSchema() throws Exception {
        System.out.println("\n--- Generated tables ---");
        try (Connection conn = java.sql.DriverManager.getConnection(
                "jdbc:h2:mem:librarydb;DB_CLOSE_DELAY=-1", "sa", "")) {

            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet tables = meta.getTables(null, "PUBLIC", "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    System.out.println("\nTABLE " + tableName);
                    try (ResultSet cols = meta.getColumns(null, "PUBLIC", tableName, "%")) {
                        while (cols.next()) {
                            System.out.println("  - " + cols.getString("COLUMN_NAME") + " : " + cols.getString("TYPE_NAME"));
                        }
                    }
                    try (ResultSet fks = meta.getImportedKeys(null, "PUBLIC", tableName)) {
                        while (fks.next()) {
                            System.out.println("  FK " + fks.getString("FKCOLUMN_NAME")
                                    + " -> " + fks.getString("PKTABLE_NAME") + "." + fks.getString("PKCOLUMN_NAME"));
                        }
                    }
                }
            }
        }
    }
}
