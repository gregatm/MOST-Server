package de.muenchen.mostserver.olingo

import org.apache.olingo.commons.api.edm.FullQualifiedName
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty

class CsdlNavigationPropertyBuilder {
    val prop = CsdlNavigationProperty()

    fun name(name: String): CsdlNavigationPropertyBuilder {
        prop.name = name
        return this
    }

    fun type(type: FullQualifiedName): CsdlNavigationPropertyBuilder {
        prop.setType(type)
        return this
    }

    fun nullable(nullable: Boolean): CsdlNavigationPropertyBuilder {
        prop.isNullable = nullable
        return this
    }

    fun collection(collection: Boolean): CsdlNavigationPropertyBuilder {
        prop.isCollection = collection
        return this
    }

    fun partner(partner: String): CsdlNavigationPropertyBuilder {
        prop.partner = partner
        return this
    }

    fun build(): CsdlNavigationProperty {
        return prop
    }
}