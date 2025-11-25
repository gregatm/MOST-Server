package de.muenchen.jpa;

import de.muenchen.jpa.dao.*;
import jakarta.persistence.criteria.CriteriaQuery;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static de.muenchen.jpa.CommonTestUtils.createContext;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubqueriesTest {

    private String buildSql(CriteriaQuery<?> q) {
        var builder = new StringBuilder();
        JpaSqlRequestBuilder.build(builder, q, new ArrayList<>());
        return builder.toString();
    }

    @Test
    public void simpleSubquery() {
        var factory = createContext();

        var q = factory.createQuery();
        q.from(Author.class);

        var subquery = q.subquery(Book.class);
        var sr = subquery.from(Book.class);
        subquery.where(factory.equal(sr.get("author"), 1));

        q.where(factory.exists(subquery));
        var sql = buildSql(q);

        assertEquals("SELECT * FROM \"Author\" \"a\" WHERE EXISTS(SELECT * FROM \"Book\" \"b\" WHERE \"b\".\"author_id\" = 1);", sql);
    }
}
