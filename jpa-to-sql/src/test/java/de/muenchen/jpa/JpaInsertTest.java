package de.muenchen.jpa;

import de.muenchen.jpa.criteria.DefaultJpaExpressionFactory;
import de.muenchen.jpa.dao.*;
import de.muenchen.jpa.metamodel.DynamicMetamodel;
import de.muenchen.jpa.metamodel.MetaModelFactory;
import jakarta.persistence.Parameter;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JpaInsertTest {
    private CriteriaBuilder createContext() {
        var metamodel = new DynamicMetamodel();
        var mmfactory = new MetaModelFactory();
        metamodel.addType(mmfactory.processClass(Author.class));
        metamodel.addType(mmfactory.processClass(Book.class));
        metamodel.addType(mmfactory.processClass(BookAttributes.class));
        metamodel.addType(mmfactory.processClass(Order.class));
        return new DefaultJpaExpressionFactory(metamodel);
    }

    public Author buildAuthor() {
        return new Author(
                UUID.fromString("17417490-042a-4779-8b9b-fca3e9564156"),
                "Charles Dickens",
                null);
    }

    public Book buildBook() {
        var author = buildAuthor();
        var attributes = new BookAttributes();
        attributes.setYearOfPublishing(1837);
        attributes.setCategory(BookCategory.NOVEL);
        var book = new Book(UUID.fromString(
                "17417490-042a-4779-8b9b-fca3e9564156"),
                "Oliver Twist",
                author,
                attributes);
        return book;
    }

    /**
     * Insert an entity.
     */
    @Test
    public void simpleInsert() {
        var author = buildAuthor();
        author.setId(null);

        var factory = createContext();
        var query = factory.createCriteriaUpdate(Author.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        JpaSqlUpdateBuilder.build(builder, query, params, author);
        var sql = builder.toString();

        assertEquals("INSERT INTO Author(name) VALUES($1)", sql);
    }

    /**
     * Insert an entity which has an embedded attribute.
     */
    @Test
    public void insertWithEmbeddable() {
        var book = buildBook();
        book.setId(null);
        book.setAuthor(null);

        var factory = createContext();
        var query = factory.createCriteriaUpdate(Book.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        JpaSqlUpdateBuilder.build(builder, query, params, book);
        var sql = builder.toString();

        assertEquals("INSERT INTO Book(name, attributes_category, attributes_yearOfPublishing) VALUES($1, $2, $3);", sql);
    }

    /**
     * Insert an entity with a relation to an existing entity.
     */
    @Test
    public void insertWithEntity() {
        var book = buildBook();
        book.setId(null);

        var factory = createContext();
        var query = factory.createCriteriaUpdate(Book.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        JpaSqlUpdateBuilder.build(builder, query, params, book);
        var sql = builder.toString();

        assertEquals("INSERT INTO Book(name, attributes_category, attributes_yearOfPublishing, author_id) VALUES($1, $2, $3, $4);", sql);
    }

    /*
     * Insert an entity which is related to another entity which is
     * also not yet inserted. Maybe a statement should be created which
     * inserts both entities. Disabled for now.
     */
    @Test
    @Disabled
    public void insertTree() {
        var book = buildBook();
        book.setId(null);
        book.getAuthor().setId(null);

        var factory = createContext();
        var query = factory.createCriteriaUpdate(Book.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        JpaSqlUpdateBuilder.build(builder, query, params, book);
        var sql = builder.toString();

        assertEquals("", sql);
    }

    /**
     * Inserts an entity with a version field. The version
     * field is populated automatically with a default value
     * by the dbms. Therefore, the statement does not insert
     * an own value.
     */
    @Test
    public void insertWithVersion() {
        var order = new Order(
                null,
                buildBook(),
                null
        );
        var factory = createContext();
        var query = factory.createCriteriaUpdate(Order.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        JpaSqlUpdateBuilder.build(builder, query, params, order);
        var sql = builder.toString();

        assertEquals("INSERT INTO Order(book) VALUES($1)", sql);
    }
}
