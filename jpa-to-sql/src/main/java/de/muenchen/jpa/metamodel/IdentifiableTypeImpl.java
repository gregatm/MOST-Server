package de.muenchen.jpa.metamodel;

import de.muenchen.jpa.metamodel.ManagedTypeImpl;
import jakarta.persistence.metamodel.IdentifiableType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.Type;
import lombok.Setter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Setter
public abstract class IdentifiableTypeImpl<X> extends ManagedTypeImpl<X> implements IdentifiableType<X> {

    private final Set<SingularAttribute<X, ?>> idAttribute = new HashSet<>();
    private SingularAttribute<X, ?> versionAttribute;

    public IdentifiableTypeImpl(PersistenceType persistenceType, Class<X> javaType) {
        super(persistenceType, javaType);
    }

    public void addIdAttribute(SingularAttribute<X, ?> attribute) {
        idAttribute.add(attribute);
    }

    @Override
    public <Y> SingularAttribute<? super X, Y> getId(Class<Y> aClass) {
        return getDeclaredId(aClass);
    }

    @Override
    public <Y> SingularAttribute<X, Y> getDeclaredId(Class<Y> aClass) {
        return idAttribute.stream()
                .filter(a -> aClass.isAssignableFrom(a.getJavaType()))
                .map(a -> (SingularAttribute<X, Y>) a)
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public <Y> SingularAttribute<? super X, Y> getVersion(Class<Y> aClass) {
        return getDeclaredVersion(aClass);
    }

    @Override
    public <Y> SingularAttribute<X, Y> getDeclaredVersion(Class<Y> aClass) {
        return Optional.ofNullable(versionAttribute)
                .filter(a -> aClass.isAssignableFrom(a.getJavaType()))
                .map(a -> (SingularAttribute<X, Y>) a)
                .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public IdentifiableType<? super X> getSupertype() {
        return null;
    }

    @Override
    public boolean hasSingleIdAttribute() {
        return idAttribute.size() == 1;
    }

    @Override
    public boolean hasVersionAttribute() {
        return versionAttribute != null;
    }

    @Override
    public Set<SingularAttribute<? super X, ?>> getIdClassAttributes() {
        return Collections.unmodifiableSet(idAttribute);
    }

    @Override
    public Type<?> getIdType() {
        return idAttribute.stream().findFirst()
                .map(SingularAttribute::getType)
                .orElse(null);
    }
}
