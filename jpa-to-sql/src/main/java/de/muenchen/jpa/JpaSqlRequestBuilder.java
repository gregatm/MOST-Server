package de.muenchen.jpa;

import de.muenchen.jpa.criteria.*;
import de.muenchen.jpa.metamodel.SqlIdentifier;
import jakarta.persistence.Parameter;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class JpaSqlRequestBuilder {

    public static <X> void build(CriteriaQuery<X> query) {
        build(new StringBuilder(), query, new ArrayList<>());
    }

    public static <X> void build(StringBuilder sb, CriteriaQuery<X> query, List<Parameter<?>> params) {
        AliasContext context = new AliasContext();

        sb.append(" FROM ");
        var roots = query.getRoots();
        if (roots == null) {
            throw new IllegalArgumentException();
        }
        var root = roots.stream().findFirst().orElseThrow();

        build(sb, context, params, root);

        var where = query.getRestriction();
        if (where != null) {
            sb.append(" WHERE ");
            build(sb, context, params, where);
        }

        var groupBys = query.getGroupList();
        if (groupBys != null && !groupBys.isEmpty()) {
            sb.append(" GROUP BY ");
            var groupBy = groupBys.getFirst();
            build(sb, context, params, groupBy);
            groupBys.stream().skip(1).forEach(g -> {
                sb.append(',');
                build(sb, context, params, g);
            });

        }

        var having = query.getGroupRestriction();
        if (having != null) {
            sb.append(" HAVING ");
            build(sb, context, params, having);
        }

        var orders = query.getOrderList();
        if (orders != null && !orders.isEmpty()) {
            sb.append(" ORDER BY ");
            var order = orders.getFirst();
            build(sb, context, params, order);
            orders.stream().skip(1).forEach(o -> {
                sb.append(',');
                build(sb, context, params, o);
            });
        }

        if (query instanceof JpaCriteriaQuery<?> q) {
            var limit = q.getLimit();
            if (limit != null) {
                sb.append(" LIMIT ");
                sb.append(limit);
            }
            var offset = q.getOffset();
            if (offset != null) {
                sb.append(" OFFSET ");
                sb.append(offset);
            }
        }

        // Build from clause at the end, so aliases in the selects are already properly set
        var selectBuilder = new StringBuilder();
        var selectParams = new ArrayList<Parameter<?>>();
        selectBuilder.append("SELECT ");

        if (query.isDistinct()) {
            selectBuilder.append("DISTINCT ");
        }

        var select = query.getSelection();
        if (select == null) {
            selectBuilder.append("*");
        } else {
            build(selectBuilder, context, selectParams, select);
        }

        if (!selectParams.isEmpty()) {
            throw new IllegalArgumentException("Parameters in select clause not yet supported");
        }

        sb.insert(0, selectBuilder);

        sb.append(';');
    }

    public static void build(StringBuilder sb, AliasContext context, List<Parameter<?>> params, Path<?> path) {
        var stack = new Stack<Path<?>>();
        var p = path;
        while (p != null) {
            stack.push(p);
            if (p instanceof Join<?,?>) {
                break;
            }
            p = p.getParentPath();
        }
        ExpressionUtils.addAliasIfExists(sb, context, stack.pop(), true);
        boolean isEmbedded = false;
        while (!stack.empty()) {
            var pa = stack.pop();
            var m = pa.getModel();
            if (m instanceof Attribute<?,?> a) {
                if (!isEmbedded) {
                    sb.append('"');
                }
                if (a.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
                    isEmbedded = true;
                    if (a instanceof SqlIdentifier s) {
                        sb.append(s.getSqlIdentifier());
                    } else {
                        sb.append(a.getName());
                    }
                    sb.append('_');
                } else {
                    if (a instanceof SqlIdentifier s) {
                        sb.append(s.getSqlIdentifier());
                    } else {
                        sb.append(a.getName());
                    }
                    sb.append('"');
                    if (!stack.empty()) {
                        sb.append('.');
                    }
                    isEmbedded = false;
                }
            } else {
                throw new IllegalStateException("Sub path with non attribute member");
            }
        }

    }

    public static void build(StringBuilder sb, AliasContext context, List<Parameter<?>> params, Selection<?> select) {
        if (select.isCompoundSelection()) {
            var first = select.getCompoundSelectionItems().getFirst();
            build(sb, context, params, first);
            select.getCompoundSelectionItems()
                    .stream()
                    .skip(1)
                    .forEach(i -> {
                        sb.append(", ");
                        build(sb, context, params, i);
                    });
        } else {
            if (select instanceof Expression<?> e) {
                build(sb, context, params, e);
            } else {
                ((JpaSql) select).toSql(sb, context, params);
            }
            ExpressionUtils.appendAliasIfExists(sb, context, select, false);
        }
    }

    public static void build(StringBuilder sb, AliasContext context, List<Parameter<?>> params, Order order) {
        build(sb, context, params, order.getExpression());
        if (order.isAscending()) {
            sb.append(" ASC");
        } else {
            sb.append(" DESC");
        }
        switch (order.getNullPrecedence()) {
            case FIRST -> sb.append(" NULLS FIRST");
            case LAST -> sb.append(" NULLS LAST");
        }
    }

    public static void build(StringBuilder sb, AliasContext context, List<Parameter<?>> params, Root<?> root) {
        sb.append('"');
        sb.append(root.getModel().getName());
        sb.append('"');
        sb.append(" ");
        if (root.getAlias() == null || root.getAlias().isBlank()) {
            root.alias(context.getAlias(root));
        }

        ExpressionUtils.addAliasIfExists(sb, context, root, false);

        var joins = root.getJoins();
        if (joins != null) {
            joins.forEach(join -> {
                switch (join.getJoinType()) {
                    case LEFT -> sb.append("LEFT");
                    case RIGHT -> sb.append("RIGHT");
                }
                sb.append(" JOIN ");
                if (join.getAlias() == null || join.getAlias().isBlank()) {
                    join.alias(context.getAlias(join));
                }
                build(sb, context, params, join);
                sb.append(" ON ");
                build(sb, context, params, join.getOn());
            });
        }

    }

    public static void build(StringBuilder sb, AliasContext context, List<Parameter<?>> params, Join<?, ?> join) {
        sb.append('"');
        sb.append(((EntityType<?>)join.getModel()).getName());
        sb.append('"');

        ExpressionUtils.appendAliasIfExists(sb, context, join, false);

        sb.append(' ');
    }

    public static void build(StringBuilder sb, AliasContext context, List<Parameter<?>> params, Predicate predicate) {
        var expressions = predicate.getExpressions();
        if (expressions == null || expressions.isEmpty()) {
            return;
        }

        // TODO handle is negated

        build(sb, context, params, expressions.getFirst());
        expressions.stream().skip(1)
                .forEach(e -> {
                    sb.append(' ');
                    sb.append(predicate.getOperator().name());
                    sb.append(' ');
                    build(sb, context, params, e);
                });
    }

    public static void build(StringBuilder sb, AliasContext context, List<Parameter<?>> params, Expression<?> expression) {
        switch (expression) {
            case Parameter<?> p -> build(sb, context, params, p);
            case CriteriaBuilder.In<?> p -> build(sb, context, params, p);
            case Predicate p -> build(sb, context, params, p);
            case Path<?> p -> build(sb, context, params, p);
            case JpaSql exp -> exp.toSql(sb, context, params);
            default -> log.debug("Ignored unknown expression type {}", expression.getClass());
        }
    }

    public static void build(StringBuilder sb, AliasContext context, List<Parameter<?>> params, CriteriaBuilder.In<?> in) {
        build(sb, context, params, in.getExpression());
        sb.append(" IN (");
        build(sb, context, params, in.getCompoundSelectionItems().stream().map(Expression.class::cast).toList(), JpaSqlRequestBuilder::build);
        sb.append(")");
    }

    public static <T> void build(StringBuilder sb, AliasContext context, List<Parameter<?>> params, List<T> list, SqlExpressionBuilder<T> builder) {
        if (list.isEmpty()) {
            return;
        }
        var first = list.getFirst();
        builder.build(sb, context, params, first);
        list.stream().skip(1)
                .forEach(e -> {
                    sb.append(',');
                    builder.build(sb, context, params, e);
                });

    }

    public static <T> void build(StringBuilder sb, AliasContext context, List<Parameter<?>> params, Parameter<?> param) {
        var idx = params.indexOf(param) + 1;
        if (idx == 0) {
            params.add(param);
            idx = params.size();
        }
        sb.append('$');
        sb.append(idx);
    }

    @FunctionalInterface
    public interface SqlExpressionBuilder<T> {
        void build(StringBuilder sb, AliasContext context, List<Parameter<?>> params, T arg);
    }
}
