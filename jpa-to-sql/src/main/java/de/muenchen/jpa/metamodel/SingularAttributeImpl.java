package de.muenchen.jpa.metamodel;

import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.Type;
import lombok.Getter;

import java.lang.reflect.Member;

@Getter
public class SingularAttributeImpl<X, T> extends AttributeImpl<X, T> implements SingularAttribute<X, T> {

    private final boolean isOptional;
    private final Type<T> type;

    public SingularAttributeImpl(
            PersistentAttributeType persistentAttributeType,
            Member member,
            String sqlIdentifier,
            ManagedType<X> declaringType,
            Type<T> type,
            boolean isId,
            boolean isVersion,
            boolean isOptional
    ) {
        super(persistentAttributeType, member, sqlIdentifier, declaringType, isId, isVersion);
        this.isOptional = isOptional;
        this.type = type;
    }

    @Override
    public ManagedType<X> getDeclaringType() {
        return super.getDeclaringType();
    }

    @Override
    public BindableType getBindableType() {
        return BindableType.SINGULAR_ATTRIBUTE;
    }

    @Override
    public Class<T> getBindableJavaType() {
        return getJavaType();
    }
}
