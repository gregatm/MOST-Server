package de.muenchen.jpa.metamodel;

import jakarta.persistence.metamodel.Type;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TypeImpl<X> implements Type<X> {
    private final PersistenceType persistenceType;
    private final Class<X> javaType;
}
