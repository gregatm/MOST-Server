package de.muenchen.jpa;

import de.muenchen.jpa.criteria.*;
import de.muenchen.jpa.metamodel.SingularAttributeImpl;
import jakarta.persistence.Column;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Parameter;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Attr;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class JpaSqlUpdateBuilder {

    public static <X> void build(CriteriaUpdate<X> query) {

    }

    public static <X> void build(StringBuilder sb, CriteriaUpdate<X> query, List<Parameter<?>> params) {
        var context = new AliasContext();
        sb.append("UPDATE ");
        var root = query.getRoot();
        if (root == null) {
            throw new IllegalStateException();
        }
        JpaSqlRequestBuilder.build(sb, context, params, root);

        sb.append(" SET ");
        root.alias(null);
        var q = (JpaCriteriaUpdate<X>) query;
        JpaSqlRequestBuilder.build(sb, context, params, q.getSets(), (sb1, context1, params1, e) -> {
            JpaSqlRequestBuilder.build(sb1, context1, params1, e.getKey());
            sb.append(" = ");
            JpaSqlRequestBuilder.build(sb1, context1, params1, e.getValue());
        });

        var where = query.getRestriction();
        if (where != null) {
            sb.append(" WHERE ");
            JpaSqlRequestBuilder.build(sb, context, params, where);
        }

        var returning = q.getSelection();
        if (returning != null) {
            sb.append(" RETURNING ");
            JpaSqlRequestBuilder.build(sb, context, params, returning);
        }

        sb.append(';');
    }

    @SneakyThrows
    public static <X> Map<Parameter<?>, Object> build(StringBuilder sb, CriteriaUpdate<X> query, List<Parameter<?>> params, X arg, Metamodel metamodel, AbstractJpaExpressionFactory factory) {
        var model = metamodel.entity(arg.getClass());
        var idAttr = model.getId(model.getIdType().getJavaType());
        Object id;
        if (idAttr.getJavaMember() instanceof Field f) {
            id = f.get(arg);
        } else if (idAttr.getJavaMember() instanceof Method m) {
            id = m.invoke(arg);
        } else {
            throw new IllegalStateException();
        }

        Map<Parameter<?>, Object> binds = null;
        if (id == null) {
            // Insert
        } else {
            // Update
            binds = JpaSqlUpdateBuilder.populateUpdateQuery(new DefaultJpaExpressionFactory(metamodel), query, arg);
            JpaSqlUpdateBuilder.build(sb, query, params);
        }
        return binds;
    }

    @SneakyThrows
    public static <T> Map<Parameter<?>, Object> populateUpdateQuery(CriteriaBuilder factory, CriteriaUpdate<T> query, T arg) {
        var params = new HashMap<Parameter<?>, Object>();
        resolveAttributes(factory, query.getRoot())
                .forEach(p -> {
                    try {
                        var val = getValue(p, arg);
                        if (val == null) {
                            return;
                        }
                        if (p.getModel() instanceof Attribute<?,?> a){
                            if (List.of(
                                    Attribute.PersistentAttributeType.MANY_TO_ONE,
                                    Attribute.PersistentAttributeType.ONE_TO_ONE
                            ).contains(a.getPersistentAttributeType())) {
                                var id = a.getDeclaringType()
                                        .getAttributes()
                                        .stream()
                                        .filter(JpaSqlUpdateBuilder::isIdAttribute)
                                        .map(attr -> {
                                            try {
                                                return getValue(p.get(attr.getName()), arg);
                                            } catch (InvocationTargetException | IllegalAccessException e) {
                                                throw new RuntimeException(e);
                                            }
                                        })
                                        .findFirst()
                                        .orElse(null);
                                val = id;
                            }
                        }
                        if (val == null) {
                            return;
                        }
                        var pe = new JpaParameterExpression<>(val.getClass(), null);
                        forcePopulateQuery(query, p, pe);
                        params.put(pe, val);
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });

        var pv = populateIncrementVersionField(factory, query, arg);
        params.putAll(pv);
        pv = populateUpdateWhereStatement(factory, query, arg);
        params.putAll(pv);
        return params;
    }

    public static <T> Map<Parameter<?>, Object> populateIncrementVersionField(CriteriaBuilder factory, CriteriaUpdate<T> query, T arg) {
        var params = new HashMap<Parameter<?>, Object>();
        query.getRoot().getModel().getAttributes()
                .stream()
                .filter(JpaSqlUpdateBuilder::isVersionAttribute)
                .findFirst()
                .ifPresent(a ->{
                    var val = nextVersion(a, arg);
                    var p = factory.parameter(val.getClass());
                    forcePopulateQuery(query, query.getRoot().get(a.getName()), p);
                    params.put(p, val);
                } );
        return params;
    }

    public static void forcePopulateQuery(CriteriaUpdate<?> query, Path<?> path, Expression<?> exp) {
        try {
            Method m = CriteriaUpdate.class.getMethod("set", Path.class, Expression.class);
            m.invoke(query, path, exp);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Object nextVersion(Attribute<?,?> attr, T arg) {
        Class<?> type = attr.getJavaType();

        try {
            if (Number.class.isAssignableFrom(type)) {
                Number newVersion;
                if (Short.class.isAssignableFrom(type)) {
                    var v = (Short) getValue(attr, arg);
                    newVersion = (short) (v == null ? 1 : (v + 1));
                } else if (Integer.class.isAssignableFrom(type)) {
                    var v = (Integer) getValue(attr, arg);
                    newVersion = v == null ? 1 : v + 1;
                } else if (Long.class.isAssignableFrom(type)) {
                    var v = (Long) getValue(attr, arg);
                    newVersion = Long.valueOf(v == null ? 1 : v + 1);
                } else {
                    throw new IllegalArgumentException("Not supported number type " + type);
                }
                return newVersion;
            } else if (Timestamp.class.isAssignableFrom(type)) {
                return Timestamp.from(Instant.now());
            } else if (Instant.class.isAssignableFrom(type)) {
                return Instant.now();
            } else if (LocalDateTime.class.isAssignableFrom(type)) {
                return LocalDateTime.now();
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        throw new IllegalArgumentException("Unsupported version type");
    }

    public static <T> Map<Parameter<?>, Object> populateUpdateWhereStatement(CriteriaBuilder factory, CriteriaUpdate<T> query, T arg) {
        var params = new HashMap<Parameter<?>, Object>();
        query.getRoot().getModel().getAttributes()
                .stream()
                .filter(JpaSqlUpdateBuilder::isIdOrVersionAttribute)
                .map(a -> query.getRoot().get(a.getName()))
                .map(a -> {
                    var p = factory.parameter(a.getJavaType());
                    try {
                        params.put(p, getValue(a, arg));
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    return factory.equal(a, p);
                })
                .reduce(factory::and)
                .ifPresent(query::where);
        return params;
    }

    /**
     * Recursively resolves all subattributes of path
     */
    public static <T> List<Path<?>> resolveAttributes(CriteriaBuilder factory, Path<T> exp) {
        var attr = exp.getModel();
        ManagedType<?> type = null;
        Collection<Attribute<?, ?>> attributes = null;

        switch (attr.getBindableType()) {
            case ENTITY_TYPE -> type = (ManagedType<?>) attr;
            case SINGULAR_ATTRIBUTE -> {
                switch (((SingularAttribute<?, ?>) attr).getPersistentAttributeType()) {
                    case EMBEDDED:
                    case ONE_TO_ONE:
                        type = ((AbstractJpaExpressionFactory) factory).getMetamodel().managedType(attr.getBindableJavaType());
                        break;
                    default: return List.of(exp);
                }

            }
            default -> log.debug("Skipping unsupported bind type {}", attr.getBindableType());
        }

        if (type == null) {
            return List.of();
        }

        return type.getAttributes()
                .stream()
                .filter(a -> !isIdOrVersionAttribute(a))
                .filter(a -> List.of(Attribute.PersistentAttributeType.EMBEDDED, Attribute.PersistentAttributeType.BASIC, Attribute.PersistentAttributeType.MANY_TO_ONE, Attribute.PersistentAttributeType.ONE_TO_ONE).contains(a.getPersistentAttributeType()))
                .filter(JpaSqlUpdateBuilder::isInsertable)
                .map(a -> exp.get(a.getName()))
                .flatMap(a -> resolveAttributes(factory, a).stream())
                .toList();
    }

    public static boolean isInsertable(Attribute<?, ?> attr) {
        var member = attr.getJavaMember();
        if (member instanceof AnnotatedElement f) {
            return Optional.ofNullable(f.getAnnotation(Column.class))
                    .map(Column::insertable)
                    .orElse(true)
                    &&
                    Optional.ofNullable(f.getAnnotation(OneToOne.class))
                            .map(OneToOne::mappedBy)
                            .map(a -> !a.isBlank())
                            .orElse(true);
        }
        return true;
    }

    public static boolean isIdOrVersionAttribute(Attribute<?, ?> attr) {
        return isIdAttribute(attr) || isVersionAttribute(attr);
    }

    public static boolean isIdAttribute(Attribute<?, ?> attr) {
        if (attr instanceof SingularAttribute<?,?> a) {
            return a.isId();
        }
        return false;
    }

    public static boolean isVersionAttribute(Attribute<?, ?> attr) {
        if (attr instanceof SingularAttribute<?,?> a) {
            return a.isVersion();
        }
        return false;
    }

    /**
     * Gets value of path from given path root object
     * @param exp
     * @param arg
     * @return
     */
    public static <T, X extends T> X getValue(Path<T> exp, Object arg) throws InvocationTargetException, IllegalAccessException {
        var stack = new Stack<Path<?>>();
        Path<?> p = exp;
        while (p != null) {
            stack.push(p);
            p = p.getParentPath();
        }
        stack.pop();
        if (stack.empty()) {
            throw new IllegalStateException("Path does not have any values");
        }
        Object value = arg;
        while (!stack.empty()) {
            var pa = stack.pop();
            value = getValue((Attribute<?, ?>) pa.getModel(), value);
        }
        return (X) value;
    }

    public static <X, Y> Y getValue(Attribute<? extends X, Y> attr, X arg) throws InvocationTargetException, IllegalAccessException {
        Y value;
        if (attr.getJavaMember() instanceof Field f) {
            // TODO look for getter instead of messing with accessibility
            f.setAccessible(true);
            value = (Y) f.get(arg);
        } else if (attr.getJavaMember() instanceof Method m) {
            m.setAccessible(true);
            value = (Y) m.invoke(arg);
        } else {
            throw new IllegalStateException();
        }
        return value;
    }
}
