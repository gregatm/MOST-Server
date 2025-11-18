package de.muenchen.jpa.criteria;

import de.muenchen.jpa.JpaSqlRequestBuilder;
import jakarta.persistence.Parameter;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.Stream;

@Getter
@Setter
public class DefaultJpaExpressionFactory extends AbstractJpaExpressionFactory {

    public DefaultJpaExpressionFactory(Metamodel metamodel) {
        super(metamodel);
    }

    @Override
    public <T> Expression<T> createCastAs(Class<T> cls, Expression<?> exp) {
        return new CastAs<>(cls, this, exp);
    }

    @Override
    public <T> Expression<Class<? extends T>> createType(Path<T> exp) {
        return new Type<>(this, exp);
    }

    @Override
    public CriteriaQuery<Object> createQuery() {
        return createQuery(Object.class);
    }

    @Override
    public <T> CriteriaQuery<T> createQuery(Class<T> clazz) {
        return new JpaCriteriaQuery<>(clazz, this);
    }

    @Override
    public CriteriaQuery<Tuple> createTupleQuery() {
        return null;
    }

    @Override
    public <T> CriteriaUpdate<T> createCriteriaUpdate(Class<T> aClass) {
        return null;
    }

    @Override
    public <T> CriteriaDelete<T> createCriteriaDelete(Class<T> aClass) {
        return null;
    }

    @Override
    public <Y> CompoundSelection<Y> construct(Class<Y> cls, Selection<?>... exp) {
        if (exp.length == 0) {
            return null;
        }
        return new JpaCompoundSelection<>(cls, exp);
    }

    @Override
    public CompoundSelection<Tuple> tuple(Selection<?>... selections) {
        return null;
    }

    @Override
    public CompoundSelection<Tuple> tuple(List<Selection<?>> list) {
        return null;
    }

    @Override
    public CompoundSelection<Object[]> array(Selection<?>... selections) {
        return null;
    }

    @Override
    public CompoundSelection<Object[]> array(List<Selection<?>> list) {
        return null;
    }

    @Override
    public <N extends Number> Expression<Double> avg(Expression<N> e) {
        return new Avg(this, e);
    }

    @Override
    public <N extends Number> Expression<N> sum(Expression<N> e) {
        return null;
    }

    @Override
    public Expression<Long> sumAsLong(Expression<Integer> e) {
        return createCastAs(Long.class, sum(e));
    }

    @Override
    public Expression<Double> sumAsDouble(Expression<Float> e) {
        return createCastAs(Double.class, sum(e));
    }

    @Override
    public <N extends Number> Expression<N> max(Expression<N> e) {
        return new Max<>(this, e);
    }

    @Override
    public <N extends Number> Expression<N> min(Expression<N> e) {
        return new Min<>(this, e);
    }

    @Override
    public <X extends Comparable<? super X>> Expression<X> greatest(Expression<X> expression) {
        return null;
    }

    @Override
    public <X extends Comparable<? super X>> Expression<X> least(Expression<X> expression) {
        return null;
    }

    @Override
    public Expression<Long> count(Expression<?> expression) {
        return new Count(this, expression);
    }

    @Override
    public Expression<Long> countDistinct(Expression<?> expression) {
        return new Count(this, expression, true);
    }

    @Override
    public Predicate exists(Subquery<?> subquery) {
        return null;
    }

    @Override
    public <Y> Expression<Y> all(Subquery<Y> subquery) {
        return null;
    }

    @Override
    public <Y> Expression<Y> some(Subquery<Y> subquery) {
        return null;
    }

    @Override
    public <Y> Expression<Y> any(Subquery<Y> subquery) {
        return null;
    }

    @Override
    public Predicate and(Expression<Boolean> e1, Expression<Boolean> e2) {
        return or(Stream.of(e1, e2)
                .map(e -> new JpaPredicate(this, e))
                .toArray(Predicate[]::new)
        );
    }

