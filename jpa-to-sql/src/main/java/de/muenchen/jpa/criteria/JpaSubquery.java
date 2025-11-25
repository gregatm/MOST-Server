package de.muenchen.jpa.criteria;

import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.EntityType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class JpaSubquery<T> extends JpaExpression<T> implements Subquery<T> {

    private final CriteriaQuery<T> delegate;
    private final CommonAbstractCriteria parent;

    public JpaSubquery(CommonAbstractCriteria parent, Class<T> cls, AbstractJpaExpressionFactory factory) {
        super(cls, factory);
        this.delegate = factory.createQuery(cls);
        this.parent = parent;
    }

    @Override
    public Subquery<T> select(Expression<T> expression) {
        delegate.select(expression);
        return this;
    }

    @Override
    public <X> Root<X> from(Class<X> aClass) {
        return delegate.from(aClass);
    }

    @Override
    public <X> Root<X> from(EntityType<X> entityType) {
        return null;
    }

    @Override
    public Subquery<T> where(Expression<Boolean> expression) {
        delegate.where(expression);
        return this;
    }

    @Override
    public Subquery<T> where(Predicate... predicates) {
        delegate.where(predicates);
        return this;
    }

    @Override
    public Subquery<T> where(List<Predicate> list) {
        delegate.where(list);
        return this;
    }

    @Override
    public Subquery<T> groupBy(Expression<?>... expressions) {
        delegate.groupBy(expressions);
        return this;
    }

    @Override
    public Subquery<T> groupBy(List<Expression<?>> list) {
        delegate.groupBy(list);
        return this;
    }

    @Override
    public Subquery<T> having(Expression<Boolean> expression) {
        delegate.having(expression);
        return this;
    }

    @Override
    public Subquery<T> having(Predicate... predicates) {
        delegate.having(predicates);
        return this;
    }

    @Override
    public Subquery<T> having(List<Predicate> list) {
        delegate.having(list);
        return this;
    }

    @Override
    public Subquery<T> distinct(boolean b) {
        delegate.distinct(b);
        return this;
    }

    @Override
    public Set<Root<?>> getRoots() {
        return delegate.getRoots();
    }

    @Override
    public <Y> Root<Y> correlate(Root<Y> root) {
        return null;
    }

    @Override
    public <X, Y> Join<X, Y> correlate(Join<X, Y> join) {
        return null;
    }

    @Override
    public <X, Y> CollectionJoin<X, Y> correlate(CollectionJoin<X, Y> collectionJoin) {
        return null;
    }

    @Override
    public <X, Y> SetJoin<X, Y> correlate(SetJoin<X, Y> setJoin) {
        return null;
    }

    @Override
    public <X, Y> ListJoin<X, Y> correlate(ListJoin<X, Y> listJoin) {
        return null;
    }

    @Override
    public <X, K, V> MapJoin<X, K, V> correlate(MapJoin<X, K, V> mapJoin) {
        return null;
    }

    @Override
    public AbstractQuery<?> getParent() {
        if (parent instanceof AbstractQuery<?> p) {
            return p;
        }
        throw new IllegalStateException("Subquery's parent is not an AbstractQuery, use getContainingQuery instead");
    }

    @Override
    public CommonAbstractCriteria getContainingQuery() {
        return parent;
    }

    @Override
    public Expression<T> getSelection() {
        return (Expression<T>) delegate.getSelection();
    }

    @Override
    public List<Expression<?>> getGroupList() {
        return delegate.getGroupList();
    }

    @Override
    public Predicate getGroupRestriction() {
        return delegate.getGroupRestriction();
    }

    @Override
    public boolean isDistinct() {
        return delegate.isDistinct();
    }

    @Override
    public Class<T> getResultType() {
        return delegate.getResultType();
    }

    @Override
    public Set<Join<?, ?>> getCorrelatedJoins() {
        return Set.of();
    }

    @Override
    public <U> Subquery<U> subquery(Class<U> aClass) {
        return delegate.subquery(aClass);
    }

    @Override
    public <U> Subquery<U> subquery(EntityType<U> entityType) {
        return delegate.subquery(entityType);
    }

    @Override
    public Predicate getRestriction() {
        return delegate.getRestriction();
    }

    @Override
    public Set<ParameterExpression<?>> getParameters() {
        return delegate.getParameters();
    }
}
