package de.muenchen.jpa.metamodel;

import jakarta.persistence.metamodel.EmbeddableType;

public class EmbeddableTypeImpl<X> extends ManagedTypeImpl<X> implements EmbeddableType<X> {
    public EmbeddableTypeImpl(Class<X> javaType) {
        super(PersistenceType.EMBEDDABLE, javaType);
    }
}
