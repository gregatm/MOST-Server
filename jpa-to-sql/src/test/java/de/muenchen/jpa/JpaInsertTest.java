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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JpaInsertTest {
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
     * Insert an entity.
     */
    @Test
    public void simpleInsert() {
        var author = buildAuthor();
        author.setId(null);

        var factory = createContext();
        var query = factory.createCriteriaUpdate(Author.class);
        query.from(Author.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        var p = JpaSqlUpdateBuilder.populateUpdateQuery(factory, query, author);
        JpaSqlInsertBuilder.build(builder, query, params);
        var sql = builder.toString();

        assertThat(List.of(sql.split("[, ()]")), containsInAnyOrder("INSERT INTO \"Author\"(\"name\") VALUES($1);".split("[, ()]")));
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
        query.from(Book.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        var p = JpaSqlUpdateBuilder.populateUpdateQuery(factory, query, book);
        JpaSqlInsertBuilder.build(builder, query, params);
        var sql = builder.toString();

        assertThat(List.of(sql.split("[, ()]")), containsInAnyOrder("INSERT INTO \"Book\"(\"name\",\"attributes_category\",\"attributes_yearOfPublishing\") VALUES($1,$2,$3);".split("[, ()]")));
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
        query.from(Book.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        var p = JpaSqlUpdateBuilder.populateUpdateQuery(factory, query, book);
        JpaSqlInsertBuilder.build(builder, query, params);
        var sql = builder.toString();

        assertThat(List.of(sql.split("[, ()]")), containsInAnyOrder("INSERT INTO \"Book\"(\"name\",\"attributes_category\",\"attributes_yearOfPublishing\",\"author_id\") VALUES($1,$2,$3,$4);".split("[, ()]")));
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
        query.from(Book.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        var p = JpaSqlUpdateBuilder.populateUpdateQuery(factory, query, book);
        JpaSqlInsertBuilder.build(builder, query, params);
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
        query.from(Order.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        var p = JpaSqlUpdateBuilder.populateUpdateQuery(factory, query, order);
        JpaSqlInsertBuilder.build(builder, query, params);
        var sql = builder.toString();

        assertThat(List.of(sql.split("[, ()]")), containsInAnyOrder("INSERT INTO \"Order\"(\"book\",\"version\") VALUES($1,$2);".split("[, ()]")));
    }

    /**
     * Insert an entity with a version field. The version
     * field has to be set. Version field is a short.
     */
    @Test
    public void insertWithVersionInteger() {
        var delivery = new Delivery(
                null,
                buildBook(),
                null
        );
        var factory = createContext();
        var query = factory.createCriteriaUpdate(Delivery.class);
        query.from(Delivery.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        var p = JpaSqlUpdateBuilder.populateUpdateQuery(factory, query, delivery);
        JpaSqlInsertBuilder.build(builder, query, params);
        var sql = builder.toString();


        assertThat(List.of(sql.split("[, ()]")), containsInAnyOrder("INSERT INTO \"Delivery\"(\"book\",\"version\") VALUES($1,$2);".split("[, ()]")));
    }
}
