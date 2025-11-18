package de.muenchen.jpa;

import de.muenchen.jpa.criteria.DefaultJpaExpressionFactory;
import de.muenchen.jpa.dao.*;
import de.muenchen.jpa.metamodel.DynamicMetamodel;
import de.muenchen.jpa.metamodel.MetaModelFactory;
import jakarta.persistence.Parameter;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JpaUpdateTest {
    private CriteriaBuilder createContext() {
        var metamodel = new DynamicMetamodel();
        var mmfactory = new MetaModelFactory();
        metamodel.addType(mmfactory.processClass(Author.class));
        metamodel.addType(mmfactory.processClass(Book.class));
        metamodel.addType(mmfactory.processClass(BookAttributes.class));
        metamodel.addType(mmfactory.processClass(Order.class));
        metamodel.addType(mmfactory.processClass(Delivery.class));
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
     * Update an entity.
     */
    @Test
    public void simpleUpdate() {
        var author = buildAuthor();

        var factory = createContext();
        var query = factory.createCriteriaUpdate(Author.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        JpaSqlUpdateBuilder.build(builder, query, params, author);
        var sql = builder.toString();

        assertEquals("UPDATE Author SET name = $1 WHERE id = $2", sql);
    }

    /**
     * Update an entity which has an embedded attribute.
     */
    @Test
    public void insertWithEmbeddable() {
        var book = buildBook();
        book.setAuthor(null);

        var factory = createContext();
        var query = factory.createCriteriaUpdate(Book.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        JpaSqlUpdateBuilder.build(builder, query, params, book);
        var sql = builder.toString();

        assertEquals("UPDATE Book SET name = $1, attributes_category = $2, attributes_yearOfPublishing = $3 WHERE id = $4;", sql);

    }

    /**
     * Update an entity with a relation to an existing entity.
     */
    @Test
    public void updateWithEntity() {
        var book = buildBook();

        var factory = createContext();
        var query = factory.createCriteriaUpdate(Book.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        JpaSqlUpdateBuilder.build(builder, query, params, book);
        var sql = builder.toString();

        assertEquals("INSERT INTO Book(name, attributes_category, attributes_yearOfPublishing, author_id) VALUES($1, $2, $3, $4);", sql);
    }

    /*
     * Update an entity which is related to another entity which is
     * yet inserted. Maybe a statement should be created which
     * inserts the second entity. Disabled for now.
     */
    @Test
    @Disabled
    public void insertTree() {
        var book = buildBook();
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
     * Update an entity with a version field. The new version
     * value has to be inserted. Version field is a timestamp
     * so DBMS internal timestamp function is used.
     */
    @Test
    public void updateWithVersion() {
        var order = new Order(
                null,
                buildBook(),
                LocalDateTime.of(200, 12, 12, 12, 24, 12, 100)
        );
        var factory = createContext();
        var query = factory.createCriteriaUpdate(Order.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        JpaSqlUpdateBuilder.build(builder, query, params, order);
        var sql = builder.toString();

        assertEquals("UPDATE Order SET book = $1, version = NOW() WHERE id = $3;", sql);
    }

    /**
     * Update an entity with a version field. The new version
     * value has to be inserted. Version field is a timestamp
     * so DBMS internal timestamp function is used.
     */
    @Test
    public void updateWithVersionInteger() {
        var delivery = new Delivery(
                null,
                buildBook(),
                Byte.MAX_VALUE
        );
        var factory = createContext();
        var query = factory.createCriteriaUpdate(Delivery.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        JpaSqlUpdateBuilder.build(builder, query, params, delivery);
        var sql = builder.toString();

        assertEquals("UPDATE Order SET book = $1, version = 0 WHERE id = $3;", sql);
    }
}
