package de.muenchen.mostserver.data.dao

import de.muenchen.mostserver.odata.EdmEntityProvider
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.apache.olingo.commons.api.edm.geo.Geospatial
import org.springframework.data.annotation.Id
import java.time.LocalDateTime
import java.util.UUID

@Table(name = "Datastream")
@EdmEntityProvider(namespace = "Odata.MOSTServer", type = "Datastream")
open class DatastreamDao(@Id val id: UUID,
                    val name: String?,
                    val description: String?,
                    val observedArea: Geospatial?,
                    val phenomomTime: LocalDateTime?,
                    val resultTime: LocalDateTime?,

                    @ManyToOne
                    @JoinColumn(name = "sensor_id")
                    val sensor: SensorDao?,

                    @ManyToOne
                    @JoinColumn(name = "thing_id")
                    val thing: ThingDao?) {
    constructor(id: UUID): this(id, null, null,null,null, null,null,null)
}