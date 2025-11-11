package de.muenchen.mostserver.data.jpa.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

public interface JpaExpressionFactory {

    <T> CriteriaBuilder.In<T> createIn(Expression<T> exp);

    Predicate createIsNull(Expression<?> exp);

    Predicate createIsNotNull(Expression<?> exp);

    <T> Expression<T> createCastAs(Class<T> cls, Expression<?> exp);

    <T> Expression<T> createNot(Expression<T> exp);

    <T> Expression<T> createAbs(Expression<T> exp);

    Predicate createEqual(Expression<?> exp);

    <T> Expression<Class<? extends T>> createType(Path<T> exp);

    Expression<Boolean> createEqual(Expression<?> left, Expression<?> right);

    <T> Expression<T> createConstant(T o);
}
