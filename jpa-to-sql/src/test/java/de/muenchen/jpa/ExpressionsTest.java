package de.muenchen.jpa;

import de.muenchen.jpa.criteria.AliasContext;
import de.muenchen.jpa.dao.Author;
import jakarta.persistence.Parameter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static de.muenchen.jpa.CommonTestUtils.createContext;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpressionsTest {

    @Test
    @Disabled
    public void isNullTest() {

    }

    @Test
    @Disabled
    public void arrayAggTest() {

    }

    @Test
    public void existsTest() {
        var factory = createContext();
        var query = factory.createQuery();
        var sq  = query.subquery(Author.class);
        var author = sq.from(Author.class);
        sq.where(factory.equal(author.get("name"), "Charles Dickens"));
        var exp = factory.exists(sq);

        var sb = new StringBuilder();
        var alias = new AliasContext();
        var params = new ArrayList<Parameter<?>>();
        JpaSqlRequestBuilder.build(sb, alias, params, exp);

        var sql = sb.toString();
        assertEquals("EXISTS(SELECT * FROM \"Author\" \"a\" WHERE \"a\".\"name\" = 'Charles Dickens')", sql);
    }
}
