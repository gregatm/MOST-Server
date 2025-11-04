package de.muenchen.mostserver.data.dao

import de.muenchen.mostserver.odata.EdmNamespace

@EdmNamespace("Odata.MOSTServer")
class UnitOfMeasurement(
    val name: String?,
    val unitSymbol: String?,
    val uri: String?
) {
}