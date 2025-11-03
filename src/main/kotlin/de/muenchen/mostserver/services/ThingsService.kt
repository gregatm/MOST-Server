package de.muenchen.mostserver.services

import de.muenchen.mostserver.controller.adpater.ThingDtoAdapter
import de.muenchen.mostserver.controller.dto.Thing
import de.muenchen.mostserver.data.dao.ThingDao
import de.muenchen.mostserver.data.repository.ThingsRepository
import io.r2dbc.spi.ConnectionFactory
import org.apache.olingo.server.api.ODataHandler
import org.apache.olingo.server.api.ODataRequest
import org.flywaydb.core.internal.sqlscript.SqlStatement
import org.keycloak.authorization.client.AuthzClient
import org.keycloak.representations.idm.authorization.DecisionStrategy
import org.keycloak.representations.idm.authorization.PermissionRequest
import org.keycloak.representations.idm.authorization.ResourceOwnerRepresentation
import org.keycloak.representations.idm.authorization.ResourceRepresentation
import org.keycloak.representations.idm.authorization.ScopePermissionRepresentation
import org.keycloak.representations.idm.authorization.ScopeRepresentation
import org.keycloak.representations.idm.authorization.UmaPermissionRepresentation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.convert.R2dbcConverter
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy
import org.springframework.data.relational.core.sql.Expressions
import org.springframework.data.relational.core.sql.SQL
import org.springframework.data.relational.core.sql.StatementBuilder
import org.springframework.jdbc.`object`.SqlQuery
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.*
import java.util.function.Function


@Component
class ThingsService
@Autowired constructor(private val authzClient: AuthzClient,
                       private val repository: ThingsRepository,
                       private val connectionFactory: ConnectionFactory,
                       private val template: R2dbcEntityTemplate ) {

    private val log: Logger = LoggerFactory.getLogger(ThingsService::class.java)

    fun getThingsForUser(ids: Set<Long>): Mono<List<Thing>> {
        val mapper = template.dataAccessStrategy.getRowMapper(ThingDao::class.java)
        return Mono.from(connectionFactory.create())
            .flatMapMany {
                connection ->
                val sb = StringBuilder("SELECT * FROM thing WHERE acl_id IN (VALUES")

                for (id in ids) {
                    sb.append('(', id, "),")
                }
                if (sb.isNotEmpty()) {
                    sb.setLength(sb.length - 1);
                }
                sb.append(")")
                log.info("Execute query: {}", sb)
                connection.createStatement(sb.toString()).execute()
            }
            .flatMap { result -> result.map(mapper) }
            .map(ThingDtoAdapter::daoToDto)
            .collectList()
    }

    fun getThings() {


    }

    fun createThing(thing: Thing?, auth: OAuth2AuthenticationToken, token: OAuth2AccessToken): Mono<Thing?> {
        val t = ThingDtoAdapter.dtoToDao(thing)
        return repository.create(t)
            .doOnNext {

                val owner = auth.principal.attributes["preferred_username"] as String
                log.info("Policy for user {}", owner)
                val res = ResourceRepresentation()
                res.name = "THING_${it.aclId}"
                res.type = "ost:thing"
                res.ownerManagedAccess = true
                res.addScope("read", "write", "delete")
                res.owner = ResourceOwnerRepresentation(owner)
                val resp = authzClient.protection().resource().create(res)

                val umaPermBuider = { scope: String ->

                    val umaPerm = UmaPermissionRepresentation()
                    umaPerm.name = "THING_${it.aclId}_${scope.uppercase()}"
                    umaPerm.addScope(scope)
                    umaPerm.addResource(resp.id)
                    umaPerm.addUser(owner)
                    umaPerm.decisionStrategy = DecisionStrategy.UNANIMOUS
                    umaPerm.owner = owner
                    umaPerm
                }
                authzClient.protection(token.tokenValue).policy(resp.id).create(umaPermBuider("read"))
                authzClient.protection(token.tokenValue).policy(resp.id).create(umaPermBuider("write"))
                authzClient.protection(token.tokenValue).policy(resp.id).create(umaPermBuider("delete"))
            }
            .map(ThingDtoAdapter::daoToDto)
    }

    fun processor() {

    }
}