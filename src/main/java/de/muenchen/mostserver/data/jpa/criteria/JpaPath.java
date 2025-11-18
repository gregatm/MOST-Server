package de.muenchen.mostserver.data.jpa.criteria;

import de.muenchen.mostserver.data.jpa.meta.SqlIdentifier;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.metamodel.*;
import org.apache.openjpa.persistence.meta.Members;

import java.util.Collection;
import java.util.Map;

public class JpaPath<X> extends JpaExpression<X> implements Path<X> {
    private final Path<?> parent;
    private final Attribute<?, X> member;

    protected JpaPath(Class<X> cls, AbstractJpaExpressionFactory factory) {
        super(cls, factory);
        this.parent = null;
        this.member = null;
    }

    public JpaPath(Path<?> parent, Attribute<?, X> member, AbstractJpaExpressionFactory factory) {
        super((Class<X>) parent.getModel().getBindableJavaType(), factory);
        this.parent = parent;
        this.member = member;
    }

    @Override
    public Bindable<X> getModel() {
        if (!(member instanceof Bindable<?>)) {
            throw new IllegalArgumentException(this + " represents a basic path and not a bindable");
        }
        return (Bindable<X>) member;
    }

    @Override
    public Path<?> getParentPath() {
        return parent;
    }

    @Override
    public <Y> Path<Y> get(SingularAttribute<? super X, Y> attr) {
        if (getType() != attr.getDeclaringType()) {
            attr = (SingularAttribute)((ManagedType)getType()).getAttribute(attr.getName());
        }
        return new JpaPath<>(this, attr, factory);
    }

    @Override
    public <E, C extends Collection<E>> Expression<C> get(PluralAttribute<? super X, C, E> coll) {
        if (getType() != coll.getDeclaringType()) {
            coll = (PluralAttribute)((ManagedType)getType()).getAttribute(coll.getName());
        }
        return new JpaPath<>(this, coll, factory);
    }

    @Override
    public <K, V, M extends Map<K, V>> Expression<M> get(MapAttribute<? super X, K, V> map) {
        if (getType() != map.getDeclaringType()) {
            map = (MapAttribute)((ManagedType)getType()).getAttribute(map.getName());
        }
        // TODO Cast should be unnecessary
        return (Expression<M>) new JpaPath<>(this, map, factory);
    }

    @Override
    public Expression<Class<? extends X>> type() {
        return factory.createType(this);
    }

    @Override
    public <Y> Path<Y> get(String attName) {
        Type<?> type = this.getType();
        if (type.getPersistenceType() == Type.PersistenceType.BASIC) {
            throw new IllegalArgumentException(this + " is a basic path and can not be navigated to " + attName);
        }

        var next = (Attribute<?, Y>) ((ManagedType<Y>) type).getAttribute(attName);
        return new JpaPath<>(this, next, factory);
    }

    public Type<?> getType() {
        return member.getDeclaringType();
    }

    @Override
    public void toSql(StringBuilder sb, AliasContext context, Query params) {
        sb.append('"');
        if (parent != null) {
            sb.append(parent.getAlias());
            sb.append('.');
        }
        if (this.member instanceof SqlIdentifier m) {
            sb.append(m.getSqlIdentifier());
        } else {
            sb.append(this.member.getName());
        }
        sb.append('"');
    }
}
