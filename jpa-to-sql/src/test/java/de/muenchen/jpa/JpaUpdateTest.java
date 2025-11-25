package de.muenchen.jpa;

import de.muenchen.jpa.dao.*;
import jakarta.persistence.Parameter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static de.muenchen.jpa.CommonTestUtils.createContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JpaUpdateTest {

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
        var p = JpaSqlUpdateBuilder.populateUpdateQuery(factory, query, author);
        JpaSqlUpdateBuilder.build(builder, query, params);
        var sql = builder.toString();

        assertEquals("UPDATE \"Author\" \"a\" SET \"name\" = $1 WHERE \"id\" = $2;", sql);
        assertThat(p.values(), containsInAnyOrder(author.getId(), author.getName()));
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
        var p = JpaSqlUpdateBuilder.populateUpdateQuery(factory, query, book);
        JpaSqlUpdateBuilder.build(builder, query, params);
        var sql = builder.toString();

        assertThat(List.of(sql.split("[, ]")), containsInAnyOrder("UPDATE \"Book\" \"a\" SET \"name\" = $1,\"attributes_category\" = $2,\"attributes_yearOfPublishing\" = $3 WHERE \"id\" = $4;".split("[, ]")));
        assertThat(p.values(), containsInAnyOrder(book.getId(), book.getName(), book.getAttributes().getCategory(), book.getAttributes().getYearOfPublishing()));
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
        var p = JpaSqlUpdateBuilder.populateUpdateQuery(factory, query, book);
        JpaSqlUpdateBuilder.build(builder, query, params);
        var sql = builder.toString();

        assertThat(List.of(sql.split("[, ]")), containsInAnyOrder("UPDATE \"Book\" \"a\" SET \"name\" = $1,\"attributes_category\" = $2,\"attributes_yearOfPublishing\" = $3,\"author_id\" = $4 WHERE \"id\" = $5;".split("[, ]")));
        assertThat(p.values(), containsInAnyOrder(book.getId(), book.getName(), book.getAuthor().getId(), book.getAttributes().getCategory(), book.getAttributes().getYearOfPublishing()));
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
        var p = JpaSqlUpdateBuilder.populateUpdateQuery(factory, query, book);
        JpaSqlUpdateBuilder.build(builder, query, params);
        var sql = builder.toString();

        assertEquals("", sql);
        assertThat(p.values(), containsInAnyOrder());
    }

    private MockedStatic<Clock> clockMock;

    /**
     * Update an entity with a version field. The new version
     * value has to be inserted. Version field is a timestamp.
     * New timestamp needs to be generated
     */
    @Test
    public void updateWithVersion() {
        Clock spyClock = Mockito.spy(Clock.systemDefaultZone());
        clockMock = Mockito.mockStatic(Clock.class);
        clockMock.when(Clock::systemDefaultZone).thenReturn(spyClock);
        Mockito.when(spyClock.instant()).thenReturn(Instant.ofEpochSecond(164000000));
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
        var p = JpaSqlUpdateBuilder.populateUpdateQuery(factory, query, order);
        JpaSqlUpdateBuilder.build(builder, query, params);
        var sql = builder.toString();

        assertThat(List.of(sql.split("[, ]")), containsInAnyOrder("UPDATE \"Order\" \"a\" SET \"book\" = $1,\"version\" = $2 WHERE \"id\" = $3 AND \"version\" = $4;".split("[, ]")));
        assertThat(p.values(), containsInAnyOrder(order.getId(), order.getBook().getId(), order.getVersion(), LocalDateTime.now()));
    }

    /**
     * Update an entity with a version field. The new version
     * value has to be inserted. Version field is a short.
     * New version has to be incremented
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
        var p = JpaSqlUpdateBuilder.populateUpdateQuery(factory, query, delivery);
        JpaSqlUpdateBuilder.build(builder, query, params);
        var sql = builder.toString();

        assertThat(List.of(sql.split("[, ]")), containsInAnyOrder("UPDATE \"Delivery\" \"a\" SET \"book\" = $1,\"version\" = $2 WHERE \"version\" = $3 AND \"id\" = $4;".split("[, ]")));
        assertThat(p.values(), containsInAnyOrder(delivery.getId(), delivery.getBook().getId(), delivery.getVersion(), (short)-32768));
    }
}
