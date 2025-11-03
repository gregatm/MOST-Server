package de.muenchen.mostserver.data.dao

import de.muenchen.mostserver.odata.EdmEntityAsType
import de.muenchen.mostserver.odata.EdmEntityProvider
import jakarta.persistence.OneToMany
import org.springframework.data.annotation.Id
import java.util.UUID

@EdmEntityProvider(namespace = "Odata.MOSTServer", type = "Project")
open class ProjectDao(@Id val id: UUID,
                 var name: String? = null,

                 @OneToMany
                 @EdmEntityAsType(SensorDao::class, isCollection = true)
                 var sensors: List<SensorDao>? = null,

                 @OneToMany
                 @EdmEntityAsType(ThingDao::class, isCollection = true)
                 var things: List<ThingDao>? = null,

                 @OneToMany
                 @EdmEntityAsType(DatastreamDao::class, isCollection = true)
                 var datastreams: List<DatastreamDao>? = null
)