package de.muenchen.mostserver.services

import de.muenchen.jpa.JpaSqlInsertBuilder
import de.muenchen.jpa.JpaSqlUpdateBuilder
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.core.uri.parser.Parser
import de.muenchen.jpa.criteria.AbstractJpaExpressionFactory
import de.muenchen.mostserver.odata.R2DbcEntityType
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.Result
import io.r2dbc.spi.Statement
import jakarta.persistence.Parameter
import jakarta.persistence.criteria.AbstractQuery
import jakarta.persistence.criteria.CommonAbstractCriteria
import jakarta.persistence.criteria.CriteriaDelete
import jakarta.persistence.criteria.CriteriaUpdate
import jakarta.persistence.criteria.ParameterExpression
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Root
import jakarta.persistence.criteria.Subquery
import org.apache.olingo.commons.api.data.Entity
import org.apache.olingo.commons.api.data.Property
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider
import org.apache.olingo.server.api.uri.UriParameter
import org.apache.olingo.server.api.uri.UriResourceEntitySet
import org.apache.olingo.server.api.uri.UriResourceKind
import org.apache.olingo.server.api.uri.UriResourceNavigation
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.net.URI
import java.util.UUID
import kotlin.collections.set
import kotlin.jvm.javaClass

class ODataService(
    val edm: Edm,
    val csdl: CsdlAbstractEdmProvider,
    val factory: AbstractJpaExpressionFactory,
    val db: ConnectionFactory) {

    fun post(uri: URI, entity: Entity): Mono<*> {
        return processUri(uri, entity, false)
    }

    class PathParsingContext(val processId: Boolean,
                             val previousType: R2DbcEntityType?,
                             val previousKey : List<UriParameter>?,
                             val previousQuery: CommonAbstractCriteria?,
                             val map : MutableMap<Parameter<*>, Any?> = HashMap())

    fun put(uri: URI, entity: Entity): Mono<*> {
        return processUri(uri, entity, true)
    }

    fun processUri(url: URI, entity: Entity, update: Boolean): Mono<*> {
        val parser = Parser(edm, OData.newInstance())
        val info = parser.parseUri(url.path, url.query, null, "");

        var ctx = PathParsingContext(update, null, null, null)

        info.uriResourceParts
            .asReversed()
            .forEach {path ->
                ctx = when (path.kind) {
                    UriResourceKind.navigationProperty -> processNavigationProperty(path as UriResourceNavigation, entity, ctx)
                    UriResourceKind.entitySet -> processEntitySet(path as UriResourceEntitySet, entity, ctx)
                    else -> throw IllegalArgumentException("Unknown path kind: " + path.kind.toString())
                }
            }

        var root = ctx.previousQuery
        while (root is Subquery<*>) {
            root = root.containingQuery
        }
        return executeQuery(root as CriteriaUpdate<*>, ctx.map, update).toMono()
    }

    fun processEntitySet(property: UriResourceEntitySet, entity: Entity, context: PathParsingContext): PathParsingContext {
        val navType = property.entityType
        val navCsdlType = csdl.getEntityType(navType.fullQualifiedName) as R2DbcEntityType

        return processPathSegment(navCsdlType, entity, property.keyPredicates, context)
    }

    fun processNavigationProperty(property: UriResourceNavigation, entity: Entity, context: PathParsingContext): PathParsingContext {
        val entityType = property.property.type
        val csdlType = csdl.getEntityType(entityType.fullQualifiedName) as R2DbcEntityType

        return processPathSegment(csdlType, entity, property.keyPredicates, context)
    }

    fun processPathSegment(csdlType: R2DbcEntityType, entity: Entity, keyPredicates: List<UriParameter>?, context: PathParsingContext): PathParsingContext {
        val clazz = csdlType.typeClass
        val q: CommonAbstractCriteria
        var r: Root<*>;
        if (context.previousQuery == null) {
            q = postCriteriaUpdate(clazz)
            r = q.root
            entity.properties
                .forEach { prop ->
                    if (csdlType.key.stream().map { it.name }.filter { it == prop.name }.findAny().isEmpty) {
                        val tmp = addPropertyToQuery(prop, q, typeToClass(prop))
                        context.map[tmp.first] = tmp.second
                    }
                }
        } else {
            q = context.previousQuery.subquery(clazz)
            r = q.from(clazz)

            if (context.previousKey != null) {
                val sj = r.join(context.previousType!!.typeClass)
                context.previousKey.forEach { jk ->
                    val p = factory.parameter(jk.text.javaClass)
                    context.map[p] = jk.text
                    q.where(q.restriction, factory.equal(sj.get<Any>(jk.name), p))
                }
            }

            combineWheres(context.previousQuery, q)
        }

        if (context.processId) {
            keyPredicates!!.forEach { id ->
                val p: ParameterExpression<Any> = factory.parameter(id.text.javaClass)
                context.map[p] = id.text;
                when (q) {
                    is AbstractQuery<*> -> q.where(q.restriction, factory.equal(r.get<Any>(id.name), p))
                    is CriteriaUpdate<*> -> q.where(q.restriction, factory.equal(r.get<Any>(id.name), p))
                    else -> throw IllegalArgumentException()
                }
            }
        } else if (!(keyPredicates == null || keyPredicates.isEmpty())) {
            throw IllegalArgumentException()
        }

        return PathParsingContext(true, csdlType, keyPredicates, q, context.map)
    }

    fun combineWheres(query: CommonAbstractCriteria, subquery: Subquery<*>) {
        when (query) {
            is AbstractQuery<*> -> query.where(query.restriction, factory.exists(subquery))
            is CriteriaUpdate<*> -> query.where(query.restriction, factory.exists(subquery))
            is CriteriaDelete<*> -> query.where(query.restriction, factory.exists(subquery))
            else -> throw NotImplementedError()
        }
    }

    fun executeQuery(query: CriteriaUpdate<*>, args: Map<Parameter<*>, *>, update: Boolean): Flux<Result> {
        return Mono.from(db.create())
            .map { conn ->
                val sb = StringBuilder()
                val params = ArrayList<Parameter<*>>()
                if (update) {
                    JpaSqlUpdateBuilder.build(sb, query, params)
                } else {
                    JpaSqlInsertBuilder.build(sb, query, params)
                }
                val sql = sb.toString()
                val s = conn.createStatement(sql)
                for (i in params.indices) {
                    val param = params[i]
                    val arg = args.getOrDefault(params[i], null)
                    if (arg == null) {
                        s.bindNull(i, param.parameterType)
                    } else {
                        s.bind(i, arg)
                    }
                }
                s
            }
            .flatMapMany(Statement::execute)
    }

    fun typeToClass(property: Property): Class<*> {
        return when (property.type) {
            EdmPrimitiveTypeKind.String.fullQualifiedName.toString() -> String::class.java
            EdmPrimitiveTypeKind.Guid.fullQualifiedName.toString() -> UUID::class.java
            EdmPrimitiveTypeKind.Int32.fullQualifiedName.toString() -> Integer::class.java
            else -> String::class.java
        }
    }

    fun <T, Y> addPropertyToQuery(property: Property, query: CriteriaUpdate<T>, type: Class<Y>): Pair<Parameter<Y>, Y> {
        val p = factory.parameter(type)
        query.set(typeToAttribute(query, type, property.name), p)
        return Pair(p, type.cast(property.value))
    }

    fun <T> typeToAttribute(query: CriteriaUpdate<*>, clazz: Class<T>, property: String): Path<T> {
        return query.root.get(property)
    }

    fun <T> postCriteriaUpdate(clazz: Class<T>): CriteriaUpdate<T> {
        val q = factory.createCriteriaUpdate(clazz);
        q.from(clazz)
        return q
    }
}