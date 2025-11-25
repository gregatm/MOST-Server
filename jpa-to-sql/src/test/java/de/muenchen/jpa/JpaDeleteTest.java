package de.muenchen.jpa;

import de.muenchen.jpa.dao.*;
import jakarta.persistence.Parameter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static de.muenchen.jpa.CommonTestUtils.createContext;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JpaDeleteTest {

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
     * Delete an entity.
     */
    @Test
    public void simpleInsert() {
        var author = buildAuthor();

        var factory = createContext();
        var query = factory.createCriteriaDelete(Author.class);
        var r = query.from(Author.class);
        query.where(factory.equal(r.get("id"), factory.parameter(UUID.class)));

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        JpaSqlDeleteBuilder.build(builder, query, params, author);
        var sql = builder.toString();

        assertEquals("DELETE FROM \"Author\" \"a\" WHERE \"a\".\"id\" = $1;", sql);
    }

    /**
     * Delete list of entities
     */
    @Test
    public void deleteList() {
        var author = buildAuthor();
        author.setId(UUID.randomUUID());
        var list = List.of(author, buildAuthor());

        var factory = createContext();
        var query = factory.createCriteriaDelete(Author.class);
        var r = query.from(Author.class);
        query.where(factory.in(r.get("id")).value(factory.parameter(UUID.class)).value(factory.parameter(UUID.class)));

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        JpaSqlDeleteBuilder.build(builder, query, params, author);
        var sql = builder.toString();

        assertEquals("DELETE FROM \"Author\" \"a\" WHERE \"a\".\"id\" IN ($1,$2);", sql);
    }
}
