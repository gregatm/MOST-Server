package de.muenchen.jpa;

import de.muenchen.jpa.criteria.DefaultJpaExpressionFactory;
import de.muenchen.jpa.dao.Author;
import de.muenchen.jpa.dao.Book;
import de.muenchen.jpa.metamodel.DynamicMetamodel;
import de.muenchen.jpa.metamodel.MetaModelFactory;
import jakarta.persistence.Parameter;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PostgresParameterTest {

    private CriteriaBuilder createContext() {
        var metamodel = new DynamicMetamodel();
        var mmfactory = new MetaModelFactory();
        metamodel.addType(mmfactory.processClass(Author.class));
        metamodel.addType(mmfactory.processClass(Book.class));
        return new DefaultJpaExpressionFactory(metamodel);
    }

    @Test
    public void simpleParameterized() {
        var factory = createContext();

        var q = factory.createQuery(Author.class);
        var r = q.from(Author.class);
        var p = factory.parameter(String.class);
        q.where(factory.equal(r.get("name"), p));

        var builder = new StringBuilder();
        var parameters = new ArrayList<Parameter<?>>();
        JpaSqlRequestBuilder.build(builder, q, parameters);

        var sql = builder.toString();
        assertEquals("SELECT * FROM \"Author\" \"a\" WHERE \"a\".\"name\" = $1;", sql);
        assertEquals(1, parameters.size());
        assertEquals(List.of(p), parameters);
    }

    @Test
    public void duplicateParameter() {
        var factory = createContext();

        var q = factory.createQuery(Author.class);
        var r = q.from(Author.class);
        var p = factory.parameter(String.class);
        q.where(
                factory.or(
                        factory.equal(r.get("name"), p),
                        factory.equal(r.get("id"), p)
                )
        );

        var builder = new StringBuilder();
        var parameters = new ArrayList<Parameter<?>>();
        JpaSqlRequestBuilder.build(builder, q, parameters);

        var sql = builder.toString();
        assertEquals("SELECT * FROM \"Author\" \"a\" WHERE \"a\".\"name\" = $1 OR \"a\".\"id\" = $1;", sql);
        assertEquals(1, parameters.size());
        assertEquals(List.of(p), parameters);
    }

    @Test
    public void multipleParameter() {
        var factory = createContext();

        var q = factory.createQuery(Author.class);
        var r = q.from(Author.class);
        var p1 = factory.parameter(String.class);
        var p2 = factory.parameter(UUID.class);
        q.where(
                factory.or(
                        factory.equal(r.get("name"), p1),
                        factory.equal(r.get("id"), p2)
                )
        );

        var builder = new StringBuilder();
        var parameters = new ArrayList<Parameter<?>>();
        JpaSqlRequestBuilder.build(builder, q, parameters);

        var sql = builder.toString();
        assertEquals("SELECT * FROM \"Author\" \"a\" WHERE \"a\".\"name\" = $1 OR \"a\".\"id\" = $2;", sql);
        assertEquals(2, parameters.size());
        assertEquals(List.of(p1, p2), parameters);
    }

    @Test
    public void selectParamter() {
        var factory = createContext();

        var q = factory.createQuery(Author.class);
        var r = q.from(Author.class);
        var p = factory.parameter(Author.class);

        q.select(p);

        var builder = new StringBuilder();
        var parameters = new ArrayList<Parameter<?>>();
        assertThrows(IllegalArgumentException.class, () -> JpaSqlRequestBuilder.build(builder, q, parameters));

    }
}
