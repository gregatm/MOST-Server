package de.muenchen.mostserver.data.dao

import de.muenchen.mostserver.odata.EdmEntityProvider
import jakarta.persistence.Basic
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.apache.olingo.commons.api.edm.geo.Geospatial
import java.time.LocalDateTime
import java.util.UUID

@Table(name = "Datastream")
@EdmEntityProvider(namespace = "Odata.MOSTServer", type = "Datastream")
@Entity
open class DatastreamDao(@Id var id: UUID,
                         @Basic
                         var test: String?,
                         var description: String?,
                         var observedArea: String?,
                         var phenomomTime: LocalDateTime?,
                         var resultTime: LocalDateTime?,

                         @Embedded
                         var unitOfMeasurement: UnitOfMeasurement?,

                         @ManyToOne
                         @JoinColumn(name = "sensor_id")
                         var sensor: SensorDao?,

                         @ManyToOne
                         @JoinColumn(name = "thing_id")
                         var thing: ThingDao?) {
    constructor(id: UUID): this(id, null, null, null,null,null, null,null,null)
}