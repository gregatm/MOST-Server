package de.muenchen.jpa.criteria;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.EntityType;
import lombok.Getter;

import java.util.*;

@Getter
public class JpaCriteriaQuery<T> implements CriteriaQuery<T> {

    private Predicate wheres = null;
    private Selection<T> selects = null;
    private final Set<Expression<?>> groupBys = new LinkedHashSet<>();
    private Predicate havings = null;
    private List<Order> orderBys = new ArrayList<>();
    private Set<Root<?>> roots = new HashSet<>();
    private Boolean isDistinct = false;

    private final Class<T> clazz;
    private final AbstractJpaExpressionFactory factory;

    public JpaCriteriaQuery(Class<T> clazz, AbstractJpaExpressionFactory factory) {
        this.clazz = clazz;
        this.factory = factory;
    }

    private Integer limit = null;
    private Integer offset = null;

    public CriteriaQuery<T> limit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public CriteriaQuery<T> offset(Integer offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public CriteriaQuery<T> select(Selection<? extends T> exp) {
        selects = (Selection<T>) exp;
        return this;
    }

    @Deprecated(since = "Deprecated in Java")
    @Override
    public CriteriaQuery<T> multiselect(Selection<?>... exp) {
        select(factory.construct(this.clazz, exp));
        return this;
    }

    @Deprecated(since = "Deprecated in Java")
    @Override
    public CriteriaQuery<T> multiselect(List<Selection<?>> selects) {
        if (selects != null) {
            multiselect(selects.toArray(new Selection[0]));
        }
        return this;
    }

    @Override
    public CriteriaQuery<T> where(Expression<Boolean> exp) {
        if (exp != null) {
            return where(new Predicate[] {new JpaPredicate(factory, exp)});
        }
        this.wheres = null;
        return this;
    }

    @Override
    public CriteriaQuery<T> where(Predicate... exp) {
        if (exp.length == 0) {
            this.wheres = null;
        }
        this.wheres = factory.and(Arrays.stream(exp).filter(Objects::nonNull).toList());
        return this;
    }

    @Override
    public CriteriaQuery<T> where(List<Predicate> exp) {
        if (exp != null) {
            return where(exp.toArray(new Predicate[0]));
        }
        this.wheres = null;
        return this;
    }

    @Override
    public CriteriaQuery<T> groupBy(Expression<?>... exp) {
        Arrays.stream(exp).filter(Objects::nonNull)
                .forEach(groupBys::add);
        return this;
    }

    @Override
    public CriteriaQuery<T> groupBy(List<Expression<?>> exp) {
        if (exp != null) {
            return groupBy(exp.toArray(new Expression[0]));
        }
        return this;
    }

    @Override
    public CriteriaQuery<T> having(Expression<Boolean> exp) {
        if (exp != null) {
            return having(new Predicate[]{new JpaPredicate(factory, exp)});
        }
        this.havings = null;
        return this;
    }

    @Override
    public CriteriaQuery<T> having(Predicate... exp) {
        if (exp.length == 0) {
            this.havings = null;
        }
        this.havings = factory.and(Arrays.stream(exp).filter(Objects::nonNull).toList());
        return this;
    }

    @Override
    public CriteriaQuery<T> having(List<Predicate> exp) {
        if (exp != null) {
            return having(exp.toArray(new Predicate[0]));
        }
        this.havings = null;
        return this;
    }

    @Override
    public CriteriaQuery<T> orderBy(Order... exp) {
        orderBys.clear();
        orderBys.addAll(Arrays.stream(exp).filter(Objects::nonNull).toList());
        return this;
    }

    @Override
    public CriteriaQuery<T> orderBy(List<Order> exp) {
        if (exp != null) {
            return orderBy(exp.toArray(new Order[0]));
        }
        return this;
    }

    @Override
    public CriteriaQuery<T> distinct(boolean d) {
        this.isDistinct = d;
        return this;
    }

    @Override
    public List<Order> getOrderList() {
    return orderBys;
    }

    @Override
    public <X> Root<X> from(Class<X> cls) {
    if (cls != null) {
        var root = factory.from(cls);
        roots.add(root);
        return root;
    }
        throw new IllegalStateException("Not yet implemented");
    }

    @Override
    public <X> Root<X> from(EntityType<X> et) {
    if (et != null) {
        var root = factory.from(et);
        roots.add(root);
        return root;
    }
        throw new IllegalStateException("Not yet implemented");
    }

    public <X> CriteriaQuery<T> from(CriteriaQuery<X> query) {
        throw new IllegalStateException("Not yet implemented");
    }

    @Override
    public Set<Root<?>> getRoots() {
        return roots;
    }

    @Override
    public Selection<T> getSelection() {
    return selects;
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
        return clazz;
    }

    @Override
    public <U> Subquery<U> subquery(Class<U> p0) {
        throw new IllegalStateException("Not yet implemented");
    }

    @Override
    public <U> Subquery<U> subquery(EntityType<U> p0) {
        throw new IllegalStateException("Not yet implemented");
    }

    @Override
    public Predicate getRestriction() {
        return wheres;
    }

    @Override
    public Set<ParameterExpression<?>> getParameters() {
            throw new IllegalStateException("Not yet implemented");
    }
}