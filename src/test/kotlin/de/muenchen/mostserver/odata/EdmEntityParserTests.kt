package de.muenchen.mostserver.odata

import de.muenchen.mostserver.controller.dto.Thing
import de.muenchen.mostserver.data.AndPredicate
import de.muenchen.mostserver.data.EqPredicate
import de.muenchen.mostserver.data.Predicate
import de.muenchen.mostserver.data.QueryBuilder
import de.muenchen.mostserver.data.SqlField
import de.muenchen.mostserver.data.SqlLiteral
import de.muenchen.mostserver.data.dao.DatastreamDao
import de.muenchen.mostserver.data.dao.SensorDao
import de.muenchen.mostserver.data.dao.ThingDao
import io.micrometer.core.instrument.util.IOUtils
import io.r2dbc.spi.Batch
import io.r2dbc.spi.Option
import org.apache.olingo.commons.api.constants.Constantsv01
import org.apache.olingo.commons.api.data.ContextURL
import org.apache.olingo.commons.api.edm.FullQualifiedName
import org.apache.olingo.commons.api.edmx.EdmxReference
import org.apache.olingo.commons.api.format.ContentType
import org.apache.olingo.commons.core.edm.EdmEntityTypeImpl
import org.apache.olingo.commons.core.edm.EdmProviderImpl
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.serializer.EntitySerializerOptions
import org.apache.olingo.server.api.uri.UriParameter
import org.apache.olingo.server.api.uri.UriResourceEntitySet
import org.apache.olingo.server.api.uri.UriResourceKind
import org.apache.olingo.server.api.uri.UriResourceNavigation
import org.apache.olingo.server.core.ServiceMetadataImpl
import org.apache.olingo.server.core.serializer.json.ODataJsonSerializer
import org.apache.olingo.server.core.uri.parser.ExpandParser
import org.apache.olingo.server.core.uri.parser.Parser
import org.apache.olingo.server.core.uri.parser.SelectParser
import org.apache.olingo.server.core.uri.parser.UriTokenizer
import org.junit.jupiter.api.Test
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy
import org.springframework.data.r2dbc.dialect.DialectResolver
import org.springframework.data.relational.core.sql.Conditions
import org.springframework.data.relational.core.sql.Join.JoinType
import org.springframework.data.relational.core.sql.StatementBuilder
import org.springframework.data.relational.core.sql.Table
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import java.net.URI
import java.util.Optional
import java.util.UUID
import java.util.function.Function
import kotlin.test.assertContains
import kotlin.test.assertEquals

class EdmEntityParserTests {


    @Test
    fun parseThing() {

        val provider = createFromClass(listOf(ThingDao::class.java))
        assertEquals("Thing", provider.getEntityType(FullQualifiedName("Odata.MOSTServer", "Thing"))!!.name)

    }

    @Test
    fun parseSelect() {
        val selectStatement = "id,name"
        val provider = createFromClass(listOf(ThingDao::class.java, SensorDao::class.java, DatastreamDao::class.java))
        val edm = EdmProviderImpl(provider)
        val parser = SelectParser(edm)
        val csdlEntity = getEntityTypeFromClass(ThingDao::class.java)
        val edmEntity = EdmEntityTypeImpl(edm, FullQualifiedName("Odata.MOSTServer", csdlEntity.name), csdlEntity)

        val select = parser.parse(UriTokenizer(selectStatement), edmEntity, true)
        assertContains(select.selectItems.map { it.resourcePath.uriResourceParts.last().segmentValue }, "id")
        assertContains(select.selectItems.map { it.resourcePath.uriResourceParts.last().segmentValue }, "name")
    }

    @Test
    fun serializeThing() {

        val t = Thing(UUID.randomUUID(), "ThingName")

        val provider = createFromClass(listOf(Thing::class.java))
        val edm = EdmProviderImpl(provider)

        val serializer = ODataJsonSerializer(ContentType.JSON, Constantsv01())

        val entityType = provider.getEntityType(
            FullQualifiedName("Odata.MOSTServer", "Thing"))!!
        val edmEntity = EdmEntityTypeImpl(edm, FullQualifiedName("Odata.MOSTServer", entityType.name), entityType)
        val entity = entityFromDto(t, provider, entityType,
            provider.getEntitySet(
                FullQualifiedName("Odata.MOSTServer", "Container"),"Things")!!)

        val options = EntitySerializerOptions.with().contextURL(ContextURL.with().type(edmEntity).build()).build()

        val edmx = listOf(EdmxReference(URI.create("http://example.com")))
        val serviceMetadata = ServiceMetadataImpl(provider, edmx, null)

        val result = serializer.entity(serviceMetadata,edmEntity, entity, options)
        val stringo = IOUtils.toString(result.content)
    }

    fun <T, U> IF(input: T, func: java.util.function.Predicate<T>, t: U, f: U) : U {
        return if (func.test(input)) t else f
    }

