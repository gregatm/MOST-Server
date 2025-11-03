package de.muenchen.mostserver.odata

import de.muenchen.mostserver.config.EdmProvider
import de.muenchen.mostserver.olingo.CsdlNavigationPropertyBuilder
import jakarta.persistence.Column
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToOne
import org.apache.olingo.commons.api.data.Entity
import org.apache.olingo.commons.api.data.EntityCollection
import org.apache.olingo.commons.api.data.Link
import org.apache.olingo.commons.api.data.Property
import org.apache.olingo.commons.api.data.ValueType
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind
import org.apache.olingo.commons.api.edm.FullQualifiedName
import org.apache.olingo.commons.api.edm.geo.Geospatial
import org.apache.olingo.commons.api.edm.provider.CsdlEdmProvider
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty
import org.apache.olingo.commons.api.edm.provider.CsdlProperty
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef
import org.apache.olingo.commons.api.edm.provider.CsdlSchema
import org.springframework.data.annotation.Id
import java.lang.reflect.Field
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID
import kotlin.reflect.KClassifier
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class EdmEntityTypeFromClass {

}

fun getEdmEntityProviderAnnotation(clazz: Class<*>): EdmEntityProvider {
    val annotation = clazz.annotations.find { a -> a is EdmEntityProvider } as EdmEntityProvider?
    if (annotation == null) {
        throw RuntimeException("Class $clazz has no EdmEntityProvider Annotation")
    }

    return annotation
}

fun getEdmEntityName(annotation: EdmEntityProvider, clazz: Class<*>): String {
    return annotation.type.ifEmpty { clazz.simpleName }
}

fun mapTypeIsCollection(field: Field): Boolean {
    val annotation = field.annotations.find { it is EdmEntityAsType } as EdmEntityAsType?
    return annotation?.isCollection ?: false
}

fun mapType(field: Field) : FullQualifiedName {
    val annotation = field.annotations.find { it is EdmEntityAsType } as EdmEntityAsType?
    val clazz = annotation?.value ?: field.type.kotlin
    return when (clazz) {
        Int::class -> EdmPrimitiveTypeKind.Int32.fullQualifiedName
        String::class -> EdmPrimitiveTypeKind.String.fullQualifiedName
        UUID::class -> EdmPrimitiveTypeKind.Guid.fullQualifiedName
        LocalDateTime::class -> EdmPrimitiveTypeKind.DateTimeOffset.fullQualifiedName
        Geospatial::class -> EdmPrimitiveTypeKind.GeometryPolygon.fullQualifiedName
        else -> {
            val annotation = getEdmEntityProviderAnnotation(clazz.java)
            val name = getEdmEntityName(annotation, clazz.java)
            FullQualifiedName(annotation.namespace, name)
        }
    }
}

fun mapToManyNavigationalProperty(field: Field): CsdlNavigationProperty {
    val clazz = (field.declaredAnnotations.first{
        it is EdmEntityAsType
    } as EdmEntityAsType).value.java
    return CsdlNavigationPropertyBuilder()
        .name(getEntitySetNameFromEntity(clazz))
        .type(mapType(field))
        .nullable(true)
        .collection(true)
        .build()
}

fun mapToOneNavigationalProperty(field: Field, annotation: Annotation): CsdlNavigationProperty {
    val optional = when (annotation) {
        is ManyToOne -> annotation.optional
        is OneToOne -> annotation.optional
        else -> throw RuntimeException("Annotation")
    }
    val clazz = field.type

    return CsdlNavigationPropertyBuilder()
        .name(getEdmEntityName(getEdmEntityProviderAnnotation(clazz), clazz))
        .type(mapType(field))
        .nullable(optional)
        .collection(false)
        .build()
}

fun getIdFieldName(clazz: Class<*>): List<String> {
    return clazz.declaredFields
        .filter { it.getAnnotation(Id::class.java) != null }
        .map { f ->
            val a = f.getAnnotation(Column::class.java)
            a?.name ?: f.name
        }
}

fun getEntityTypeFromClass(clazz: Class<*>): R2DbcEntityType {
    val annotation = getEdmEntityProviderAnnotation(clazz)

    val entity = R2DbcEntityType()
    entity.name = getEdmEntityName(annotation, clazz)
    entity.typeClass = clazz

    val properties = clazz.declaredFields
        .filter { f -> f.annotations.none { it is EdmEntityEntityExclude } }
        .filter { f -> f.annotations.none{  a ->
            a is ManyToOne || a is OneToMany || a is OneToOne || a is ManyToMany}}
        .map { f -> mapType(f)
            CsdlProperty().setName(f.name).setType(mapType(f)).setCollection(mapTypeIsCollection(f))
        }
        .toList()

    val id = getIdFieldName(clazz)
        .map {
            val id = CsdlPropertyRef()
            id.name = it
            id
        }
        .take(1)
        .toList()

    val navs = clazz.declaredFields
        .map {
            Pair(it, it.annotations.firstOrNull { a ->
                a is ManyToOne || a is OneToMany || a is OneToOne || a is ManyToMany
            })
        }
        .filter { it.second != null }
        .mapNotNull {
            when (it.second!!) {
                is ManyToOne, is OneToOne -> mapToOneNavigationalProperty(it.first, it.second!!)
                is ManyToMany, is OneToMany -> mapToManyNavigationalProperty(it.first)
                else -> null
            }
        }
        .onEach { a -> a.partner = entity.name }

    entity.properties = properties
    entity.key = id
    entity.navigationProperties = navs
    return entity
}

