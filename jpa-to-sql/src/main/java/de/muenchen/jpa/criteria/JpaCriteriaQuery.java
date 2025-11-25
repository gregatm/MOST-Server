package de.muenchen.jpa.criteria;

import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.EntityType;
import lombok.Getter;

import java.util.*;

@Getter
public class JpaCriteriaQuery<T> extends JpaAbstractQuery<T> implements CriteriaQuery<T> {

    private final List<Order> orderBys = new ArrayList<>();
    private Selection<T> selects = null;

    public JpaCriteriaQuery(Class<T> clazz, AbstractJpaExpressionFactory factory) {
        super(clazz, factory);
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
        select(this.getFactory().construct(this.getClazz(), exp));
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
    public Selection<T> getSelection() {
        return selects;
    }

    @Override
    public List<Order> getOrderList() {
        return orderBys;
    }

    public <X> CriteriaQuery<T> from(CriteriaQuery<X> query) {
        throw new IllegalStateException("Not yet implemented");
    }

    @Override
    public CriteriaQuery<T> where(Expression<Boolean> exp) {
        return (CriteriaQuery<T>) super.where(exp);
    }

    @Override
    public CriteriaQuery<T> where(Predicate... exp) {
        return (CriteriaQuery<T>) super.where(exp);
    }

    @Override
    public CriteriaQuery<T> where(List<Predicate> exp) {
        return (CriteriaQuery<T>) super.where(exp);
    }

    @Override
    public CriteriaQuery<T> groupBy(Expression<?>... exp) {
        return (CriteriaQuery<T>) super.groupBy(exp);
    }

    @Override
    public CriteriaQuery<T> groupBy(List<Expression<?>> exp) {
        return (CriteriaQuery<T>) super.groupBy(exp);
    }

    @Override
    public CriteriaQuery<T> having(Expression<Boolean> exp) {
        return (CriteriaQuery<T>) super.having(exp);
    }

    @Override
    public CriteriaQuery<T> having(Predicate... exp) {
        return (CriteriaQuery<T>) super.having(exp);
    }

    @Override
    public CriteriaQuery<T> having(List<Predicate> exp) {
        return (CriteriaQuery<T>) super.having(exp);
    }

    @Override
    public CriteriaQuery<T> distinct(boolean d) {
        return (CriteriaQuery<T>) super.distinct(d);
    }
}