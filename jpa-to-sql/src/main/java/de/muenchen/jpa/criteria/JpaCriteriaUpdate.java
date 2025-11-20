package de.muenchen.jpa.criteria;

import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;

import java.util.*;


public class JpaCriteriaUpdate<T> extends JpaCommonAbstractCriteria<T> implements CriteriaUpdate<T> {

    private Predicate wheres = null;
    private Root<T> root = null;
    private Selection<T> selects = null;

    private final List<AbstractMap.SimpleImmutableEntry<Path<?>, Expression<?>>> sets = new ArrayList<>();

    public JpaCriteriaUpdate(Class<T> clazz, AbstractJpaExpressionFactory factory) {
        super(clazz, factory);
    }

    public CriteriaUpdate<T> select(Selection<? extends T> exp) {
        selects = (Selection<T>) exp;
        return this;
    }

    public Selection<T> getSelection() {
        return selects;
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
    public <Y, X extends Y> CriteriaUpdate<T> set(SingularAttribute<? super T, Y> singularAttribute, X x) {
        return set(singularAttribute, getFactory().literal(x));
    }

    @Override
    public <Y> CriteriaUpdate<T> set(SingularAttribute<? super T, Y> singularAttribute, Expression<? extends Y> expression) {
        return set(root.get(singularAttribute), expression);
    }

    @Override
    public <Y, X extends Y> CriteriaUpdate<T> set(Path<Y> path, X x) {
        return set(path, getFactory().literal(x));
    }

    @Override
    public <Y> CriteriaUpdate<T> set(Path<Y> path, Expression<? extends Y> expression) {
        sets.add(new AbstractMap.SimpleImmutableEntry<>(path, expression));
        return this;
    }

    @Override
    public CriteriaUpdate<T> set(String s, Object o) {
        return set(root.get(s), o);
    }

    @Override
    public CriteriaUpdate<T> where(Expression<Boolean> exp) {
        if (exp != null) {
            return where(new Predicate[] {new JpaPredicate(getFactory(), exp)});
        }
        this.wheres = null;
        return this;
    }

    @Override
    public CriteriaUpdate<T> where(Predicate... exp) {
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

    public List<Map.Entry<Path<?>, Expression<?>>> getSets() {
        return Collections.unmodifiableList(sets);
    }
}
