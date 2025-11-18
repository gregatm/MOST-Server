package de.muenchen.jpa.criteria;

import jakarta.persistence.criteria.Selection;

public class ExpressionUtils {

    public static void addAliasIfExists(StringBuilder sb, AliasContext context, Selection<?> select, boolean inPath) {
        if (select.getAlias() == null) {
            return;
        }

        sb.append('"');
        sb.append(select.getAlias());
        sb.append('"');

        if(inPath) {
            sb.append('.');
        }
    }

    public static void appendAliasIfExists(StringBuilder sb, AliasContext context, Selection<?> select, boolean inPath) {
        if (select.getAlias() == null) {
            return;
        }
        sb.append(' ');
        addAliasIfExists(sb, context, select, inPath);
    }
}
