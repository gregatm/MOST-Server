package de.muenchen.mostserver.data.jpa.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collection;

public class DefaultJpaExpressionFactory implements JpaExpressionFactory {

    @Override
    public <T> CriteriaBuilder.In<T> createIn(Expression<T> exp) {
        return new In<>(exp);
    }

    @Override
    public Predicate createIsNull(Expression<?> exp) {
        return new IsNull(exp);
    }

    @Override
    public Predicate createIsNotNull(Expression<?> exp) {
        return new IsNull(exp).not();
    }

    @Override
    public <T> Expression<T> createCastAs(Class<T> cls, Expression<?> exp) {
        return new CastAs<>(cls, exp);
    }

    @Override
    public <T> Expression<T> createNot(Expression<T> exp) {
        return null;
    }

    @Override
    public <T> Expression<T> createAbs(Expression<T> exp) {
        return null;
    }

    @Override
    public <T> Expression<Class<? extends T>> createType(Path<T> exp) {
        return new Type<>(exp);
    }

    @Override
    public Expression<Boolean> createEqual(Expression<?> left, Expression<?> right) {
        return new Equal(left, right);
    }

    @Override
    public <T> Expression<T> createConstant(T o) {
        return new Constant<>(o);
    }

    public abstract static class FunctionalExpression<X> extends JpaExpression<X> {
        protected final Expression<?>[] args;
        protected final String function;

        public FunctionalExpression(Class<X> cls, String function, Expression<?>... args) {
            super(cls);
            this.function = function;
            this.args = Arrays.copyOf(args, args.length);
        }
    }

    public abstract static class BinaryOperatorExpression<X> extends JpaExpression<X> {
        private final Expression<?> left;
        private final Expression<?> right;
        private final Expression<X> operator;

        public BinaryOperatorExpression(Class<X> t, Expression<X> operator, Expression<?> e1, Expression<?> e2) {
            super(t);
            this.left = e1;
            this.right = e2;
            this.operator = operator;
        }
    }

    public static class Equal extends BinaryOperatorExpression<Boolean> {
        public Equal(Expression<?> left, Expression<?> right) {
            super(Boolean.class, Constant<>("="), left, right);
        }
    }

    public static class Abs<X> extends FunctionalExpression<X> {
        public Abs(Class<X> cls, Expression<?> e) {
            super(cls, "ABS", e);
        }
    }

    public static class Exponential extends FunctionalExpression<Double> {
        public Exponential(Expression<?> e) {
            super(Double.class, "EXP", e);
        }
    }

    public static class Floor<X> extends FunctionalExpression<X> {
        public Floor(Expression<X> e) {
            super((Class<X>) e.getJavaType(), "FLOOR", e);
        }
    }

    public static class Ceiling<X> extends FunctionalExpression<X> {
        public Ceiling(Expression<X> e) {
            super((Class<X>) e.getJavaType(), "CEILING", e);
        }
    }

    public static class NaturalLogarithm extends FunctionalExpression<Double> {
        public NaturalLogarithm(Expression<? extends Number> e) {
            super(Double.class, "LN", e);
        }
    }

    public static class Sign extends FunctionalExpression<Double> {
        public Sign(Expression<? extends Number> e) {
            super(Integer.class, "SIGN", e);
        }
    }

    public static class Power<X, Y extends Number> extends FunctionalExpression<Double> {
        public Power(Expression<X> x, Expression<Y> y) {
            super(Double.class, "POWER", x, y);
        }

        public Power(Expression<X> x, Y y) {
            this(x, new Constant<>(y));
        }
    }

    public static class Round<X, Y extends Number> extends FunctionalExpression<X> {
        public Power(Expression<X> x, Expression<Y> y) {
            super((Class<X>) x.getJavaType(), "ROUND", x, y);
        }
    }

    @Getter
    @Setter
    public static class Count extends FunctionalExpression<Long> {
        private boolean distinct;
        public Count(Expression<?> e) {
            this(e, false);
        }

        public Count(Expression<?> e, boolean distinct) {
            super(Long.class, "COUNT", e);
            this.distinct = distinct;
        }

        public Expression<Long> distinct(boolean distinct) {
            this.distinct = distinct;
            return this;
        }
    }

    public static class Avg extends FunctionalExpression<Double> {
        public Avg(Expression<?> e) {
            super(Double.class, "AVG", e);
        }
    }

    public static class Sqrt extends FunctionalExpression<Double> {
        public Sqrt(Expression<? extends Number> e) {
            super(Double.class, "SQRT", e);
        }
    }

    public static class Max<X> extends FunctionalExpression<X> {
        public Max(Expression<?> e) {
            super((Class<X>) e.getJavaType(), "MAX", e);
        }
    }

    public static class Min<X> extends FunctionalExpression<X> {
        public Min(Expression<?> e) {
            super((Class<X>) e.getJavaType(), "MIN", e);
        }
    }

    public static class Size extends FunctionalExpression<Integer> {
        public Size(Expression<? extends Collection<?>> e) {
            super(Integer.class, "SIZE", e);
        }
    }

    public static class Constant<X> extends JpaExpression<X> {
        public final X arg;
        public Constant(Class<X> t, X x) {
            super(t);
            this.arg = x;
        }

        public Constant(X x) {
            this(x == null ? null : (Class<X>) x.getClass(), x);
        }

        static Constant<Void> createNull() {
            return new Constant<>(Void.class, null);
        }
    }

    public static class Type<X extends Class> extends FunctionalExpression<X> {
        public Type(Path<?> path) {
            super((Class) Class.class, "TYPE", path);
        }
    }

    public static class In<T> extends JpaPredicate implements CriteriaBuilder.In<T> {
        private final Expression<T> e;
        public In(Expression<T> exp) {
            this.e = exp;
        }

        @Override
        public Expression<T> getExpression() {
            return e;
        }

        @Override
        public CriteriaBuilder.In<T> value(T t) {
            return null;
        }

        @Override
        public CriteriaBuilder.In<T> value(Expression<? extends T> expression) {
            return null;
        }
    }

    public static class IsNull extends JpaPredicate {
        final Expression<?> e;
        public IsNull(Expression<?> e) {
            this.e = e;
        }
    }

    public static class CastAs<Y> extends JpaExpression<Y> {
        private final Expression<?> actual;
        public CastAs(Class<Y> cls, Expression<?> exp) {
            super(cls);
            this.actual = exp;
        }
    }
}
