package de.muenchen.mostserver.data.dao

import de.muenchen.mostserver.odata.EdmNamespace
import jakarta.persistence.Embeddable

@EdmNamespace("Odata.MOSTServer")
@Embeddable
class UnitOfMeasurement(
    val name: String?,
    val unitSymbol: String?,
    val uri: String?
) {
    constructor(): this(null, null, null)
}