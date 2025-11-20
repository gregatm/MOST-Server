package de.muenchen.jpa.criteria;

import jakarta.persistence.Parameter;
import jakarta.persistence.criteria.ParameterExpression;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
public class JpaParameterExpression<T> extends JpaExpression<T> implements ParameterExpression<T> {

    private final String name;
    private final Integer position;

    protected JpaParameterExpression(Class<T> cls, String name, Integer position, AbstractJpaExpressionFactory factory) {
        super(cls, factory);
        this.name = name;
        this.position = position;
    }

    public JpaParameterExpression(Class<T> cls, AbstractJpaExpressionFactory factory) {
        this(cls, null, null, factory);
    }

    public JpaParameterExpression(Class<T> cls, String name, AbstractJpaExpressionFactory factory) {
        this(cls, name, null, factory);
    }

    public JpaParameterExpression(Class<T> cls, Integer position, AbstractJpaExpressionFactory factory) {
        this(cls, null, position, factory);
    }

    @Override
    public Class<T> getParameterType() {
        return (Class<T>) this.getJavaType();
    }

    @Override
    public boolean equals(Object o) {
        if (name == null && position == null) {
            return super.equals(o);
        }
        if (o == null || getClass() != o.getClass()) return false;
        JpaParameterExpression<?> that = (JpaParameterExpression<?>) o;
        return Objects.equals(name, that.name) && Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        if (name == null && position == null) {
            return super.hashCode();
        }
        return Objects.hash(name, position);
    }
}
