package de.muenchen.mostserver.controller

import de.muenchen.mostserver.controller.adpater.ThingDtoAdapter
import de.muenchen.mostserver.controller.dto.Thing
import de.muenchen.mostserver.data.dao.ThingDao
import de.muenchen.mostserver.data.repository.ThingsRepository
import de.muenchen.mostserver.services.ThingsService
import org.apache.olingo.commons.api.IConstants
import org.apache.olingo.commons.api.constants.Constantsv01
import org.apache.olingo.commons.api.format.ContentType
import org.apache.olingo.server.core.serializer.json.ODataJsonSerializer
import org.keycloak.authorization.client.AuthzClient
import org.keycloak.representations.idm.authorization.AuthorizationRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*
import java.util.function.Function

@RestController
@RequestMapping("api/v1.1")
class SensorThingsAPI(private val thingsService: ThingsService,
                      private val thingsRepository: ThingsRepository,
                      private val authzClient: AuthzClient,
    private val oauth2AuthorizationClientManager: OAuth2AuthorizedClientManager) {

    private val log: Logger = LoggerFactory.getLogger(SensorThingsAPI::class.java)

    @GetMapping("Things")
    fun getThings(): Mono<List<Thing>> {
        SecurityContextHolder.getContext().authentication
        val auth = SecurityContextHolder.getContext().authentication
        if (auth is OAuth2AuthenticationToken) {
            val authRequest = OAuth2AuthorizeRequest.withClientRegistrationId(auth.authorizedClientRegistrationId)
                .principal(auth)
                .build()
            val client = oauth2AuthorizationClientManager.authorize(authRequest);
            val token = client?.accessToken!!

            log.info("User with access token: '{}'", token.tokenValue)

            val request = AuthorizationRequest()
            request.addPermission(null, "read")
            val t = authzClient.authorization(token.tokenValue).authorize(request)
            val rpt = authzClient.protection().introspectRequestingPartyToken(t.token)
            val readPerm : HashSet<Long> = HashSet<Long>()
            for (permission in rpt.permissions)
            {
                if (permission.scopes.contains("read") && permission.resourceName.startsWith("THING_"))
                {
                    val id = permission.resourceName.substringAfter('_')
                    readPerm.add(id.toLong())
                }
            }
            return thingsService.getThingsForUser(readPerm)
        }
        return thingsRepository
            .findAll()
            .map(
                Function { thing: ThingDao? -> ThingDtoAdapter.daoToDto(thing) }
            )
            .collectList()
    }

    @PostMapping("Things")
    fun createThing(@RequestBody thing: Thing?): Mono<Thing?> {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth is OAuth2AuthenticationToken) {
            val authRequest = OAuth2AuthorizeRequest.withClientRegistrationId(auth.authorizedClientRegistrationId)
                .principal(auth)
                .build()
            val client = oauth2AuthorizationClientManager.authorize(authRequest);
            val token = client?.accessToken!!

            log.info("User with access token: '{}'", token?.tokenValue)

            val request = AuthorizationRequest()
            request.addPermission("THING", "write")
            authzClient.authorization(token.tokenValue).authorize(request)
            return thingsService.createThing(thing, auth, token)
        } else {
            throw RuntimeException("No valid token")
        }
    }

    @GetMapping("Things({id})/**")
    fun getThing(@PathVariable id: UUID, @RequestParam("\$select") select: String): Mono<Thing?> {
        log.info("Looking for thing '{}'", id)
        val serializer = ODataJsonSerializer(ContentType.APPLICATION_JSON, Constantsv01())
        return thingsRepository.findById(id)
            .doOnNext { t -> log.info("Retrieved Thing({}, {}, {})", t.id, t.aclId, t.name) }
            .map<Thing?>(Function { thing: ThingDao? -> ThingDtoAdapter.daoToDto(thing) })
    }

    @GetMapping("permissions")
    fun getPermissions(): Mono<Any> {
        return Mono.just(AuthorizationRequest())
            .doOnNext { it.addPermission(null, "read") }
            .map { request -> authzClient.authorization("test", "test").authorize(request) }
            .map { response -> response.token }
    }
}