package de.muenchen.mostserver.data.jpa.criteria;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JpaPredicate extends JpaExpression<Boolean> implements Predicate {
    private final List<Expression<Boolean>> expressions;
    private final BooleanOperator operator;

    public JpaPredicate(Expression<Boolean>... exp) {
        this(BooleanOperator.AND, exp);
    }

    public JpaPredicate(BooleanOperator operator, Expression<Boolean>... exp) {
        super(Boolean.class);
        this.operator = operator;
        this.expressions = Arrays.asList(exp);
    }


    @Override
    public BooleanOperator getOperator() {
        return operator;
    }

    @Override
    public boolean isNegated() {
        return false;
    }

    @Override
    public List<Expression<Boolean>> getExpressions() {
        return Collections.unmodifiableList(expressions);
    }

    @Override
    public Predicate not() {
        return null;
    }
}
