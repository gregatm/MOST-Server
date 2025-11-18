package de.muenchen.jpa.criteria;

import jakarta.persistence.criteria.CommonAbstractCriteria;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.EntityType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public abstract class JpaCommonAbstractCriteria<T> implements CommonAbstractCriteria {

    private final Class<T> clazz;
    private final AbstractJpaExpressionFactory factory;


    @Override
    public <U> Subquery<U> subquery(Class<U> aClass) {
        return null;
    }

    @Override
    public <U> Subquery<U> subquery(EntityType<U> entityType) {
        return null;
    }

    @Override
    public Set<ParameterExpression<?>> getParameters() {
        throw new IllegalStateException("Not yet implemented");
    }
}
