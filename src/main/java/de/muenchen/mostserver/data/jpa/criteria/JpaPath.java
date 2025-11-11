package de.muenchen.mostserver.data.jpa.criteria;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.metamodel.*;
import org.apache.openjpa.persistence.meta.Members;

import java.util.Collection;
import java.util.Map;

public class JpaPath<Z, X> extends JpaExpression<X> implements Path<X> {
    private final JpaPath<?, Z> parent;
    private final Members.Member<? super Z, ?> member;

    protected JpaPath(Class<X> cls) {
        this(null, null, cls);
    }

    protected JpaPath(Class<X> cls, JpaExpressionFactory factory) {
        this(null, null, cls, factory);
    }

    public JpaPath(JpaPath<?, Z> parent, Members.Member<? super Z, ?> member, Class<X> cls) {
        super(cls);
        this.parent = parent;
        this.member = member;
    }

    public JpaPath(JpaPath<?, Z> parent, Members.Member<? super Z, ?> member, Class<X> cls, JpaExpressionFactory factory) {
        super(cls, factory);
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
    public Path<Z> getParentPath() {
        return parent;
    }

    @Override
    public <Y> Path<Y> get(SingularAttribute<? super X, Y> attr) {
        if (getType() != attr.getDeclaringType()) {
            attr = (SingularAttribute)((ManagedType)getType()).getAttribute(attr.getName());
        }
        return new JpaPath<>(this, (Members.SingularAttributeImpl<? super X, Y>)attr, attr.getJavaType());
    }

    @Override
    public <E, C extends Collection<E>> Expression<C> get(PluralAttribute<? super X, C, E> coll) {
        if (getType() != coll.getDeclaringType()) {
            coll = (PluralAttribute)((ManagedType)getType()).getAttribute(coll.getName());
        }
        return new JpaPath<>(this, (Members.PluralAttributeImpl<? super X, C, E>)coll, coll.getJavaType());
    }

    @Override
    public <K, V, M extends Map<K, V>> Expression<M> get(MapAttribute<? super X, K, V> map) {
        if (getType() != map.getDeclaringType()) {
            map = (MapAttribute)((ManagedType)getType()).getAttribute(map.getName());
        }
        return new JpaPath<>(this, (Members.MapAttributeImpl<? super X,K,V>)map, (Class<M>)map.getJavaType());
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

        Members.Member<? super X, Y> next = (Members.Member<? super X, Y>)
                ((ManagedType<? super X>)type).getAttribute(attName);
        return new JpaPath<>(this, next, next.getJavaType());
    }

    public Type<?> getType() {
        return member.getType();
    }
}
