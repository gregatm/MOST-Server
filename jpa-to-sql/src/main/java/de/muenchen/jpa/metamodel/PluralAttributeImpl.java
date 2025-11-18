package de.muenchen.jpa.metamodel;

import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.Type;
import lombok.Getter;

import java.lang.reflect.Member;

@Getter
public abstract class PluralAttributeImpl<X, Y, E> extends AttributeImpl<X, Y> implements PluralAttribute<X, Y, E> {

    private final CollectionType collectionType;
    private final Type<E> elementType;

    public PluralAttributeImpl(PersistentAttributeType persistentAttributeType, Member member, String sqlIdentifier, ManagedType<X> declaringType, CollectionType collectionType, Type<E> elementType) {
        super(persistentAttributeType, member, sqlIdentifier, declaringType, false, false);
        this.elementType = elementType;
        this.collectionType = collectionType;
    }

    @Override
    public BindableType getBindableType() {
        return BindableType.PLURAL_ATTRIBUTE;
    }

    @Override
    public Class<E> getBindableJavaType() {
        return elementType.getJavaType();
    }
}
