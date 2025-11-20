package de.muenchen.jpa;

import de.muenchen.jpa.criteria.AliasContext;
import jakarta.persistence.Parameter;
import jakarta.persistence.criteria.CriteriaDelete;

import java.util.List;

public class JpaSqlDeleteBuilder {

    public static <X> void build(CriteriaDelete<X> query, X argument) {

    }

    public static <X> void build(StringBuilder sb, CriteriaDelete<X> query, List<Parameter<?>> params, X arg) {
        var context = new AliasContext();
        sb.append("DELETE FROM ");
        var root = query.getRoot();
        if (root == null) {
            throw new IllegalArgumentException();
        }
         JpaSqlRequestBuilder.build(sb, context, params, root);

        var where = query.getRestriction();
        if (where != null) {
            sb.append(" WHERE ");
            JpaSqlRequestBuilder.build(sb, context, params, where);
        }

        sb.append(';');
    }
}
