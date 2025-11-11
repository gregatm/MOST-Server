package de.muenchen.mostserver.data.jpa.criteria;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

public abstract class JpaExpression<X> extends JpaSelection<X> implements Expression<X> {

    protected final JpaExpressionFactory factory;

    public JpaExpression(Class<X> cls) {
        this(cls, new DefaultJpaExpressionFactory());
    }

    public JpaExpression(Class<X> cls, JpaExpressionFactory factory) {
        super(cls);
        this.factory = factory;
    }

    @Override
    public Predicate isNull() {
        return factory.createIsNull(this);
    }

    @Override
    public Predicate isNotNull() {
        return factory.createIsNotNull(this);
    }

    @Override
    public Predicate in(Object... objects) {
        return in(Arrays.stream(objects)
                .map(o -> (X) o));
    }

    @Override
    public Predicate in(Expression<?>... expressions) {
        return in(Arrays.stream(expressions));
    }

    @Override
    public Predicate in(Collection<?> collection) {
        return in(collection.stream()
                .map(o -> (X) o));
    }

    private Predicate in(Stream<X> stream) {
        var result = factory.createIn(this);
        stream.forEach(result::value);
        return result;
    }

    @Override
    public Predicate in(Expression<Collection<?>> expression) {
        return null;
    }

    @Override
    public <Y> Expression<Y> as(Class<Y> cls) {
        return cls == getJavaType() ? (Expression<Y>) this : factory.createCastAs(cls, this);
    }

    @Override
    public Predicate equalTo(Expression<?> expression) {
        return new JpaPredicate(factory.createEqual(this, expression));
    }

    @Override
    public Predicate equalTo(Object o) {
        return equalTo(factory.createConstant(o));
    }

    @Override
    public Predicate notEqualTo(Expression<?> expression) {
        return equalTo(expression).not();
    }

    @Override
    public Predicate notEqualTo(Object o) {
        return equalTo(o).not();
    }

    @Override
    public <X1> Expression<X1> cast(Class<X1> aClass) {
        throw new NotImplementedException();
    }
}