    @Override
    public Predicate and(Predicate... predicates) {
        return new JpaPredicate(Predicate.BooleanOperator.AND, this, predicates);
    }

    @Override
    public Predicate and(List<Predicate> list) {
        return and(list.toArray(new Predicate[0]));
    }

    @Override
    public Predicate or(Expression<Boolean> e1, Expression<Boolean> e2) {
        return or(Stream.of(e1, e2)
                .map(e -> new JpaPredicate(this, e))
                .toArray(Predicate[]::new)
        );
    }

    @Override
    public Predicate or(Predicate... predicates) {
        return new JpaPredicate(Predicate.BooleanOperator.OR, this, predicates);
    }

    @Override
    public Predicate or(List<Predicate> list) {
        return or(list.toArray(new Predicate[0]));
    }

    @Override
    public Predicate not(Expression<Boolean> expression) {
        return null;
    }

    @Override
    public Predicate conjunction() {
        return null;
    }

    @Override
    public Predicate disjunction() {
        return null;
    }

    @Override
    public Predicate isTrue(Expression<Boolean> e) {
        return new JpaPredicate(this, e);
    }

    @Override
    public Predicate isFalse(Expression<Boolean> e) {
        return isTrue(e).not();
    }

    @Override
    public Predicate isNull(Expression<?> expression) {
        return new IsNull(this, expression);
    }

    @Override
    public Predicate isNotNull(Expression<?> expression) {
        return new IsNull(this, expression).not();
    }

