package de.muenchen.mostserver.data.jpa.criteria;

import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.*;
import org.apache.openjpa.persistence.meta.AbstractManagedType;

import java.util.HashSet;
import java.util.Set;

public class JpaFrom<Z, X> extends JpaPath<Z, X> implements From<Z, X> {

    private final Set<Join<X, ?>> joins = new HashSet<>();
    private final Type<X> type;

    public JpaFrom(AbstractManagedType<X> type) {
        super(type.getJavaType());
        this.type = type;
    }

    public JpaFrom(AbstractManagedType<X> type, JpaExpressionFactory factory) {
        super(type.getJavaType(), factory);
        this.type = type;
    }

    @Override
    public Set<Join<X, ?>> getJoins() {
        return Set.of();
    }

    @Override
    public boolean isCorrelated() {
        return false;
    }

    @Override
    public From<Z, X> getCorrelationParent() {
        return null;
    }

    @Override
    public <Y> Join<X, Y> join(Class<Y> aClass) {
        return null;
    }

    @Override
    public <Y> Join<X, Y> join(Class<Y> aClass, JoinType joinType) {
        return null;
    }

    @Override
    public <Y> Join<X, Y> join(EntityType<Y> entityType) {
        return null;
    }

    @Override
    public <Y> Join<X, Y> join(EntityType<Y> entityType, JoinType joinType) {
        return null;
    }

    @Override
    public <Y> Join<X, Y> join(SingularAttribute<? super X, Y> singularAttribute) {
        return null;
    }

    @Override
    public <Y> Join<X, Y> join(SingularAttribute<? super X, Y> singularAttribute, JoinType joinType) {
        return null;
    }

    @Override
    public <Y> CollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collectionAttribute) {
        return null;
    }

    @Override
    public <Y> SetJoin<X, Y> join(SetAttribute<? super X, Y> setAttribute) {
        return null;
    }

    @Override
    public <Y> ListJoin<X, Y> join(ListAttribute<? super X, Y> listAttribute) {
        return null;
    }

    @Override
    public <K, V> MapJoin<X, K, V> join(MapAttribute<? super X, K, V> mapAttribute) {
        return null;
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
        return null;
    }

    @Override
    public <X1, Y> CollectionJoin<X1, Y> joinCollection(String s) {
        return null;
    }

    @Override
    public <X1, Y> SetJoin<X1, Y> joinSet(String s) {
        return null;
    }

    @Override
    public <X1, Y> ListJoin<X1, Y> joinList(String s) {
        return null;
    }

    @Override
    public <X1, K, V> MapJoin<X1, K, V> joinMap(String s) {
        return null;
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
        return null;
    }

    @Override
    public <Y> Fetch<X, Y> fetch(SingularAttribute<? super X, Y> singularAttribute, JoinType joinType) {
        return null;
    }

    @Override
    public <Y> Fetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> pluralAttribute) {
        return null;
    }

    @Override
    public <Y> Fetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> pluralAttribute, JoinType joinType) {
        return null;
    }

    @Override
    public <X1, Y> Fetch<X1, Y> fetch(String s) {
        return null;
    }

    @Override
    public <X1, Y> Fetch<X1, Y> fetch(String s, JoinType joinType) {
        return null;
    }
}
