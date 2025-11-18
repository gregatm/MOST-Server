package de.muenchen.jpa.criteria;

import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.EntityType;

import java.util.*;

public abstract class JpaAbstractQuery<T> extends JpaCommonAbstractCriteria<T> implements AbstractQuery<T> {

    private Predicate wheres = null;
    private final Set<Expression<?>> groupBys = new LinkedHashSet<>();
    private Predicate havings = null;
    private final Set<Root<?>> roots = new HashSet<>();
    private Boolean isDistinct = false;

    public JpaAbstractQuery(Class<T> clazz, AbstractJpaExpressionFactory factory) {
        super(clazz, factory);
    }

    @Override
    public <X> Root<X> from(Class<X> cls) {
        if (cls != null) {
            var root = getFactory().from(cls);
            roots.add(root);
            return root;
        }
        throw new IllegalStateException("Not yet implemented");
    }

    @Override
    public <X> Root<X> from(EntityType<X> et) {
        if (et != null) {
            var root = getFactory().from(et);
            roots.add(root);
            return root;
        }
        throw new IllegalStateException("Not yet implemented");
    }

    @Override
    public AbstractQuery<T> where(Expression<Boolean> exp) {
        if (exp != null) {
            return where(new Predicate[] {new JpaPredicate(getFactory(), exp)});
        }
        this.wheres = null;
        return this;
    }

    @Override
    public AbstractQuery<T> where(Predicate... exp) {
        if (exp.length == 0) {
            this.wheres = null;
        }
        this.wheres = getFactory().and(Arrays.stream(exp).filter(Objects::nonNull).toList());
        return this;
    }

    @Override
    public AbstractQuery<T> where(List<Predicate> exp) {
        if (exp != null) {
            return where(exp.toArray(new Predicate[0]));
        }
        this.wheres = null;
        return this;
    }

    @Override
    public AbstractQuery<T> groupBy(Expression<?>... exp) {
        Arrays.stream(exp).filter(Objects::nonNull)
                .forEach(groupBys::add);
        return this;
    }

    @Override
    public AbstractQuery<T> groupBy(List<Expression<?>> exp) {
        if (exp != null) {
            return groupBy(exp.toArray(new Expression[0]));
        }
        return this;
    }

    @Override
    public AbstractQuery<T> having(Expression<Boolean> exp) {
        if (exp != null) {
            return having(new Predicate[]{new JpaPredicate(getFactory(), exp)});
        }
        this.havings = null;
        return this;
    }

    @Override
    public AbstractQuery<T> having(Predicate... exp) {
        if (exp.length == 0) {
            this.havings = null;
        }
        this.havings = getFactory().and(Arrays.stream(exp).filter(Objects::nonNull).toList());
        return this;
    }

    @Override
    public AbstractQuery<T> having(List<Predicate> exp) {
        if (exp != null) {
            return having(exp.toArray(new Predicate[0]));
        }
        this.havings = null;
        return this;
    }

    @Override
    public AbstractQuery<T> distinct(boolean d) {
        this.isDistinct = d;
        return this;
    }

    @Override
    public Set<Root<?>> getRoots() {
        return roots;
    }

    @Override
    public Predicate getRestriction() {
        return wheres;
    }

    @Override
    public List<Expression<?>> getGroupList() {
        return List.copyOf(groupBys);
    }

    @Override
    public Predicate getGroupRestriction() {
        return havings;
    }

    @Override
    public boolean isDistinct() {
        return this.isDistinct;
    }

    @Override
    public Class<T> getResultType() {
        return getClazz();
    }
}
