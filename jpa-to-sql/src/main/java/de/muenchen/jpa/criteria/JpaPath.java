package de.muenchen.jpa.criteria;

import de.muenchen.jpa.metamodel.SqlIdentifier;
import jakarta.persistence.Parameter;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.metamodel.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JpaPath<X> extends JpaExpression<X> implements Path<X> {
    private final Path<?> parent;
    private final Bindable<X> member;

    protected JpaPath(Bindable<X> type, AbstractJpaExpressionFactory factory) {
        super(type.getBindableJavaType(), factory);
        this.parent = null;
        this.member = type;
    }

    public JpaPath(Path<?> parent, Bindable<X> member, AbstractJpaExpressionFactory factory) {
        super((Class<X>) parent.getModel().getBindableJavaType(), factory);
        this.parent = parent;
        this.member = member;
    }

    @Override
    public Bindable<X> getModel() {
        return this.member;
    }

    @Override
    public Path<?> getParentPath() {
        return parent;
    }

    @Override
    public <Y> Path<Y> get(SingularAttribute<? super X, Y> attr) {
        if (getType() != attr.getDeclaringType()) {
            var model = factory.metamodel.managedType(this.getModel().getBindableJavaType());
            attr = (SingularAttribute) model.getAttribute(attr.getName());
        }
        return new JpaPath<>(this, attr, factory);
    }

    @Override
    public <E, C extends Collection<E>> Expression<C> get(PluralAttribute<? super X, C, E> coll) {
        throw new IllegalStateException();
    }

    @Override
    public <K, V, M extends Map<K, V>> Expression<M> get(MapAttribute<? super X, K, V> map) {
        throw new IllegalStateException();
    }

    @Override
    public Expression<Class<? extends X>> type() {
        return factory.createType(this);
    }

    @Override
    public <Y> Path<Y> get(String attName) {
        if (this.getType().getPersistenceType() == Type.PersistenceType.BASIC) {
            throw new IllegalArgumentException(this + " is a basic path and can not be navigated to " + attName);
        }
        var model = factory.metamodel.managedType(this.getModel().getBindableJavaType());

        var attr = model.getAttribute(attName);

        Bindable<Y> next = null;

        if (attr instanceof Bindable<?> b) {
            next = (Bindable<Y>) b;
        } else if (attr != null) {
            throw new IllegalArgumentException("Cannot create path from non bindable attribute");
        }
        return new JpaPath<>(this, next, factory);
    }

    public Type<?> getType() {
        if (this.member instanceof ManagedType<?> m) {
            return m;
        } else if (this.member instanceof Attribute<?,?> a) {
            return a.getDeclaringType();
        }
        throw new IllegalStateException("Path element provides no element");
    }
}
