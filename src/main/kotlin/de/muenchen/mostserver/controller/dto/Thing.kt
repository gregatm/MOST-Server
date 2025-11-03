package de.muenchen.mostserver.controller.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.muenchen.mostserver.odata.EdmEntityAsType
import de.muenchen.mostserver.odata.EdmEntityProvider
import jakarta.persistence.Id
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
@EdmEntityProvider(namespace = "Odata.MOSTServer")
class Thing(@field:EdmEntityAsType(String::class) @field:Id val id: UUID?, val name: String, )