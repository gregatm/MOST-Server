package de.muenchen.jpa.metamodel;

import jakarta.persistence.metamodel.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DynamicMetamodel implements Metamodel {

    @Getter
    @RequiredArgsConstructor
    public static class ProxyType<X> implements ManagedType<X> {

        private final Metamodel model;
        private final Class<X> clazz;

        private ManagedType<X> loaded;

        protected void load() {
            if (loaded == null) {
                loaded = model.managedType(clazz);
            }
        }

        @Override
        public Set<Attribute<? super X, ?>> getAttributes() {
            load();
            return loaded.getAttributes();
        }

        @Override
        public Set<Attribute<X, ?>> getDeclaredAttributes() {
            load();
            return loaded.getDeclaredAttributes();
        }

        @Override
        public <Y> SingularAttribute<? super X, Y> getSingularAttribute(String s, Class<Y> aClass) {
            load();
            return loaded.getSingularAttribute(s, aClass);
        }

        @Override
        public <Y> SingularAttribute<X, Y> getDeclaredSingularAttribute(String s, Class<Y> aClass) {
            load();
            return loaded.getDeclaredSingularAttribute(s, aClass);
        }

        @Override
        public Set<SingularAttribute<? super X, ?>> getSingularAttributes() {
            load();
            return loaded.getSingularAttributes();
        }

        @Override
        public Set<SingularAttribute<X, ?>> getDeclaredSingularAttributes() {
            load();
            return loaded.getDeclaredSingularAttributes();
        }

        @Override
        public <E> CollectionAttribute<? super X, E> getCollection(String s, Class<E> aClass) {
            load();
            return loaded.getCollection(s,aClass);
        }

        @Override
        public <E> CollectionAttribute<X, E> getDeclaredCollection(String s, Class<E> aClass) {
            load();
            return loaded.getDeclaredCollection(s, aClass);
        }

        @Override
        public <E> SetAttribute<? super X, E> getSet(String s, Class<E> aClass) {
            load();
            return loaded.getSet(s, aClass);
        }

        @Override
        public <E> SetAttribute<X, E> getDeclaredSet(String s, Class<E> aClass) {
            load();
            return loaded.getDeclaredSet(s, aClass);
        }

        @Override
        public <E> ListAttribute<? super X, E> getList(String s, Class<E> aClass) {
            load();
            return loaded.getList(s, aClass);
        }

        @Override
        public <E> ListAttribute<X, E> getDeclaredList(String s, Class<E> aClass) {
            load();
            return loaded.getDeclaredList(s, aClass);
        }

        @Override
        public <K, V> MapAttribute<? super X, K, V> getMap(String s, Class<K> aClass, Class<V> aClass1) {
            load();
            return loaded.getMap(s, aClass, aClass1);
        }

        @Override
        public <K, V> MapAttribute<X, K, V> getDeclaredMap(String s, Class<K> aClass, Class<V> aClass1) {
            load();
            return loaded.getDeclaredMap(s, aClass, aClass1);
        }

        @Override
        public Set<PluralAttribute<? super X, ?, ?>> getPluralAttributes() {
            load();
            return loaded.getPluralAttributes();
        }

        @Override
        public Set<PluralAttribute<X, ?, ?>> getDeclaredPluralAttributes() {
            load();
            return loaded.getDeclaredPluralAttributes();
        }

        @Override
        public Attribute<? super X, ?> getAttribute(String s) {
            load();
            return loaded.getAttribute(s);
        }

        @Override
        public Attribute<X, ?> getDeclaredAttribute(String s) {
            load();
            return loaded.getDeclaredAttribute(s);
        }

        @Override
        public SingularAttribute<? super X, ?> getSingularAttribute(String s) {
            load();
            return loaded.getSingularAttribute(s);
        }

        @Override
        public SingularAttribute<X, ?> getDeclaredSingularAttribute(String s) {
            load();
            return loaded.getDeclaredSingularAttribute(s);
        }

        @Override
        public CollectionAttribute<? super X, ?> getCollection(String s) {
            load();
            return loaded.getCollection(s);
        }

        @Override
        public CollectionAttribute<X, ?> getDeclaredCollection(String s) {
            load();
            return loaded.getDeclaredCollection(s);
        }

        @Override
        public SetAttribute<? super X, ?> getSet(String s) {
            load();
            return loaded.getSet(s);
        }

        @Override
        public SetAttribute<X, ?> getDeclaredSet(String s) {
            load();
            return loaded.getDeclaredSet(s);
        }

        @Override
        public ListAttribute<? super X, ?> getList(String s) {
            load();
            return loaded.getList(s);
        }

        @Override
        public ListAttribute<X, ?> getDeclaredList(String s) {
            load();
            return loaded.getDeclaredList(s);
        }

        @Override
        public MapAttribute<? super X, ?, ?> getMap(String s) {
            load();
            return loaded.getMap(s);
        }

        @Override
        public MapAttribute<X, ?, ?> getDeclaredMap(String s) {
            load();
            return loaded.getDeclaredMap(s);
        }

        @Override
        public PersistenceType getPersistenceType() {
            load();
            return loaded.getPersistenceType();
        }

        @Override
        public Class<X> getJavaType() {
            return clazz;
        }
    }

    private final Set<ManagedType<?>> managedTypes = new HashSet<>();

    public void addType(ManagedType<?> type) {
        managedTypes.add(type);
    }

    @Override
    public EntityType<?> entity(String s) {
        return getEntities().stream()
                .filter(e -> e.getName().equals(s))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public <X> EntityType<X> entity(Class<X> aClass) {
        return getEntities().stream()
                .filter(e -> aClass.isAssignableFrom(e.getJavaType()))
                .map(e -> (EntityType<X>) e)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public <X> ManagedType<X> managedType(Class<X> aClass) {
        return managedTypes.stream()
                .filter(e -> aClass.isAssignableFrom(e.getJavaType()))
                .map(e -> (ManagedType<X>) e)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public <X> EmbeddableTypeImpl<X> embeddable(Class<X> aClass) {
        return getEmbeddables().stream()
                .filter(e -> aClass.isAssignableFrom(e.getJavaType()))
                .map(e -> (EmbeddableTypeImpl<X>) e)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public Set<ManagedType<?>> getManagedTypes() {
        return Set.copyOf(managedTypes);
    }

    @Override
    public Set<EntityType<?>> getEntities() {
        return managedTypes.stream()
                .filter(t -> t.getPersistenceType() == Type.PersistenceType.ENTITY)
                .map(t -> (EntityType<?>) t)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<EmbeddableType<?>> getEmbeddables() {
        return Set.of();
    }
}
