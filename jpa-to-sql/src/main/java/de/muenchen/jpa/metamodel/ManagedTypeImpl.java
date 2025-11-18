package de.muenchen.jpa.metamodel;

import jakarta.persistence.metamodel.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ManagedTypeImpl<X> extends TypeImpl<X> implements ManagedType<X> {

    private static final Predicate<Attribute<?, ?>> IS_SINGULAR_ATTRIBUTE = a -> a instanceof SingularAttribute<?,?>;
    private static final Predicate<Attribute<?, ?>> IS_COLLECTION_ATTRIBUTE = a -> a instanceof CollectionAttribute<?,?>;
    private static final Predicate<Attribute<?, ?>> IS_MAP_ATTRIBUTE = a -> a instanceof MapAttribute<?,?,?>;
    private static final Predicate<Attribute<?, ?>> IS_SET_ATTRIBUTE = a -> a instanceof SetAttribute<?,?>;
    private static final Predicate<Attribute<?, ?>> IS_LIST_ATTRIBUTE = a -> a instanceof ListAttribute<?,?>;
    private static final Predicate<Attribute<?, ?>> IS_PLURAL_ATTRIBUTE = a -> a instanceof PluralAttribute<?,?, ?>;

    private final Map<String, Attribute<X, ?>> attributes = new HashMap<>();

    public ManagedTypeImpl(PersistenceType persistenceType, Class<X> javaType) {
        super(persistenceType, javaType);
    }

    public void addAttribute(Attribute<X, ?> attr) {
        attributes.put(attr.getName(), attr);
    }

    @Override
    public Set<Attribute<? super X, ?>> getAttributes() {
        return Collections.unmodifiableSet(getDeclaredAttributes());
    }

    @Override
    public Set<Attribute<X, ?>> getDeclaredAttributes() {
        return Set.copyOf(attributes.values());
    }

    @Override
    public <Y> SingularAttribute<? super X, Y> getSingularAttribute(String s, Class<Y> aClass) {
        return getDeclaredSingularAttribute(s, aClass);
    }

    @Override
    public <Y> SingularAttribute<X, Y> getDeclaredSingularAttribute(String s, Class<Y> aClass) {
        return Optional.ofNullable(attributes.get(s))
                .filter(a -> aClass.isAssignableFrom(a.getJavaType()))
                .filter(IS_SINGULAR_ATTRIBUTE)
                .map(a -> (SingularAttribute<X, Y>) a)
                .orElse(null);
    }

    @Override
    public Set<SingularAttribute<? super X, ?>> getSingularAttributes() {
        return Collections.unmodifiableSet(getDeclaredSingularAttributes());
    }

    @Override
    public Set<SingularAttribute<X, ?>> getDeclaredSingularAttributes() {
        return attributes.values().stream()
                .filter(IS_SINGULAR_ATTRIBUTE)
                .map(a -> (SingularAttribute<X, ?>) a)
                .collect(Collectors.toSet());
    }

    @Override
    public <E> CollectionAttribute<? super X, E> getCollection(String s, Class<E> aClass) {
        return getDeclaredCollection(s, aClass);
    }

    @Override
    public <E> CollectionAttribute<X, E> getDeclaredCollection(String s, Class<E> aClass) {
        return null;
    }

    @Override
    public <E> SetAttribute<? super X, E> getSet(String s, Class<E> aClass) {
        return getDeclaredSet(s,aClass);
    }

    @Override
    public <E> SetAttribute<X, E> getDeclaredSet(String s, Class<E> aClass) {
        return null;
    }

    @Override
    public <E> ListAttribute<? super X, E> getList(String s, Class<E> aClass) {
        return getDeclaredList(s, aClass);
    }

    @Override
    public <E> ListAttribute<X, E> getDeclaredList(String s, Class<E> aClass) {
        return null;
    }

    @Override
    public <K, V> MapAttribute<? super X, K, V> getMap(String s, Class<K> aClass, Class<V> aClass1) {
        return getDeclaredMap(s, aClass, aClass1);
    }

    @Override
    public <K, V> MapAttribute<X, K, V> getDeclaredMap(String s, Class<K> aClass, Class<V> aClass1) {
        return null;
    }

    @Override
    public Set<PluralAttribute<? super X, ?, ?>> getPluralAttributes() {
        return Collections.unmodifiableSet(getDeclaredPluralAttributes());
    }

    @Override
    public Set<PluralAttribute<X, ?, ?>> getDeclaredPluralAttributes() {
        return attributes.values().stream()
                .filter(IS_PLURAL_ATTRIBUTE)
                .map(a -> (PluralAttribute<X, ?, ?>) a)
                .collect(Collectors.toSet());
    }

    @Override
    public Attribute<? super X, ?> getAttribute(String s) {
        return getDeclaredAttribute(s);
    }

    @Override
    public Attribute<X, ?> getDeclaredAttribute(String s) {
        return attributes.get(s);
    }

    @Override
    public SingularAttribute<? super X, ?> getSingularAttribute(String s) {
        return getDeclaredSingularAttribute(s);
    }

    @Override
    public SingularAttribute<X, ?> getDeclaredSingularAttribute(String s) {
        return Optional.ofNullable(attributes.get(s))
                .filter(IS_SINGULAR_ATTRIBUTE)
                .map(a -> (SingularAttribute<X, ?>) a)
                .orElse(null);
    }

    @Override
    public CollectionAttribute<? super X, ?> getCollection(String s) {
        return getDeclaredCollection(s);
    }

    @Override
    public CollectionAttribute<X, ?> getDeclaredCollection(String s) {
        return Optional.ofNullable(attributes.get(s))
                .filter(IS_COLLECTION_ATTRIBUTE)
                .map(a -> (CollectionAttribute<X, ?>) a)
                .orElse(null);
    }

    @Override
    public SetAttribute<? super X, ?> getSet(String s) {
        return getDeclaredSet(s);
    }

    @Override
    public SetAttribute<X, ?> getDeclaredSet(String s) {
        return Optional.ofNullable(attributes.get(s))
                .filter(IS_SET_ATTRIBUTE)
                .map(a -> (SetAttribute<X, ?>) a)
                .orElse(null);
    }

    @Override
    public ListAttribute<? super X, ?> getList(String s) {
        return getDeclaredList(s);
    }

    @Override
    public ListAttribute<X, ?> getDeclaredList(String s) {
        return Optional.ofNullable(attributes.get(s))
                .filter(IS_LIST_ATTRIBUTE)
                .map(a -> (ListAttribute<X, ?>) a)
                .orElse(null);
    }

    @Override
    public MapAttribute<? super X, ?, ?> getMap(String s) {
        return getDeclaredMap(s);
    }

    @Override
    public MapAttribute<X, ?, ?> getDeclaredMap(String s) {
        return Optional.ofNullable(attributes.get(s))
                .filter(IS_MAP_ATTRIBUTE)
                .map(a -> (MapAttribute<X, ?, ?>) a)
                .orElse(null);
    }
}
