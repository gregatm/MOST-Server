package de.muenchen.mostserver.data.dao

import de.muenchen.mostserver.odata.EdmEntityAsType
import de.muenchen.mostserver.odata.EdmEntityProvider
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import java.util.UUID

@EdmEntityProvider(namespace = "Odata.MOSTServer", type = "Sensor")
@Entity
open class SensorDao(@Id var id: UUID?,
                     open var manufacturer: String? = null,
                     open var name: String? = null,
                     @EdmEntityAsType(DatastreamDao::class, isCollection = true)
                @OneToMany
                var datastreams: List<DatastreamDao>? = null,
                     @ManyToOne
                @JoinColumn
                var project: ProjectDao? = null
) {

    constructor(id: UUID) : this(id, null, null, null) {}
}