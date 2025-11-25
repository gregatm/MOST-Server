package de.muenchen.jpa.criteria;

import de.muenchen.jpa.criteria.JpaExpressionFactory;
import jakarta.persistence.Parameter;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public abstract class AbstractJpaExpressionFactory implements JpaExpressionFactory {

    protected final Metamodel metamodel;

    public abstract <T> ParameterExpression<T> parameter(Class<T> cls, Integer i);

    public abstract <X> Root<X> from(EntityType<X> et);

    public abstract <X> Root<X> from(Class<X> et);

    public abstract <X> Subquery<X> subquery(CommonAbstractCriteria query, Class<X> cls);

    public abstract <X, Y> Join<X, Y> join(From<?, X> parent, EntityType<X> root, EntityType<Y> et, JoinType joinType);
    public abstract <X, Y> Join<X, Y> join(From<?, X> parent, Class<X> root, Class<Y> cls, JoinType joinType);

    public Order order(Expression<?> exp) {
        return order(exp, true, Nulls.NONE);
    }

    public Order order(Expression<?> exp, boolean ascending) {
        return order(exp, ascending, Nulls.NONE);
    }

    public Order order(Expression<?> exp, boolean ascending, Nulls nullPrecedence) {
        return new JpaOrder(exp, ascending, nullPrecedence);
    }

    @Override
    public Order asc(Expression<?> e) {
        return asc(e, Nulls.NONE);
    }

    @Override
    public Order desc(Expression<?> e) {
        return desc(e, Nulls.NONE);
    }

    @Override
    public Order asc(Expression<?> e, Nulls nulls) {
        return order(e, true, nulls);
    }

    @Override
    public Order desc(Expression<?> e, Nulls nulls) {
        return order(e, false, nulls);
    }

    @Override
    public Predicate equal(Expression<?> e, Object o) {
        return equal(e, literal(o));
    }

    @Override
    public Predicate notEqual(Expression<?> e1, Expression<?> e2) {
        return equal(e1, e2).not();
    }

    @Override
    public Predicate notEqual(Expression<?> e, Object o) {
        return equal(e, o).not();
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate lessThan(Expression<? extends Y> e1, Expression<? extends Y> e2) {
        return greaterThanOrEqualTo(e2, e1);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> e1, Expression<? extends Y> e2) {
        return greaterThan(e2, e1);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> e1, Y y) {
        return lessThanOrEqualTo(e1, literal(y));
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate between(Expression<? extends Y> e1, Y y, Y y1) {
        return between(e1, literal(y), literal(y1));
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate greaterThan(Expression<? extends Y> e, Y y) {
        return greaterThan(e, literal(y));
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Expression<? extends Y> e, Y y) {
        return greaterThanOrEqualTo(e, literal(y));
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate lessThan(Expression<? extends Y> e, Y y) {
        return lessThan(e, literal(y));
    }

    @Override
    public Predicate lt(Expression<? extends Number> e1, Expression<? extends Number> e2) {
        return ge(e2, e1);
    }

    @Override
    public Predicate le(Expression<? extends Number> e1, Expression<? extends Number> e2) {
        return gt(e2, e1);
    }

    @Override
    public Predicate gt(Expression<? extends Number> expression, Number number) {
        return gt(expression, literal(number));
    }

    @Override
    public Predicate ge(Expression<? extends Number> e, Number number) {
        return ge(e, literal(number));
    }

    @Override
    public Predicate lt(Expression<? extends Number> e, Number number) {
        return lt(e, literal(number));
    }

    @Override
    public Predicate le(Expression<? extends Number> e, Number number) {
        return le(e, literal(number));
    }

    @Override
    public <N extends Number> Expression<N> sum(Expression<? extends N> e, N n) {
        return sum(e, literal(n));
    }

    @Override
    public <N extends Number> Expression<N> sum(N n, Expression<? extends N> e) {
        return sum(literal(n), e);
    }

    @Override
    public <N extends Number> Expression<N> prod(Expression<? extends N> e, N n) {
        return prod(e, literal(n));
    }

    @Override
    public <N extends Number> Expression<N> prod(N n, Expression<? extends N> e) {
        return prod(literal(n), e);
    }

    @Override
    public <N extends Number> Expression<N> diff(Expression<? extends N> e, N n) {
        return diff(e, literal(n));
    }

    @Override
    public <N extends Number> Expression<N> diff(N n, Expression<? extends N> e) {
        return diff(literal(n), e);
    }

    @Override
    public Expression<Double> power(Expression<? extends Number> e, Number number) {
        return power(e, literal(number));
    }

    @Override
    public Expression<Number> quot(Expression<? extends Number> e, Number number) {
        return quot(e, literal(number));
    }

    @Override
    public Expression<Number> quot(Number number, Expression<? extends Number> e) {
        return quot(literal(number), e);
    }

    @Override
    public Expression<Integer> mod(Integer integer, Expression<Integer> e) {
        return mod(literal(integer), e);
    }

    @Override
    public Expression<Integer> mod(Expression<Integer> e, Integer integer) {
        return mod(e, literal(integer));
    }

    @Override
    public <C extends Collection<?>> Predicate isNotEmpty(Expression<C> e) {
        return isEmpty(e).not();
    }

    @Override
    public <C extends Collection<?>> Expression<Integer> size(C objects) {
        return size(literal(objects));
    }

    @Override
    public <E, C extends Collection<E>> Predicate isMember(E e, Expression<C> e2) {
        return isMember(literal(e), e2);
    }

    @Override
    public <E, C extends Collection<E>> Predicate isNotMember(Expression<E> e1, Expression<C> e2) {
        return isMember(e1, e2).not();
    }

    @Override
    public <E, C extends Collection<E>> Predicate isNotMember(E e, Expression<C> e1) {
        return isMember(literal(e), e1).not();
    }

    @Override
    public Predicate like(Expression<String> e1, String s) {
        return like(e1, literal(s));
    }

    @Override
    public Predicate like(Expression<String> e1, Expression<String> e2, char c) {
        return like(e1, e2, literal(c));
    }

    @Override
    public Predicate like(Expression<String> e1, String s, Expression<Character> e2) {
        return like(e1, literal(s), e2);
    }

    @Override
    public Predicate like(Expression<String> e1, String s, char c) {
        return like(e1, literal(s), literal(c));
    }

    @Override
    public Predicate notLike(Expression<String> e1, Expression<String> e2) {
        return like(e1, e2).not();
    }

    @Override
    public Predicate notLike(Expression<String> e1, String s) {
        return like(e1, s).not();
    }

    @Override
    public Predicate notLike(Expression<String> e1, Expression<String> e2, Expression<Character> e3) {
        return like(e1, e2, e3).not();
    }

    @Override
    public Predicate notLike(Expression<String> e1, Expression<String> e2, char c) {
        return like(e1, e2, c).not();
    }

    @Override
    public Predicate notLike(Expression<String> e1, String s, Expression<Character> e3) {
        return like(e1, s, e3).not();
    }

    @Override
    public Predicate notLike(Expression<String> e1, String s, char c) {
        return like(e1, s, c).not();
    }

    @Override
    public Expression<String> concat(Expression<String> e1, Expression<String> e2) {
        return concat(List.of(e1, e2));
    }

    @Override
    public Expression<String> concat(Expression<String> e1, String s) {
        return concat(e1, literal(s));
    }

    @Override
    public Expression<String> concat(String s, Expression<String> e2) {
        return concat(literal(s), e2);
    }

    @Override
    public Expression<String> substring(Expression<String> e1, int i) {
        return substring(e1, literal(i));
    }

    @Override
    public Expression<String> substring(Expression<String> e1, int i, int i1) {
        return substring(e1, literal(i), literal(i1));
    }

    @Override
    public Expression<String> trim(Expression<String> e1) {
        return trim(Trimspec.BOTH, e1);
    }

    @Override
    public Expression<String> trim(Expression<Character> e1, Expression<String> e2) {
        return trim(Trimspec.BOTH, e1, e2);
    }

    @Override
    public Expression<String> trim(char c, Expression<String> e1) {
        return trim(literal(c), e1);
    }

    @Override
    public Expression<String> left(Expression<String> e1, int i) {
        return left(e1, literal(i));
    }

    @Override
    public Expression<String> right(Expression<String> e1, int i) {
        return right(e1, literal(i));
    }

    @Override
    public Expression<String> replace(Expression<String> e1, String s, Expression<String> e3) {
        return replace(e1, literal(s), e3);
    }

    @Override
    public Expression<String> replace(Expression<String> e1, Expression<String> e2, String s) {
        return replace(e1, e2, literal(s));
    }

    @Override
    public Expression<String> replace(Expression<String> e1, String s, String s1) {
        return replace(e1, literal(s), literal(s1));
    }

    @Override
    public Expression<Integer> locate(Expression<String> e1, String s) {
        return locate(e1, literal(s));
    }

    @Override
    public Expression<Integer> locate(Expression<String> e1, String s, int i) {
        return locate(e1, literal(s), literal(i));
    }

    @Override
    public <Y> Expression<Y> coalesce(Expression<? extends Y> e1, Y y) {
        return coalesce(e1, literal(y));
    }

    @Override
    public <Y> Expression<Y> nullif(Expression<Y> e1, Y y) {
        return nullif(e1, literal(y));
    }

    @Override
    public <X> Constant<X> literal(X x) {
        return new Constant<>(x, this);
    }



    public static class Constant<X> extends JpaExpression<X> {
        public final X arg;
        public Constant(Class<X> t, X x, AbstractJpaExpressionFactory factory) {
            super(t, factory);
            this.arg = x;
        }

        public Constant(X x, AbstractJpaExpressionFactory factory) {
            this(x == null ? null : (Class<X>) x.getClass(), x, factory);
        }

        @Override
        public void toSql(StringBuilder sb, AliasContext context, List<Parameter<?>> params) {
            Object constant = arg;
            if (arg instanceof String s) {
                sb.append('\'');
                constant = s.replace("'", "''");
            }
            sb.append(constant);
            if (arg instanceof String) {
                sb.append('\'');
            }
        }
    }
}
