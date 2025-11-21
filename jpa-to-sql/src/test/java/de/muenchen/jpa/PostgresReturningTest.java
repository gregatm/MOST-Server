package de.muenchen.jpa;

import de.muenchen.jpa.criteria.DefaultJpaExpressionFactory;
import de.muenchen.jpa.criteria.JpaCriteriaUpdate;
import de.muenchen.jpa.dao.Author;
import de.muenchen.jpa.dao.Book;
import de.muenchen.jpa.dao.BookAttributes;
import de.muenchen.jpa.metamodel.DynamicMetamodel;
import de.muenchen.jpa.metamodel.MetaModelFactory;
import jakarta.persistence.Parameter;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PostgresReturningTest {
    private CriteriaBuilder createContext() {
        var metamodel = new DynamicMetamodel();
        var mmfactory = new MetaModelFactory();
        metamodel.addType(mmfactory.processClass(Author.class));
        metamodel.addType(mmfactory.processClass(Book.class));
        metamodel.addType(mmfactory.processClass(BookAttributes.class));
        return new DefaultJpaExpressionFactory(metamodel);
    }

    public Author buildAuthor() {
        return new Author(
                UUID.fromString("17417490-042a-4779-8b9b-fca3e9564156"),
                "Charles Dickens",
                null);
    }

    @Test
    public void insert() {
        var author = buildAuthor();
        author.setId(null);

        var factory = createContext();
        var query = factory.createCriteriaUpdate(Author.class);
        var root = query.from(Author.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        ((JpaCriteriaUpdate<Author>) query).select(root.get("id"));
        var p = JpaSqlUpdateBuilder.populateUpdateQuery(factory, query, author);
        JpaSqlInsertBuilder.build(builder, query, params);
        var sql = builder.toString();

        assertEquals("INSERT INTO \"Author\"(\"name\") VALUES($1) RETURNING \"id\";", sql);
    }

    @Test
    public void update() {
        var author = buildAuthor();

        var factory = createContext();
        var query = factory.createCriteriaUpdate(Author.class);
        var root = query.from(Author.class);

        var builder = new StringBuilder();
        List<Parameter<?>> params = new ArrayList<>();
        ((JpaCriteriaUpdate<Author>) query).select(factory.construct(Author.class, root.get("id"), root.get("name")));
        var p = JpaSqlUpdateBuilder.populateUpdateQuery(factory, query, author);
        JpaSqlUpdateBuilder.build(builder, query, params);
        var sql = builder.toString();

        assertEquals("UPDATE \"Author\" \"a\" SET \"name\" = $1 WHERE \"id\" = $2 RETURNING \"id\", \"name\";", sql);
    }

}
