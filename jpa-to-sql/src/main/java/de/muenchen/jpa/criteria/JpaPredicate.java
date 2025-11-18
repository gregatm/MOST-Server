package de.muenchen.jpa.criteria;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Selection;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JpaPredicate extends JpaExpression<Boolean> implements Predicate {
    private final List<Expression<Boolean>> expressions;
    private final BooleanOperator operator;
    private final boolean isNegated;

    public JpaPredicate(AbstractJpaExpressionFactory factory, Expression<Boolean>... exp) {
        this(BooleanOperator.AND, factory, exp);
    }

    public JpaPredicate(BooleanOperator operator, AbstractJpaExpressionFactory factory, Expression<Boolean>... exp) {
        this(operator, factory, false, exp);
    }

    protected JpaPredicate(BooleanOperator operator, AbstractJpaExpressionFactory factory, boolean negated, Expression<Boolean>... exp) {
        super(Boolean.class, factory);
        this.isNegated = negated;
        this.operator = operator;
        this.expressions = Arrays.asList(exp);
    }

    @Override
    public BooleanOperator getOperator() {
        return operator;
    }

    @Override
    public boolean isNegated() {
        return isNegated;
    }

    @Override
    public List<Expression<Boolean>> getExpressions() {
        return Collections.unmodifiableList(expressions);
    }

    @Override
    public Predicate not() {
        return new JpaPredicate(operator, factory, !isNegated, expressions.toArray(new Expression[0]));
    }
}
