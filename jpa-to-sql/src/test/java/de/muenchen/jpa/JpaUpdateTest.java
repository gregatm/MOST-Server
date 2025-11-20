package de.muenchen.jpa;

import de.muenchen.jpa.criteria.DefaultJpaExpressionFactory;
import de.muenchen.jpa.dao.*;
import de.muenchen.jpa.metamodel.DynamicMetamodel;
import de.muenchen.jpa.metamodel.MetaModelFactory;
import jakarta.persistence.Parameter;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
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
        query.from(Author.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        JpaSqlUpdateBuilder.populateUpdateQuery(factory, query, author);
        JpaSqlUpdateBuilder.build(builder, query, params);
        var sql = builder.toString();

        assertEquals("UPDATE \"Author\" \"a\" SET \"a\".\"name\" = $1 WHERE \"a\".\"id\" = $2;", sql);
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
        query.from(Book.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        JpaSqlUpdateBuilder.populateUpdateQuery(factory, query, book);
        JpaSqlUpdateBuilder.build(builder, query, params);
        var sql = builder.toString();

        assertThat(List.of(sql.split("[, ]")), containsInAnyOrder("UPDATE \"Book\" \"a\" SET \"a\".\"name\" = $1,\"a\".\"attributes_category\" = $2,\"a\".\"attributes_yearOfPublishing\" = $3 WHERE \"a\".\"id\" = $4;".split("[, ]")));
    }

    /**
     * Update an entity with a relation to an existing entity.
     */
    @Test
    public void updateWithEntity() {
        var book = buildBook();

        var factory = createContext();
        var query = factory.createCriteriaUpdate(Book.class);
        query.from(Book.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        JpaSqlUpdateBuilder.populateUpdateQuery(factory, query, book);
        JpaSqlUpdateBuilder.build(builder, query, params);
        var sql = builder.toString();

        assertThat(List.of(sql.split("[, ]")), containsInAnyOrder("UPDATE \"Book\" \"a\" SET \"a\".\"name\" = $1,\"a\".\"attributes_category\" = $2,\"a\".\"attributes_yearOfPublishing\" = $3,\"a\".\"author_id\" = $4 WHERE \"a\".\"id\" = $5;".split("[, ]")));
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
        JpaSqlUpdateBuilder.populateUpdateQuery(factory, query, book);
        JpaSqlUpdateBuilder.build(builder, query, params);
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
        query.from(Order.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        JpaSqlUpdateBuilder.populateUpdateQuery(factory, query, order);
        JpaSqlUpdateBuilder.build(builder, query, params);
        var sql = builder.toString();

        assertEquals("UPDATE \"Order\" \"a\" SET \"a\".\"book\" = $1,\"a\".\"version\" = $2 WHERE \"a\".\"id\" = $3 AND \"a\".\"version\" = $4;", sql);
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
                Short.MAX_VALUE
        );
        var factory = createContext();
        var query = factory.createCriteriaUpdate(Delivery.class);
        query.from(Delivery.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        JpaSqlUpdateBuilder.populateUpdateQuery(factory, query, delivery);
        JpaSqlUpdateBuilder.build(builder, query, params);
        var sql = builder.toString();

        assertThat(List.of(sql.split("[, ]")), containsInAnyOrder("UPDATE \"Delivery\" \"a\" SET \"a\".\"book\" = $1,\"a\".\"version\" = $2 WHERE \"a\".\"version\" = $3 AND \"a\".\"id\" = $4;".split("[, ]")));
    }
}