fun getEntitySetNameFromEntity(clazz: Class<*>): String {
    val annotation = getEdmEntityProviderAnnotation(clazz)
    val c = annotation.type.ifEmpty { clazz.name }
    return annotation.typeSet.ifEmpty { "${c}s" }
}

fun getEntitySetFromEntity(clazz: Class<*>, entity: CsdlEntityType): CsdlEntitySet {
    val annotation = getEdmEntityProviderAnnotation(clazz)
    val set = CsdlEntitySet()
    set.setType(FullQualifiedName(annotation.namespace, entity.name))
    set.name = annotation.typeSet.ifEmpty { "${entity.name}s" }
    return set
}

fun getContainerFromEntity(annotation: EdmEntityProvider): CsdlEntityContainer {
    val container = CsdlEntityContainer()
    container.name = annotation.container
    container.entitySets = ArrayList()
    return container
}

fun getSchemasFromEntity(annotation: EdmEntityProvider, entity: CsdlEntityType,container: CsdlEntityContainer): CsdlSchema {
    val schema = CsdlSchema()
    schema.namespace = annotation.namespace

    schema.entityTypes = listOf(entity)

    schema.entityContainer = container

    return schema
}

fun createFromClass(clazzes: List<Class<*>>): EdmEntityProviderGenerated {
    var namespace: String? = null
    val entityMap = HashMap<String, R2DbcEntityType>()
    val schemas = ArrayList<CsdlSchema>()
    val entitySetMap = HashMap<String, CsdlEntitySet>()
    var container: CsdlEntityContainer? = null
    for (clazz in clazzes) {
        val annotation = getEdmEntityProviderAnnotation(clazz)
        if (namespace == null) {
            namespace = annotation.namespace
        } else if (namespace != annotation.namespace){
            throw RuntimeException("Different namespaces in one provider not supported: current = '$namespace', found = '${annotation.namespace}'")
        }

        val entity = getEntityTypeFromClass(clazz)
        entityMap[FullQualifiedName(namespace, entity.name).fullQualifiedNameAsString] = entity

        val entitySet = getEntitySetFromEntity(clazz, entity)
        entitySetMap[entitySet.name] = entitySet

        if (container == null) {
            container = getContainerFromEntity(annotation)
        } else if (container.name != annotation.container) {
            throw RuntimeException("Different container names in one provider not supported: current = '${container.name}', found = '${annotation.container}'")
        }

        schemas.add(getSchemasFromEntity(annotation, entity, container))
    }

    return EdmEntityProviderGenerated(namespace!!, entityMap, container!!, entitySetMap, schemas)
}

fun mapListValueType(obj: KClassifier?) : ValueType {
    return when(mapValueType(obj)) {
        ValueType.PRIMITIVE -> ValueType.COLLECTION_PRIMITIVE
        else -> ValueType.COLLECTION_COMPLEX
    }
}

fun mapValueType(obj: KClassifier?) : ValueType {
    return when (obj) {
        String::class, Int::class -> ValueType.PRIMITIVE
        UUID::class -> ValueType.PRIMITIVE
        else -> ValueType.COMPLEX
    }
}

fun <T> entityCollectionFromDto(dtos: Collection<T>, edm: CsdlEdmProvider, entityType: CsdlEntityType, entitySet: CsdlEntitySet): EntityCollection {
    val collection = EntityCollection()

    collection.entities.addAll(
        dtos.map { entityFromDto(it, edm, entityType, entitySet)}
    )

    return collection
}

fun <T> entityFromDto(dto: T, edm: CsdlEdmProvider, entityType: CsdlEntityType, entitySet: CsdlEntitySet): Entity? {
    if (dto == null)
        return null
    val e = Entity();

    val propertyValue = { dto: T, property: Property ->
        val clazz = dto!!::class
        val field = clazz.memberProperties.find{ f -> f.name == property.name }

        if (field != null) {
            val d = if (field.isAccessible) {
                field.getter.call(dto)
            } else {
                val m = clazz.java.methods.first { m -> m.name.lowercase() == "get${field.name}" }
                m.invoke(dto)
            }
            val valueType = if (property.isCollection) mapListValueType(field.findAnnotation<EdmEntityAsType>()!!.value)
            else mapValueType(field.returnType.classifier)
            property.setValue(valueType, d)
        }
    }

    entityType.properties
        .map {
            Property(it.type, it.name)
        }
        .map {
            propertyValue(dto, it)
            it
        }
        .forEach(e::addProperty)

    // TODO Extend proxy object with interface to inject context. Before extracting value, check if object is proxy and inject context

    e.navigationLinks.addAll(
        entityType.navigationProperties
            .map { property ->
                val link = Link()
                link.title = property.name
                val p = Property(property.type, property.name.lowercase())
                propertyValue(dto, p)
                if (p.value != null)
                    link.inlineEntitySet = entityCollectionFromDto(p.value as List<*>, edm, entityType, entitySet)
                link
            })

    val keys = entityType.key
        .map {
            val p = Property()
            p.name = it.name
            p
        }
        .map {
            propertyValue(dto, it)
            it
        }
        .toList()
    e.id = URI("${entitySet.name}(${keys.joinToString(",")})")

    return e
}