package de.muenchen.jpa;

import de.muenchen.jpa.criteria.AbstractJpaExpressionFactory;
import de.muenchen.jpa.criteria.AggregateSelections;
import de.muenchen.jpa.criteria.DefaultJpaExpressionFactory;
import de.muenchen.jpa.dao.Author;
import de.muenchen.jpa.dao.Book;
import de.muenchen.jpa.dao.BookAttributes;
import de.muenchen.jpa.metamodel.DynamicMetamodel;
import de.muenchen.jpa.metamodel.MetaModelFactory;
import jakarta.persistence.Parameter;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Nulls;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JpaSelectTest {

    @Test
    public void buildJpaContext() {
        createContext();
    }

    private CriteriaBuilder createContext() {
        var metamodel = new DynamicMetamodel();
        var mmfactory = new MetaModelFactory();
        metamodel.addType(mmfactory.processClass(Author.class));
        metamodel.addType(mmfactory.processClass(Book.class));
        metamodel.addType(mmfactory.processClass(BookAttributes.class));
        return new DefaultJpaExpressionFactory(metamodel);
    }

    private String buildSql(CriteriaQuery<?> q) {
        var builder = new StringBuilder();
        JpaSqlRequestBuilder.build(builder, q, new ArrayList<>());
        return builder.toString();
    }

    @Test
    public void simpleQuery() {

        var factory = createContext();

        var q = factory.createQuery();
        var r = q.from(Author.class);

        var sql = buildSql(q);

        assertEquals("SELECT * FROM \"Author\" \"a\";", sql);
    }

    @Test
    public void simpleWhere() {
        var factory = createContext();

        var q = factory.createQuery(Author.class);
        var r = q.from(Author.class);
        q.where(factory.equal(r.get("name"), "Charles Dickens"));

        var sql = buildSql(q);

        assertEquals("SELECT * FROM \"Author\" \"a\" WHERE \"a\".\"name\" = 'Charles Dickens';", sql);
    }

    @Test
    public void simpleOrder() {
        var factory = createContext();

        var q = factory.createQuery(Author.class);
        var r = q.from(Author.class);
        q.orderBy(List.of(factory.asc(r.get("name"))));

        var sql = buildSql(q);

        assertEquals("SELECT * FROM \"Author\" \"a\" ORDER BY \"a\".\"name\" ASC;", sql);

        q.orderBy(List.of(factory.desc(r.get("name"))));

        sql = buildSql(q);

        assertEquals("SELECT * FROM \"Author\" \"a\" ORDER BY \"a\".\"name\" DESC;", sql);
    }

    @Test
    public void orderWithNull() {
        var factory = createContext();

        var q = factory.createQuery(Author.class);
        var r = q.from(Author.class);
        q.orderBy(List.of(factory.asc(r.get("name"), Nulls.LAST)));

        var sql = buildSql(q);

        assertEquals("SELECT * FROM \"Author\" \"a\" ORDER BY \"a\".\"name\" ASC NULLS LAST;", sql);

        q.orderBy(List.of(factory.desc(r.get("name"), Nulls.FIRST)));

        sql = buildSql(q);

        assertEquals("SELECT * FROM \"Author\" \"a\" ORDER BY \"a\".\"name\" DESC NULLS FIRST;", sql);
    }

    @Test
    public void orderMultiple() {
        var factory = createContext();

        var q = factory.createQuery(Author.class);
        var r = q.from(Author.class);
        q.orderBy(List.of(factory.asc(r.get("name")), factory.asc(r.get("id"))));

        var sql = buildSql(q);

        assertEquals("SELECT * FROM \"Author\" \"a\" ORDER BY \"a\".\"name\" ASC,\"a\".\"id\" ASC;", sql);

        q.orderBy(List.of(factory.desc(r.get("name")), factory.asc(r.get("id"))));

        sql = buildSql(q);

        assertEquals("SELECT * FROM \"Author\" \"a\" ORDER BY \"a\".\"name\" DESC,\"a\".\"id\" ASC;", sql);
    }

    @Test
    public void orderMultipleWithNulls() {
        var factory = createContext();

        var q = factory.createQuery(Author.class);
        var r = q.from(Author.class);
        q.orderBy(List.of(factory.asc(r.get("name"), Nulls.FIRST), factory.asc(r.get("id"), Nulls.FIRST)));

        var sql = buildSql(q);

        assertEquals("SELECT * FROM \"Author\" \"a\" ORDER BY \"a\".\"name\" ASC NULLS FIRST,\"a\".\"id\" ASC NULLS FIRST;", sql);

        q.orderBy(List.of(factory.asc(r.get("name"), Nulls.LAST), factory.desc(r.get("id"), Nulls.FIRST)));

        sql = buildSql(q);

        assertEquals("SELECT * FROM \"Author\" \"a\" ORDER BY \"a\".\"name\" ASC NULLS LAST,\"a\".\"id\" DESC NULLS FIRST;", sql);
    }

    @Test
    public void simpleSelect() {
        var factory = createContext();

        var q = factory.createQuery(Author.class);
        var r = q.from(Author.class);
        q.select(r.get("name"));

        var sql = buildSql(q);

        assertEquals("SELECT \"a\".\"name\" FROM \"Author\" \"a\";", sql);
    }

    @Test
    public void distinctSelect() {
        var factory = createContext();

        var q = factory.createQuery(Author.class);
        var r = q.from(Author.class);
        q.select(r.get("name"));
        q.distinct(true);

        var sql = buildSql(q);

        assertEquals("SELECT DISTINCT \"a\".\"name\" FROM \"Author\" \"a\";", sql);
    }

    @Test
    public void selectWithAlias() {
        var factory = createContext();

        var q = factory.createQuery(Author.class);
        var r = q.from(Author.class);
        q.select(
                factory.construct(
                        Author.class,
                        r.get("name").alias("n")
                )
        );

        var sql = buildSql(q);

        assertEquals("SELECT \"a\".\"name\" \"n\" FROM \"Author\" \"a\";", sql);
    }

    @Test
    public void simpleJoin() {
        var factory = createContext();

        var q = factory.createQuery();
        var r = q.from(Author.class);
        var j = r.join(Book.class);

        var sql = buildSql(q);

        assertEquals("SELECT * FROM \"Author\" \"a\" JOIN \"Book\" \"b\"  ON \"a\".\"id\" = \"b\".\"author_id\";", sql);
    }

    @Test
    public void joinWithWhere() {
        var factory = createContext();

        var q = factory.createQuery();
        var r = q.from(Author.class);
        var j = r.join(Book.class);
        q.where(factory.equal(j.get("name"), "Oliver Twist"));

        var sql = buildSql(q);

        assertEquals("SELECT * FROM \"Author\" \"a\" JOIN \"Book\" \"b\"  ON \"a\".\"id\" = \"b\".\"author_id\" WHERE \"b\".\"name\" = 'Oliver Twist';", sql);
    }

    @Test
    public void simpleJoinWithWhere() {
        var factory = createContext();

        var q = factory.createQuery();
        var r = q.from(Author.class);
        var j = r.join(Book.class);

        q.where(factory.equal(j.get("name"), r.get("name")));

        var sql = buildSql(q);

        assertEquals("SELECT * FROM \"Author\" \"a\" JOIN \"Book\" \"b\"  ON \"a\".\"id\" = \"b\".\"author_id\" WHERE \"b\".\"name\" = \"a\".\"name\";", sql);
    }

    @Test
    public void simpleGroupBy() {
        var factory = createContext();

        var q = factory.createQuery();
        var r = q.from(Author.class);
        q.groupBy(r.get("name"));

        var sql = buildSql(q);
        assertEquals("SELECT * FROM \"Author\" \"a\" GROUP BY \"a\".\"name\";", sql);
    }

    @Test
    public void simpleHaving() {
        var factory = createContext();

        var q = factory.createQuery();
        var r = q.from(Author.class);
        q.having(factory.equal(r.get("name"), "Charles Dickens"));

        var sql = buildSql(q);
        assertEquals("SELECT * FROM \"Author\" \"a\" HAVING \"a\".\"name\" = 'Charles Dickens';", sql);
    }

    @Test
    public void multiselect() {
        var factory = createContext();

        var q = factory.createQuery(Author.class);
        var r = q.from(Author.class);
        q.select(
                factory.construct(
                        Author.class,
                        r.get("name"),
                        r.get("id")
                )
        );

        var sql = buildSql(q);

        assertEquals("SELECT \"a\".\"name\", \"a\".\"id\" FROM \"Author\" \"a\";", sql);
    }

    @Test
    public void arrayAggregate() {
        var factory = createContext();

        var q = factory.createQuery();
        var r = q.from(Author.class);
        var j = r.join(Book.class);
        q.groupBy(r.get("id"), r.get("name"));
        q.select(factory.construct(
                Author.class,
                new AggregateSelections.ArrayAggregate<>(Author.class, j.get("name")).alias("book_names")
        ));

        var sql = buildSql(q);

        assertEquals("SELECT array_agg(\"b\".\"name\") \"book_names\" FROM \"Author\" \"a\" JOIN \"Book\" \"b\"  ON \"a\".\"id\" = \"b\".\"author_id\" GROUP BY \"a\".\"id\",\"a\".\"name\";", sql);
    }

    @Test
    public void arrayRemove() {
        var factory = createContext();

        var q = factory.createQuery();
        var r = q.from(Author.class);
        var j = r.join(Book.class);
        q.groupBy(r.get("id"), r.get("name"));
        q.select(factory.construct(
                Author.class,
                new AggregateSelections.ArrayRemove<Object>(Object.class, new AggregateSelections.ArrayAggregate<>(Author.class, j.get("name")), factory.nullLiteral(Object.class)).alias("book_names")
        ));

        var sql = buildSql(q);

        assertEquals("SELECT array_remove(array_agg(\"b\".\"name\"), null) \"book_names\" FROM \"Author\" \"a\" JOIN \"Book\" \"b\"  ON \"a\".\"id\" = \"b\".\"author_id\" GROUP BY \"a\".\"id\",\"a\".\"name\";", sql);
    }

    @Test
    public void selectEmbeddedAttribute() {
        var factory = createContext();

        var q = factory.createQuery(Book.class);
        var r = q.from(Book.class);
        q.where(factory.equal(r.get("attributes").get("yearOfPublishing"), 1968));

        var sql = buildSql(q);

        assertEquals("SELECT * FROM \"Book\" \"a\" WHERE \"a\".\"attributes_yearOfPublishing\" = 1968;", sql);
    }

    @Test
    @Disabled
    public void selectEmbeddedEntityAtribute() {
        var factory = createContext();

        var q = factory.createQuery(Book.class);
        var r = q.from(Book.class);
        q.where(factory.equal(r.get("author").get("name"), "Charles Dickens"));

        var sql = buildSql(q);

        assertEquals(1, sql);
    }

}
