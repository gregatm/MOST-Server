package de.muenchen.mostserver.odata

import de.muenchen.mostserver.controller.dto.Thing
import de.muenchen.mostserver.data.QueryBuilder
import de.muenchen.mostserver.data.dao.DatastreamDao
import de.muenchen.mostserver.data.dao.ProjectDao
import de.muenchen.mostserver.data.dao.SensorDao
import de.muenchen.mostserver.data.dao.ThingDao
import de.muenchen.mostserver.data.dao.UnitOfMeasurement
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.micrometer.core.instrument.util.IOUtils
import io.r2dbc.spi.Batch
import jakarta.persistence.Persistence
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Selection
import org.apache.olingo.commons.api.constants.Constantsv01
import org.apache.olingo.commons.api.data.ContextURL
import org.apache.olingo.commons.api.edm.FullQualifiedName
import org.apache.olingo.commons.api.edmx.EdmxReference
import org.apache.olingo.commons.api.format.ContentType
import org.apache.olingo.commons.core.edm.EdmEntityTypeImpl
import org.apache.olingo.commons.core.edm.EdmProviderImpl
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.serializer.EntitySerializerOptions
import org.apache.olingo.server.core.ServiceMetadataImpl
import org.apache.olingo.server.core.serializer.json.ODataJsonSerializer
import org.apache.olingo.server.core.uri.parser.ExpandParser
import org.apache.olingo.server.core.uri.parser.Parser
import org.apache.olingo.server.core.uri.parser.SelectParser
import org.apache.olingo.server.core.uri.parser.UriTokenizer
import org.apache.openjpa.conf.OpenJPAConfigurationImpl
import org.apache.openjpa.meta.MetaDataRepository
import org.apache.openjpa.persistence.PersistenceMetaDataFactory
import org.apache.openjpa.persistence.criteria.CriteriaBuilderImpl
import org.apache.openjpa.persistence.criteria.OpenJPACriteriaQuery
import org.apache.openjpa.persistence.meta.MetamodelImpl
import org.apache.openjpa.persistence.meta.Types
import org.apache.openjpa.persistence.query.LiteralExpression
import org.apache.openjpa.persistence.query.ParameterExpression
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy
import org.springframework.data.r2dbc.dialect.DialectResolver
import org.springframework.data.relational.core.sql.Join.JoinType
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import java.net.URI
import java.net.URL
import java.time.Duration
import java.time.temporal.TemporalUnit
import java.util.Optional
import java.util.Properties
import java.util.UUID

import kotlin.test.assertContains
import kotlin.test.assertEquals

class EdmEntityParserTests {


    fun getProvider(): EdmEntityProviderGenerated {
        return createFromClass(
            listOf(ThingDao::class.java, SensorDao::class.java, DatastreamDao::class.java),
            listOf(UnitOfMeasurement::class.java)
        )
    }

    @Test
    fun parseThing() {

        val provider = getProvider()
        assertEquals("Thing", provider.
                getEntityType(FullQualifiedName("Odata.MOSTServer", "Thing"))!!.name)

    }

    @Test
    fun parseSelect() {
        val selectStatement = "id,name"
        val provider = getProvider()
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

        val t = ThingDao(UUID.randomUUID())

        val provider = getProvider()
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
        val provider = getProvider()
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
        val dc = DatastreamDao(d.id, "DatastreamName", "DatastreamDescription", null, null, null, null, null, tp)
        val t = ThingDao(tf.id, 0, "ThingName", listOf(dp))

        context.addParameters(dc)
        context.addParameters(t)


        val provider = getProvider()
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



    @Test
    fun createDb() {

        val config = OpenJPAConfigurationImpl()

        val factory = PersistenceMetaDataFactory()


        val metaRepos = MetaDataRepository()
        metaRepos.setConfiguration(config)
        metaRepos.metaDataFactory = factory
        metaRepos.addMetaData(ThingDao::class.java)
        metaRepos.addMetaData(DatastreamDao::class.java)
        metaRepos.addMetaData(SensorDao::class.java)
        val metamodel = MetamodelImpl(metaRepos)

        Types.Entity<ThingDao>(metaRepos.getMetaData(ThingDao::class.java, null, false), metamodel)

        metaRepos.getMetaData(ThingDao::class.java, null, false)


        val b = CriteriaBuilderImpl().setMetaModel(metamodel)
        val q = b.createQuery(ThingDao::class.java)


        val r = q.from(ThingDao::class.java)
        //val d = q.from(DatastreamDao::class.java)
        val j = r.join<ThingDao, DatastreamDao>("datastreams", jakarta.persistence.criteria.JoinType.LEFT)
        j.on(b.equal(r.get<ThingDao>("id"), j.get<DatastreamDao>("thing")))
        //q.select(r.get("id"))
        //q.select(r.get("name"))
        q.groupBy(r.get<ThingDao>("id"), r.get<ThingDao>("name"))

        val t = q.multiselect(
            r.get<ThingDao>("id").alias("id"),
            r.get<ThingDao>("name").alias("name"),
            b.function("array_remove", ThingDao::class.java,
                b.function("array_agg", ThingDao::class.java,j.get<DatastreamDao>("id")
                ), b.literal(null)).alias("datastreams"))

        val sql = (q as OpenJPACriteriaQuery).toCQL()
    }

}