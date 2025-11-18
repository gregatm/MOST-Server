package de.muenchen.jpa.criteria;

import de.muenchen.jpa.JpaSqlRequestBuilder;
import jakarta.persistence.Parameter;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Selection;

import java.util.List;

public class AggregateSelections {

    public static class AggregateFunction<X> extends JpaSelection<X> {
        private final String function;
        private final List<Selection<?>> parameters;
        public AggregateFunction(Class<X> cls, String function, Selection<?>... parameters) {
            super(cls);
            this.function = function;
            this.parameters = List.of(parameters);
        }

        @Override
        public void toSql(StringBuilder sb, AliasContext context, List<Parameter<?>> params) {
            sb.append(function);
            sb.append('(');
            JpaSqlRequestBuilder.build(sb, context, params, parameters.getFirst());
            parameters.stream().skip(1)
                    .forEach(p -> {
                        sb.append(", ");
                        JpaSqlRequestBuilder.build(sb, context, params, p);
                        ExpressionUtils.appendAliasIfExists(sb, context, p, false);
                    });
            sb.append(")");
        }
    }

    public static class ArrayAggregate<X> extends AggregateFunction<X> {
        public ArrayAggregate(Class<X> cls, Selection<?> exp) {
            super(cls, "array_agg", exp);
        }
    }

    public static class ArrayRemove<X> extends AggregateFunction<X> {
        public ArrayRemove(Class<X> cls, Selection<?> array, Selection<?> remove) {
            super(cls, "array_remove", array, remove);
        }
    }
}