    @Test
    fun complexQuery() {
        val provider = createFromClass(listOf(ThingDao::class.java, SensorDao::class.java, DatastreamDao::class.java))
        val edm = EdmProviderImpl(provider)
        val query = "OData/Odata.MOSTServer/Things(${UUID.randomUUID()})/Datastreams?\$expand=Sensor(\$select=name&\$filter=startswith(name, 'test'))&\$filter=contains(name, 'test') and name eq 'test 1'&\$orderby=Datasreams/\$count&\$top=10&\$skip=10&\$count=true"

        val parser = Parser(edm, OData.newInstance())
        val info = parser.parseUri("Things(${UUID.randomUUID()})/Datastreams", "\$expand=Sensor(\$select=name;\$filter=startswith(name, 'test'))&\$filter=contains(name, 'test') and name eq 'test 1'&\$orderby=name%20desc&\$top=10&\$skip=10&\$count=true", null, "/api/v1")

        val factory = ConnectionFactoryBuilder.withUrl("r2dbc:postgres://postgres:postgres@localhost/most").build()
        val mapContext = DaoContext()
        val dataAccessStrategy = DefaultReactiveDataAccessStrategy(DialectResolver.getDialect(factory), listOf(
            UUIDToProxyObjectConverter(setOf(DatastreamDao::class.java, ThingDao::class.java), mapContext)))
        val thingMapper = dataAccessStrategy.getRowMapper(ThingDao::class.java)
        val sensorMapper = dataAccessStrategy.getRowMapper(SensorDao::class.java)

        val builders = info.uriResourceParts
            .map { urlPathTo(it, Optional.ofNullable(IF(it, {i -> i == info.uriResourceParts.last()}, info,null)), provider) }
        var qb: QueryBuilder<*> = builders.first()
        for (q in builders.drop(1)) {
            q.join(qb, JoinType.JOIN)
            qb = q
        }

        val statement = qb.build()

        val batch = Mono.from(factory.create())
            .map { connection ->
                val b = connection.createBatch()
                b.add("select t.id, t.acl_id, t.name, ARRAY_REMOVE(ARRAY_AGG(s.id ORDER BY s.name), NULL) as datastreams from thing t left join datastream s on t.id = s.thing_id GROUP BY t.id, t.acl_id, t.name;")
                    .add("SELECT * FROM sensor;")
                    .add("SELECT id, sensor_id FROM (SELECT id, sensor_id, ROW_NUMBER() OVER (PARTITION BY sensor_id) as n FROM datastream) WHERE n > 1 AND n < 3;")
            }
            .flatMapMany(Batch::execute)
            .map{ t ->
                t.map{row, meta ->
                    if (meta.contains("acl_id")) {
                        val d = row.get("datastreams")
                        thingMapper.apply(row, meta)
                    }
                    else sensorMapper.apply(row, meta)
                }.toFlux()
            }
            .map{
                t -> t.collectList()
            }
            .flatMap{t -> t.toFlux() }
            .collectList()
            .block()

    }

    @Test
    fun applyProxy() {
        val dao = SensorDao(UUID.randomUUID())
        val context = DaoContext();
        val loaded = SensorDao(dao.id, "manufacturer", "sensorname", null)
        context.addParameters(loaded)
        val proxy = createProxy(dao, context)
        println(proxy.name)
    }

    @Test
    fun serialize() {

        val context = DaoContext()
        val d = DatastreamDao(UUID.randomUUID())
        val tf = ThingDao(UUID.randomUUID())
        val dp = createProxy(d, context)
        val tp = createProxy(tf, context)
        val dc = DatastreamDao(d.id, "DatastreamName", "DatastreamDescription", null, null, null, null, tp)
        val t = ThingDao(tf.id, 0, "ThingName", listOf(dp))

        context.addParameters(dc)
        context.addParameters(t)


        val provider = createFromClass(listOf(ThingDao::class.java, DatastreamDao::class.java, SensorDao::class.java))
        val edm = EdmProviderImpl(provider)

        val serializer = ODataJsonSerializer(ContentType.JSON, Constantsv01())

        val entityType = provider.getEntityType(
            FullQualifiedName("Odata.MOSTServer", "Thing"))!!
        val edmEntity = EdmEntityTypeImpl(edm, FullQualifiedName("Odata.MOSTServer", entityType.name), entityType)
        val entity = entityFromDto(t, provider, entityType,
            provider.getEntitySet(
                FullQualifiedName("Odata.MOSTServer", "Container"),"Things")!!)

        val selectOption = SelectParser(edm).parse(UriTokenizer("*"), edmEntity, false)
        val expandOption = ExpandParser(edm, OData.newInstance(), HashMap(), ArrayList())
            .parse(UriTokenizer("Datastreams"), edmEntity)
        val options = EntitySerializerOptions.with().contextURL(ContextURL.with()
            .type(edmEntity)
            .build())
            .select(selectOption)
            //.expand(expandOption)
            .build()

        val edmx = listOf(EdmxReference(URI.create("http://example.com")))
        val serviceMetadata = ServiceMetadataImpl(provider, edmx, null)

        val result = serializer.entity(serviceMetadata,edmEntity, entity, options)
        val stringo = IOUtils.toString(result.content)
    }
}