    @Override
    public Predicate equal(Expression<?> e1, Expression<?> e2) {
        return new JpaPredicate(this, new Equal(this, e1, e2));
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate greaterThan(Expression<? extends Y> e1, Expression<? extends Y> e2) {
        return new JpaPredicate(this, new GreaterThan(this, e1, e2));
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Expression<? extends Y> e1, Expression<? extends Y> e2) {
        return new JpaPredicate(this, new GreaterThanOrEqual(this, e1, e2));
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate between(Expression<? extends Y> e1, Expression<? extends Y> e2, Expression<? extends Y> e3) {
        return null;
    }

    @Override
    public Predicate gt(Expression<? extends Number> e1, Expression<? extends Number> e2) {
        return new JpaPredicate(this, new GreaterThan(this, e1, e2));
    }

    @Override
    public Predicate ge(Expression<? extends Number> e1, Expression<? extends Number> e2) {
        return new JpaPredicate(this, new GreaterThanOrEqual(this, e1, e2));
    }

    @Override
    public Expression<Integer> sign(Expression<? extends Number> e) {
        return new Sign(this, e);
    }

    @Override
    public <N extends Number> Expression<N> neg(Expression<N> e) {
        return null;
    }

    @Override
    public <N extends Number> Expression<N> abs(Expression<N> e) {
        return new Abs<>(this, e);
    }

    @Override
    public <N extends Number> Expression<N> ceiling(Expression<N> e) {
        return new Ceiling<>(this, e);
    }

    @Override
    public <N extends Number> Expression<N> floor(Expression<N> e) {
        return new Floor<>(this, e);
    }

    @Override
    public <N extends Number> Expression<N> sum(Expression<? extends N> e1, Expression<? extends N> e2) {
        return null;
    }

    @Override
    public <N extends Number> Expression<N> prod(Expression<? extends N> e1, Expression<? extends N> e2) {
        return null;
    }

    @Override
    public <N extends Number> Expression<N> diff(Expression<? extends N> e1, Expression<? extends N> e2) {
        return null;
    }

    @Override
    public Expression<Number> quot(Expression<? extends Number> e1, Expression<? extends Number> e2) {
        return null;
    }

    @Override
    public Expression<Integer> mod(Expression<Integer> e1, Expression<Integer> e2) {
        return null;
    }

    @Override
    public Expression<Double> sqrt(Expression<? extends Number> e) {
        return new Sqrt(this, e);
    }

    @Override
    public Expression<Double> exp(Expression<? extends Number> e) {
        return null;
    }

    @Override
    public Expression<Double> ln(Expression<? extends Number> e) {
        return null;
    }

    @Override
    public Expression<Double> power(Expression<? extends Number> e1, Expression<? extends Number> e2) {
        return new Power<>(this, e1, e2);
    }

    @Override
    public <T extends Number> Expression<T> round(Expression<T> expression, Integer integer) {
        return null;
    }

    @Override
    public Expression<Long> toLong(Expression<? extends Number> expression) {
        return null;
    }

    @Override
    public Expression<Integer> toInteger(Expression<? extends Number> expression) {
        return null;
    }

    @Override
    public Expression<Float> toFloat(Expression<? extends Number> expression) {
        return null;
    }

    @Override
    public Expression<Double> toDouble(Expression<? extends Number> expression) {
        return null;
    }

    @Override
    public Expression<BigDecimal> toBigDecimal(Expression<? extends Number> expression) {
        return null;
    }

    @Override
    public Expression<BigInteger> toBigInteger(Expression<? extends Number> expression) {
        return null;
    }

    @Override
    public Expression<String> toString(Expression<Character> expression) {
        return null;
    }

    @Override
    public <T> Expression<T> nullLiteral(Class<T> cls) {
        return new Constant<>(cls, null, this);
    }

    @Override
    public <T> ParameterExpression<T> parameter(Class<T> cls) {
        return new JpaParameterExpression<>(cls, this);
    }

    @Override
    public <T> ParameterExpression<T> parameter(Class<T> cls, String s) {
        return new JpaParameterExpression<>(cls, s, this);
    }

    @Override
    public <T> ParameterExpression<T> parameter(Class<T> cls, Integer i) {
        return new JpaParameterExpression<>(cls, i, this);
    }

    @Override
    public <C extends Collection<?>> Predicate isEmpty(Expression<C> e) {
        return null;
    }

    @Override
    public <C extends Collection<?>> Expression<Integer> size(Expression<C> e) {
        return new Size(this, e);
    }

    @Override
    public <E, C extends Collection<E>> Predicate isMember(Expression<E> expression, Expression<C> expression1) {
        return null;
    }

    @Override
    public <V, M extends Map<?, V>> Expression<Collection<V>> values(M m) {
        return null;
    }

    @Override
    public <K, M extends Map<K, ?>> Expression<Set<K>> keys(M m) {
        return null;
    }

    @Override
    public Predicate like(Expression<String> e1, Expression<String> e2) {
        return null;
    }

    @Override
    public Predicate like(Expression<String> e1, Expression<String> e2, Expression<Character> e3) {
        return null;
    }

    @Override
    public Expression<String> concat(List<Expression<String>> list) {
        return null;
    }

    @Override
    public Expression<String> substring(Expression<String> e1, Expression<Integer> e2) {
        return null;
    }

    @Override
    public Expression<String> substring(Expression<String> e1, Expression<Integer> e2, Expression<Integer> e3) {
        return null;
    }

    @Override
    public Expression<String> trim(Trimspec trimspec, Expression<String> e1) {
        return null;
    }

    @Override
    public Expression<String> trim(Trimspec trimspec, Expression<Character> e1, Expression<String> e2) {
        return null;
    }

    @Override
    public Expression<String> trim(Trimspec trimspec, char c, Expression<String> expression) {
        return null;
    }

    @Override
    public Expression<String> lower(Expression<String> e) {
        return null;
    }

    @Override
    public Expression<String> upper(Expression<String> e) {
        return null;
    }

    @Override
    public Expression<Integer> length(Expression<String> e) {
        return null;
    }

    @Override
    public Expression<String> left(Expression<String> e1, Expression<Integer> e2) {
        return null;
    }

    @Override
    public Expression<String> right(Expression<String> e1, Expression<Integer> e2) {
        return null;
    }

    @Override
    public Expression<String> replace(Expression<String> e1, Expression<String> e2, Expression<String> e3) {
        return null;
    }

    @Override
    public Expression<Integer> locate(Expression<String> e1, Expression<String> e2) {
        return null;
    }

    @Override
    public Expression<Integer> locate(Expression<String> e1, Expression<String> e2, Expression<Integer> e3) {
        return null;
    }

    @Override
    public Expression<Date> currentDate() {
        return null;
    }

    @Override
    public Expression<Timestamp> currentTimestamp() {
        return null;
    }

    @Override
    public Expression<Time> currentTime() {
        return null;
    }

    @Override
    public Expression<LocalDate> localDate() {
        return null;
    }

    @Override
    public Expression<LocalDateTime> localDateTime() {
        return null;
    }

    @Override
    public Expression<LocalTime> localTime() {
        return null;
    }

    @Override
    public <N, T extends Temporal> Expression<N> extract(TemporalField<N, T> temporalField, Expression<T> expression) {
        return null;
    }

    @Override
    public <T> CriteriaBuilder.In<T> in(Expression<? extends T> e) {
        return new In<>(this, e);
    }

    @Override
    public <Y> Expression<Y> coalesce(Expression<? extends Y> e1, Expression<? extends Y> e2) {
        return null;
    }

    @Override
    public <Y> Expression<Y> nullif(Expression<Y> e1, Expression<?> e2) {
        return null;
    }

    @Override
    public <T> Coalesce<T> coalesce() {
        return null;
    }

    @Override
    public <C, R> SimpleCase<C, R> selectCase(Expression<? extends C> expression) {
        return null;
    }

    @Override
    public <R> Case<R> selectCase() {
        return null;
    }

    @Override
    public <T> Expression<T> function(String s, Class<T> aClass, Expression<?>... expressions) {
        return null;
    }

    @Override
    public <X, T, V extends T> Join<X, V> treat(Join<X, T> join, Class<V> aClass) {
        return null;
    }

    @Override
    public <X, T, E extends T> CollectionJoin<X, E> treat(CollectionJoin<X, T> collectionJoin, Class<E> aClass) {
        return null;
    }

    @Override
    public <X, T, E extends T> SetJoin<X, E> treat(SetJoin<X, T> setJoin, Class<E> aClass) {
        return null;
    }

    @Override
    public <X, T, E extends T> ListJoin<X, E> treat(ListJoin<X, T> listJoin, Class<E> aClass) {
        return null;
    }

    @Override
    public <X, K, T, V extends T> MapJoin<X, K, V> treat(MapJoin<X, K, T> mapJoin, Class<V> aClass) {
        return null;
    }

    @Override
    public <X, T extends X> Path<T> treat(Path<X> path, Class<T> aClass) {
        return null;
    }

    @Override
    public <X, T extends X> Root<T> treat(Root<X> root, Class<T> aClass) {
        return null;
    }

    @Override
    public <T> CriteriaSelect<T> union(CriteriaSelect<? extends T> criteriaSelect, CriteriaSelect<? extends T> criteriaSelect1) {
        return null;
    }

    @Override
    public <T> CriteriaSelect<T> unionAll(CriteriaSelect<? extends T> criteriaSelect, CriteriaSelect<? extends T> criteriaSelect1) {
        return null;
    }

    @Override
    public <T> CriteriaSelect<T> intersect(CriteriaSelect<? super T> criteriaSelect, CriteriaSelect<? super T> criteriaSelect1) {
        return null;
    }

    @Override
    public <T> CriteriaSelect<T> intersectAll(CriteriaSelect<? super T> criteriaSelect, CriteriaSelect<? super T> criteriaSelect1) {
        return null;
    }

    @Override
    public <T> CriteriaSelect<T> except(CriteriaSelect<T> criteriaSelect, CriteriaSelect<?> criteriaSelect1) {
        return null;
    }

    @Override
    public <T> CriteriaSelect<T> exceptAll(CriteriaSelect<T> criteriaSelect, CriteriaSelect<?> criteriaSelect1) {
        return null;
    }

    @Override
    public <X> Root<X> from(EntityType<X> et) {
        return new JpaRoot<>(et, this);
    }

    public <X> Root<X> from(Class<X> clazz) {
        var et = metamodel.entity(clazz);
        return from(et);
    }

    @Override
    public <X, Y> Join<X, Y> join(From<?, X> parent, EntityType<X> root, EntityType<Y> et, JoinType joinType) {
        return new JpaJoin<>(parent, root, et, joinType, this);
    }

    @Override
    public <X, Y> Join<X, Y> join(From<?, X> parent, Class<X> root, Class<Y> cls, JoinType joinType) {
        return join(parent, metamodel.entity(root), metamodel.entity(cls), joinType);
    }

    public static class SqlOperatorExpression {
        public SqlOperatorExpression(String operator) {

        }
    }

    public abstract static class FunctionalExpression<X> extends JpaExpression<X> {
        protected final Expression<?>[] args;
        protected final String function;

        public FunctionalExpression(Class<X> cls, String function,  AbstractJpaExpressionFactory factory, Expression<?>... args) {
            super(cls, factory);
            this.function = function;
            this.args = Arrays.copyOf(args, args.length);
        }
    }

    public abstract static class BinaryOperatorExpression<X> extends JpaExpression<X> {
        private final Expression<?> left;
        private final Expression<?> right;
        private final String operator;

        public BinaryOperatorExpression(Class<X> t, AbstractJpaExpressionFactory factory, String operator, Expression<?> e1, Expression<?> e2) {
            super(t, factory);
            this.left = e1;
            this.right = e2;
            this.operator = operator;
        }

        @Override
        public void toSql(StringBuilder sb, AliasContext context, List<Parameter<?>> params) {
            JpaSqlRequestBuilder.build(sb, context, params, left);
            sb.append(' ');
            sb.append(operator);
            sb.append(' ');
            JpaSqlRequestBuilder.build(sb, context, params, right);
        }
    }

    public static class Equal extends BinaryOperatorExpression<Boolean> {
        public Equal(AbstractJpaExpressionFactory factory, Expression<?> left, Expression<?> right) {
            super(Boolean.class, factory, "=", left, right);
        }
    }

    public static class GreaterThan extends BinaryOperatorExpression<Boolean> {
        public GreaterThan(AbstractJpaExpressionFactory factory, Expression<?> left, Expression<?> right) {
            super(Boolean.class, factory, ">", left, right);
        }
    }

    public static class GreaterThanOrEqual extends BinaryOperatorExpression<Boolean> {
        public GreaterThanOrEqual(AbstractJpaExpressionFactory factory, Expression<?> left, Expression<?> right) {
            super(Boolean.class, factory, ">=", left, right);
        }
    }

    public static class Abs<X> extends FunctionalExpression<X> {
        public Abs(AbstractJpaExpressionFactory factory, Expression<X> e) {
            super((Class<X>) e.getJavaType(), "ABS", factory, e);
        }
    }

    public static class Exponential extends FunctionalExpression<Double> {
        public Exponential(AbstractJpaExpressionFactory factory, Expression<?> e) {
            super(Double.class, "EXP", factory, e);
        }
    }

    public static class Floor<X> extends FunctionalExpression<X> {
        public Floor(AbstractJpaExpressionFactory factory, Expression<X> e) {
            super((Class<X>) e.getJavaType(), "FLOOR", factory, e);
        }
    }

    public static class Ceiling<X> extends FunctionalExpression<X> {
        public Ceiling(AbstractJpaExpressionFactory factory, Expression<X> e) {
            super((Class<X>) e.getJavaType(), "CEILING", factory, e);
        }
    }

    public static class NaturalLogarithm extends FunctionalExpression<Double> {
        public NaturalLogarithm(AbstractJpaExpressionFactory factory, Expression<? extends Number> e) {
            super(Double.class, "LN", factory, e);
        }
    }

    public static class Sign extends FunctionalExpression<Integer> {
        public Sign(AbstractJpaExpressionFactory factory, Expression<? extends Number> e) {
            super(Integer.class, "SIGN", factory, e);
        }
    }

    public static class Power<X, Y extends Number> extends FunctionalExpression<Double> {
        public Power(AbstractJpaExpressionFactory factory, Expression<X> x, Expression<Y> y) {
            super(Double.class, "POWER", factory, x, y);
        }

        public Power(AbstractJpaExpressionFactory factory, Expression<X> x, Y y) {
            this(factory, x, new Constant<>(y, factory));
        }
    }

    public static class Round<X, Y extends Number> extends FunctionalExpression<X> {
        public Round(AbstractJpaExpressionFactory factory, Expression<X> x, Expression<Y> y) {
            super((Class<X>) x.getJavaType(), "ROUND", factory, x, y);
        }
    }

    @Getter
    @Setter
    public static class Count extends FunctionalExpression<Long> {
        private boolean distinct;
        public Count(AbstractJpaExpressionFactory factory, Expression<?> e) {
            this(factory, e, false);
        }

        public Count(AbstractJpaExpressionFactory factory, Expression<?> e, boolean distinct) {
            super(Long.class, "COUNT", factory, e);
            this.distinct = distinct;
        }

        public Expression<Long> distinct(boolean distinct) {
            this.distinct = distinct;
            return this;
        }
    }

    public static class Avg extends FunctionalExpression<Double> {
        public Avg(AbstractJpaExpressionFactory factory, Expression<?> e) {
            super(Double.class, "AVG", factory, e);
        }
    }

    public static class Sqrt extends FunctionalExpression<Double> {
        public Sqrt(AbstractJpaExpressionFactory factory, Expression<? extends Number> e) {
            super(Double.class, "SQRT", factory, e);
        }
    }

    public static class Max<X> extends FunctionalExpression<X> {
        public Max(AbstractJpaExpressionFactory factory, Expression<?> e) {
            super((Class<X>) e.getJavaType(), "MAX", factory, e);
        }
    }

    public static class Min<X> extends FunctionalExpression<X> {
        public Min(AbstractJpaExpressionFactory factory, Expression<?> e) {
            super((Class<X>) e.getJavaType(), "MIN", factory, e);
        }
    }

    public static class Size extends FunctionalExpression<Integer> {
        public Size(AbstractJpaExpressionFactory factory, Expression<? extends Collection<?>> e) {
            super(Integer.class, "SIZE", factory, e);
        }
    }

    public static class Type<X extends Class> extends FunctionalExpression<X> {
        public Type(AbstractJpaExpressionFactory factory, Path<?> path) {
            super((Class) Class.class, "TYPE", factory, path);
        }
    }

    public static class In<T> extends JpaPredicate implements CriteriaBuilder.In<T> {
        private final Expression<T> e;
        private final List<Expression<?>> list = new ArrayList<>();
        public In(AbstractJpaExpressionFactory factory, Expression<?> exp) {
            super(factory);
            this.e = (Expression<T>) exp;
        }

        @Override
        public Expression<T> getExpression() {
            return e;
        }

        @Override
        public CriteriaBuilder.In<T> value(T t) {
            return value(factory.literal(t));
        }

        @Override
        public CriteriaBuilder.In<T> value(Expression<? extends T> e) {
            list.add(e);
            return this;
        }
    }

    public static class IsNull extends JpaPredicate {
        final Expression<?> e;
        public IsNull(AbstractJpaExpressionFactory factory, Expression<?> e) {
            super(factory);
            this.e = e;
        }
    }

    public static class CastAs<Y> extends JpaExpression<Y> {
        private final Expression<?> actual;
        public CastAs(Class<Y> cls, AbstractJpaExpressionFactory factory, Expression<?> exp) {
            super(cls, factory);
            this.actual = exp;
        }
    }
}
