package de.muenchen.jpa.metamodel;

import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

@RequiredArgsConstructor
@Getter
public abstract class AttributeImpl<X, Y> implements Attribute<X, Y>, SqlIdentifier {

    private final PersistentAttributeType persistentAttributeType;
    private final Member member;
    private final String sqlIdentifier;
    private final ManagedType<X> declaringType;
    private final boolean isId;
    private final boolean isVersion;

    @Override
    public String getName() {
        return member.getName();
    }

    @Override
    public Class<Y> getJavaType() {
        Class<?> clazz = switch (member) {
            case Field f -> f.getType();
            case Method m -> m.getReturnType();
            default -> throw new IllegalStateException("Unexpected value: " + member);
        };

        return (Class<Y>) clazz;
    }

    @Override
    public Member getJavaMember() {
        return member;
    }

    @Override
    public boolean isAssociation() {
        return false;
    }

    @Override
    public boolean isCollection() {
        return false;
    }
}
