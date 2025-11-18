package de.muenchen.jpa.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;

public interface JpaExpressionFactory extends CriteriaBuilder {

    <T> Expression<T> createCastAs(Class<T> cls, Expression<?> exp);
    <T> Expression<Class<? extends T>> createType(Path<T> exp);
}
