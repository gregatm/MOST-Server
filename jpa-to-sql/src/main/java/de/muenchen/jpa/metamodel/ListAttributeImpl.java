package de.muenchen.jpa.metamodel;

import de.muenchen.jpa.metamodel.PluralAttributeImpl;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Type;

import java.lang.reflect.Member;
import java.util.List;

public class ListAttributeImpl<X, E> extends PluralAttributeImpl<X, List<E>, E> implements ListAttribute<X, E> {
    public ListAttributeImpl(PersistentAttributeType attributeType, Member member, String sqlIdentifier,
                             ManagedType<X> declaringType, Type<E> elementType) {
        super(attributeType, member, sqlIdentifier, declaringType, CollectionType.LIST, elementType);
    }
}
