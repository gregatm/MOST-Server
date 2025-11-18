package de.muenchen.jpa.criteria;

import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.EntityType;

import java.util.Arrays;
import java.util.Objects;

public class JpaCriteriaDelete<T> extends JpaCommonAbstractCriteria<T> implements CriteriaDelete<T> {

    private Predicate wheres = null;
    private Root<T> root = null;

    public JpaCriteriaDelete(Class<T> clazz, AbstractJpaExpressionFactory factory) {
        super(clazz, factory);
    }

    @Override
    public Root<T> from(Class<T> cls) {
        if (cls != null) {
            this.root = getFactory().from(cls);
            return this.root;
        }
        throw new IllegalStateException("Not yet implemented");
    }

    @Override
    public Root<T> from(EntityType<T> et) {
        if (et != null) {
            root = getFactory().from(et);
            return this.root;
        }
        throw new IllegalStateException("Not yet implemented");
    }

    @Override
    public Root<T> getRoot() {
        return root;
    }

    @Override
    public CriteriaDelete<T> where(Expression<Boolean> exp) {
        if (exp != null) {
            return where(new Predicate[] {new JpaPredicate(getFactory(), exp)});
        }
        this.wheres = null;
        return this;
    }

    @Override
    public CriteriaDelete<T> where(Predicate... exp) {
        if (exp.length == 0) {
            this.wheres = null;
        }
        this.wheres = getFactory().and(Arrays.stream(exp).filter(Objects::nonNull).toList());
        return this;
    }

    @Override
    public Predicate getRestriction() {
        return wheres;
    }
}
