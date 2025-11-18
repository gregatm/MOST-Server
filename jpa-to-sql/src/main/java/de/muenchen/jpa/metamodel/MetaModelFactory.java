package de.muenchen.jpa.metamodel;

import jakarta.persistence.*;
import jakarta.persistence.metamodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.WildcardType;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MetaModelFactory {

    private Logger LOG = LoggerFactory.getLogger(MetaModelFactory.class);

    public <X> ManagedType<X> processClass(Class<X> clazz) {
        var entityMetadata = new EntityMetaData<>(clazz);

        processClassAnnotations(clazz, entityMetadata);
        processFieldAnnotations(clazz, entityMetadata);

        return switch(entityMetadata.getPersistenceType()) {
            case ENTITY -> entityFromEntityMetadata(entityMetadata);
            case EMBEDDABLE -> embeddableFromEntityMetadata(entityMetadata);
            default -> throw new IllegalArgumentException("Cannot create manged type from " + entityMetadata.getPersistenceType());
        };
    }

    public <X> void processFieldAnnotations(Class<X> clazz, EntityMetaData<X> model) {
        for (var field : clazz.getDeclaredFields()) {
            var metadata = new FieldMetaData<>(clazz, field);
            processFieldAnnotations(metadata, field);
            model.addField(metadata);
        }
    }

    public void processClassAnnotations(Class<?> clazz, EntityMetaData<?> model) {
        for (var annotation : clazz.getDeclaredAnnotations()) {
            switch (annotation) {
                case Table t:
                    processTableAnnotation(model, t);
                    break;
                case Entity e:
                    processEntityAnnotation(model, e);
                    break;
                case Embeddable e:
                    processEmbeddableAnnotation(model, e);
                    break;
                default:
                    LOG.debug("Skip annotation {} on class {}", annotation, clazz);
            }

        }
    }

    public void processFieldAnnotations(FieldMetaData<?> model, Field field) {
        for (var annotation : field.getDeclaredAnnotations()) {
            switch (annotation) {
                case Embedded e:
                    processEmbeddedAnnotation(model, field, e);
                    break;
                case ManyToMany m:
                    processManyToManyAnnotation(model, field, m);
                    break;
                case ManyToOne m:
                    processManyToOneAnnotation(model, field, m);
                    break;
                case OneToMany m:
                    processOneToManyAnnotation(model, field, m);
                    break;
                case OneToOne m:
                    processOneToOneAnnotation(model, field, m);
                    break;
                case Column c:
                    processColumnAnnotation(model, field, c);
                    break;
                case JoinColumn c:
                    processJoinColumnAnnotation(model, field, c);
                    break;
                case JoinTable c:
                    processJoinTableAnnotation(model, field, c);
                    break;
                case Id i:
                    processIdAnnotation(model, field, i);
                    break;
                case GeneratedValue g:
                    break;
                case Basic b:
                    break;
                case ForeignKey f:
                    break;
                case Transient t:
                    break;
                case Version v:
                    processVersionAnnotation(model, field, v);
                    break;
                default:
                    LOG.debug("Skip annotation {} on field {}", annotation, field);
            }
        }
    }

    public void processTableAnnotation(EntityMetaData<?> metadata, Table table) {
        if (!table.name().isEmpty()) {
            metadata.setTable(table.name());
        }
    }

    public void processEntityAnnotation(EntityMetaData<?> metadata, Entity entity) {
        metadata.setPersistenceType(Type.PersistenceType.ENTITY);
    }

    public void processEmbeddableAnnotation(EntityMetaData<?> metadata, Embeddable embeddable) {
        metadata.setPersistenceType(Type.PersistenceType.EMBEDDABLE);
    }

    private static final Set<String> VERSION_ALLOWED_TYPES;

    static {
        VERSION_ALLOWED_TYPES = Set.of(
                Integer.class.getName(),
                Short.class.getName(),
                Long.class.getName(),
                Timestamp.class.getName(),
                Instant.class.getName(),
                LocalDateTime.class.getName()
        );
    }

    public void processIdAnnotation(FieldMetaData<?> model, Field f, Id id) {
        model.setId(true);
    }

    public void processEmbeddedAnnotation(FieldMetaData<?> model, Field f, Embedded e) {
        model.setEmbeddable(true);
        model.setAttributeType(Attribute.PersistentAttributeType.EMBEDDED);
    }

    public void processColumnAnnotation(FieldMetaData<?> model, Field f, Column c) {
        if (!c.name().isEmpty()) {
            model.setSqlName(c.name());
        }
    }

    public void processJoinColumnAnnotation(FieldMetaData<?> model, Field f, JoinColumn c) {
        if (!c.name().isEmpty()) {
            model.setSqlName(c.name());
        }
    }

    public void processJoinTableAnnotation(FieldMetaData<?> model, Field f, JoinTable j) {

    }

    public void processVersionAnnotation(FieldMetaData<?> model, Field f, Version v) {
        if (!VERSION_ALLOWED_TYPES.contains(f.getType().getName())) {
            LOG.error("Field type {} not supported with version annotation", f.getType());
            throw new IllegalStateException("Type " + f.getType() + "not supported on version annotated field");
        }
        model.setVersion(true);
    }

    public void processManyToManyAnnotation(FieldMetaData<?> metaData, Field f, ManyToMany m) {
        metaData.setAttributeType(Attribute.PersistentAttributeType.MANY_TO_MANY);
    }

    public void processManyToOneAnnotation(FieldMetaData<?> metaData, Field f, ManyToOne m) {
        metaData.setAttributeType(Attribute.PersistentAttributeType.MANY_TO_ONE);
    }

    public void processOneToManyAnnotation(FieldMetaData<?> metaData, Field f, OneToMany m) {
        metaData.setAttributeType(Attribute.PersistentAttributeType.ONE_TO_MANY);
    }

    public void processOneToOneAnnotation(FieldMetaData<?> metaData, Field f, OneToOne m) {
        metaData.setAttributeType(Attribute.PersistentAttributeType.ONE_TO_ONE);
    }

    public <X> Attribute<X, ?> attributeFromFieldMetaData(ManagedType<X> declaring, FieldMetaData<X> metaData) {
        if (metaData.isTransient()) {
            return null;
        }

        var fieldClass = metaData.getField().getType();
        if (List.class.isAssignableFrom(fieldClass)) {
            return listAttributeFromFieldMetadata(declaring, metaData);
        } else if (Set.class.isAssignableFrom(fieldClass)) {
            return setAttributeFromFieldMetadata(declaring, metaData);
        } else if (Collection.class.isAssignableFrom(fieldClass)) {
            return collectionAttributeFromFieldMetadata(declaring, metaData);
        }

        return singularAttributeFromFieldMetaData(declaring, metaData);
    }

    public <X> CollectionAttribute<X, ?> collectionAttributeFromFieldMetadata(Type<X> declaring, FieldMetaData<X> metadata) {
        throw new IllegalStateException();
    }

    public <X> ListAttribute<X, ?> listAttributeFromFieldMetadata(ManagedType<X> declaring, FieldMetaData<X> metadata) {
        var tp = switch (metadata.getAttributeType()) {
            case MANY_TO_ONE, ONE_TO_ONE, MANY_TO_MANY, ONE_TO_MANY
                    -> Type.PersistenceType.ENTITY;
            case BASIC, ELEMENT_COLLECTION -> Type.PersistenceType.BASIC;

            case EMBEDDED -> Type.PersistenceType.EMBEDDABLE;
        };

        var cls = switch (((ParameterizedType) metadata.getField().getGenericType()).getActualTypeArguments()[0]) {
            case Class<?> c -> c;
            case WildcardType w -> w.getUpperBounds()[0];
            default -> throw new IllegalStateException(metadata.getField().getGenericType().toString());
        };

        var t = new TypeImpl<>(tp, (Class<?>) cls);
        return new ListAttributeImpl<>(metadata.getAttributeType(), metadata.getField(), metadata.getSqlIdentifier(), declaring, t);
    }

    public <X> SetAttribute<X, ?> setAttributeFromFieldMetadata(ManagedType<X> declaring, FieldMetaData<X> metadata) {
        throw new IllegalStateException();
    }

    public <X> SingularAttribute<X, ?> singularAttributeFromFieldMetaData(ManagedType<X> declaring, FieldMetaData<X> metaData) {
        var tp = switch (metaData.getAttributeType()) {
            case MANY_TO_ONE, ONE_TO_ONE, MANY_TO_MANY, ONE_TO_MANY
                    -> Type.PersistenceType.ENTITY;
            case BASIC, ELEMENT_COLLECTION -> Type.PersistenceType.BASIC;

            case EMBEDDED -> Type.PersistenceType.EMBEDDABLE;
        };
        var t = new TypeImpl<>(tp, metaData.getField().getType());
        return new SingularAttributeImpl<>(metaData.getAttributeType(), metaData.getField(), metaData.getSqlIdentifier(), declaring, t, metaData.isId(), metaData.isVersion(), metaData.isOptional());
    }

    public <X> EmbeddableTypeImpl<X> embeddableFromEntityMetadata(EntityMetaData<X> metadata) {
        var embeddable = new EmbeddableTypeImpl<>(metadata.getClazz());

        metadata.getFields().stream()
                .map(f -> this.attributeFromFieldMetaData(embeddable, f))
                .filter(Objects::nonNull)
                .forEach(embeddable::addAttribute);

        return embeddable;
    }

    public <X> EntityType<X> entityFromEntityMetadata(EntityMetaData<X> metaData) {
        var entity = new EntityTypeImpl<>(metaData.getClazz(), metaData.getEntityName());

        metaData.getFields().stream()
                .map(f -> this.attributeFromFieldMetaData(entity, f))
                .filter(Objects::nonNull)
                .forEach(entity::addAttribute);

        entity.getDeclaredSingularAttributes()
                .stream()
                .filter(SingularAttribute::isId)
                .forEach(entity::addIdAttribute);

        entity.getDeclaredSingularAttributes()
                .stream()
                .filter(SingularAttribute::isVersion)
                .findFirst()
                .ifPresent(entity::setVersionAttribute);

        return entity;
    }
}
