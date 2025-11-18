package de.muenchen.jpa.criteria;

import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.*;

import java.util.HashSet;
import java.util.Set;

public class JpaFrom<Z, X> extends JpaPath<X> implements From<Z, X> {

    private final Set<Join<X, ?>> joins = new HashSet<>();

    public JpaFrom(EntityType<X> type, AbstractJpaExpressionFactory factory) {
        super(type, factory);
    }

    public JpaFrom(Path<?> parent, Bindable<X> member, AbstractJpaExpressionFactory factory) {
        super(parent, member, factory);
    }

    @Override
    public Set<Join<X, ?>> getJoins() {
        return Set.copyOf(joins);
    }

    @Override
    public boolean isCorrelated() {
        return false;
    }

    @Override
    public From<Z, X> getCorrelationParent() {
        return null;
    }

    public <Y> Join<X, Y> join(CriteriaQuery<Y> query) {
        return null;
    }

    @Override
    public <Y> Join<X, Y> join(Class<Y> clazz) {
        return join(clazz, JoinType.INNER);
    }

    @Override
    public <Y> Join<X, Y> join(Class<Y> clazz, JoinType joinType) {
        var j = factory.join(this, ((ManagedType<X>) this.getType()).getJavaType(), clazz, joinType);
        joins.add(j);
        return j;
    }

    @Override
    public <Y> Join<X, Y> join(EntityType<Y> entityType) {
        return join(entityType, JoinType.INNER);
    }

    @Override
    public <Y> Join<X, Y> join(EntityType<Y> entityType, JoinType joinType) {
        return null;
    }

    @Override
    public <Y> Join<X, Y> join(SingularAttribute<? super X, Y> singularAttribute) {
        return join(singularAttribute, JoinType.INNER);
    }

    @Override
    public <Y> Join<X, Y> join(SingularAttribute<? super X, Y> singularAttribute, JoinType joinType) {
        return null;
    }

    @Override
    public <Y> CollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collectionAttribute) {
        return join(collectionAttribute, JoinType.INNER);
    }

    @Override
    public <Y> SetJoin<X, Y> join(SetAttribute<? super X, Y> setAttribute) {
        return join(setAttribute, JoinType.INNER);
    }

    @Override
    public <Y> ListJoin<X, Y> join(ListAttribute<? super X, Y> listAttribute) {
        return join(listAttribute, JoinType.INNER);
    }

    @Override
    public <K, V> MapJoin<X, K, V> join(MapAttribute<? super X, K, V> mapAttribute) {
        return join(mapAttribute, JoinType.INNER);
    }

    @Override
    public <Y> CollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collectionAttribute, JoinType joinType) {
        return null;
    }

    @Override
    public <Y> SetJoin<X, Y> join(SetAttribute<? super X, Y> setAttribute, JoinType joinType) {
        return null;
    }

    @Override
    public <Y> ListJoin<X, Y> join(ListAttribute<? super X, Y> listAttribute, JoinType joinType) {
        return null;
    }

    @Override
    public <K, V> MapJoin<X, K, V> join(MapAttribute<? super X, K, V> mapAttribute, JoinType joinType) {
        return null;
    }

    @Override
    public <X1, Y> Join<X1, Y> join(String s) {
        return join(s, JoinType.INNER);
    }

    @Override
    public <X1, Y> CollectionJoin<X1, Y> joinCollection(String s) {
        return joinCollection(s, JoinType.INNER);
    }

    @Override
    public <X1, Y> SetJoin<X1, Y> joinSet(String s) {
        return joinSet(s, JoinType.INNER);
    }

    @Override
    public <X1, Y> ListJoin<X1, Y> joinList(String s) {
        return joinList(s, JoinType.INNER);
    }

    @Override
    public <X1, K, V> MapJoin<X1, K, V> joinMap(String s) {
        return joinMap(s, JoinType.INNER);
    }

    @Override
    public <X1, Y> Join<X1, Y> join(String s, JoinType joinType) {
        return null;
    }

    @Override
    public <X1, Y> CollectionJoin<X1, Y> joinCollection(String s, JoinType joinType) {
        return null;
    }

    @Override
    public <X1, Y> SetJoin<X1, Y> joinSet(String s, JoinType joinType) {
        return null;
    }

    @Override
    public <X1, Y> ListJoin<X1, Y> joinList(String s, JoinType joinType) {
        return null;
    }

    @Override
    public <X1, K, V> MapJoin<X1, K, V> joinMap(String s, JoinType joinType) {
        return null;
    }

    @Override
    public Set<Fetch<X, ?>> getFetches() {
        return Set.of();
    }

    @Override
    public <Y> Fetch<X, Y> fetch(SingularAttribute<? super X, Y> singularAttribute) {
        return fetch(singularAttribute, JoinType.INNER);
    }

    @Override
    public <Y> Fetch<X, Y> fetch(SingularAttribute<? super X, Y> singularAttribute, JoinType joinType) {
        return null;
    }

    @Override
    public <Y> Fetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> pluralAttribute) {
        return fetch(pluralAttribute, JoinType.INNER);
    }

    @Override
    public <Y> Fetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> pluralAttribute, JoinType joinType) {
        return null;
    }

    @Override
    public <X1, Y> Fetch<X1, Y> fetch(String s) {
        return fetch(s, JoinType.INNER);
    }

    @Override
    public <X1, Y> Fetch<X1, Y> fetch(String s, JoinType joinType) {
        return null;
    }
}
