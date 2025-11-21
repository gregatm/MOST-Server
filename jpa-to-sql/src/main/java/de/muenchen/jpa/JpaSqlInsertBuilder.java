package de.muenchen.jpa;

import de.muenchen.jpa.criteria.AliasContext;
import de.muenchen.jpa.criteria.JpaCriteriaUpdate;
import jakarta.persistence.Parameter;
import jakarta.persistence.criteria.CriteriaUpdate;

import java.util.List;

public class JpaSqlInsertBuilder {
    public static <X> void build(CriteriaUpdate<X> query) {

    }

    public static <X> void build(StringBuilder sb, CriteriaUpdate<X> query, List<Parameter<?>> params) {
        var context = new AliasContext();
        sb.append("INSERT INTO ");
        var root = query.getRoot();
        if (root == null) {
            throw new IllegalStateException();
        }
        sb.append('"');
        sb.append(root.getModel().getName());
        sb.append('"');

        sb.append('(');
        var q = (JpaCriteriaUpdate<X>) query;
        JpaSqlRequestBuilder.build(sb, context, params, q.getSets(), (sb1, context1, params1, e) -> {
            JpaSqlRequestBuilder.build(sb1, context1, params1, e.getKey());
        });
        sb.append(") VALUES(");

        JpaSqlRequestBuilder.build(sb, context, params, q.getSets(), (sb1, context1, params1, e) -> {
            JpaSqlRequestBuilder.build(sb1, context1, params1, e.getValue());
        });
        sb.append(')');

        var returning = q.getSelection();
        if (returning != null) {
            sb.append(" RETURNING ");
            JpaSqlRequestBuilder.build(sb, context, params, returning);
        }

        sb.append(';');

    }
}
