package de.muenchen.jpa.criteria;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Nulls;
import jakarta.persistence.criteria.Order;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class JpaOrder implements Order {

    private final Expression<?> expression;
    private final boolean isAscending;
    private final Nulls nullPrecedence;

    @Override
    public Order reverse() {
        var nulls = switch(nullPrecedence) {
            case LAST -> Nulls.FIRST;
            case FIRST -> Nulls.LAST;
            case NONE -> Nulls.NONE;
        };
        return new JpaOrder(expression, !isAscending, nulls);
    }
}
