package de.muenchen.jpa.metamodel;

import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;

import java.util.Set;

public class EntityTypeImpl<X> extends IdentifiableTypeImpl<X> implements EntityType<X> {

    private final String name;

    public EntityTypeImpl(Class<X> cls, String name) {
        super(PersistenceType.ENTITY, cls);
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public BindableType getBindableType() {
        return BindableType.ENTITY_TYPE;
    }

    @Override
    public Class<X> getBindableJavaType() {
        return this.getJavaType();
    }

    public <Y> Attribute<X, Y> getJoinAttribute(EntityType<Y> entity) {
        return (Attribute<X, Y>) getAttributes()
                .stream()
                .filter(a ->
                        Set.of(Attribute.PersistentAttributeType.ONE_TO_ONE,
                                        Attribute.PersistentAttributeType.MANY_TO_ONE,
                                        Attribute.PersistentAttributeType.ONE_TO_MANY,
                                        Attribute.PersistentAttributeType.MANY_TO_MANY)
                                .contains(a.getPersistentAttributeType()))
                .filter(a -> entity.getJavaType().isAssignableFrom(switch (a) {
                    case PluralAttribute<?, ?, ?> p -> p.getBindableJavaType();
                    case SingularAttribute<?, ?> s -> s.getBindableJavaType();
                    default -> throw new IllegalStateException("Unexpected value: " + a);
                }))
                .findFirst()
                .orElseThrow();
    }

}
