package de.muenchen.mostserver.odata

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType

class R2DbcComplexType: CsdlComplexType() {
    var typeClass: Class<*>? = null
}