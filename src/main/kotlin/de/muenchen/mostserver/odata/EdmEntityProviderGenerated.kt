package de.muenchen.mostserver.odata

import org.apache.olingo.commons.api.edm.FullQualifiedName
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType
import org.apache.olingo.commons.api.edm.provider.CsdlSchema

class EdmEntityProviderGenerated(private val namespace: String,
                                 private val csdlEntityType: Map<String, R2DbcEntityType>,
                                 private val csdlEntityContainer: CsdlEntityContainer,
                                 private val csdlEntitySet: Map<String, CsdlEntitySet>,
                                 private val csdlSchemas: List<CsdlSchema>
    ) : CsdlAbstractEdmProvider() {
    override fun getEntityType(entityTypeName: FullQualifiedName?): R2DbcEntityType? {
        if (entityTypeName != null)
            return csdlEntityType[entityTypeName.fullQualifiedNameAsString]

        return null
    }

    override fun getEntitySet(
        entityContainer: FullQualifiedName?,
        entitySetName: String?
    ): CsdlEntitySet? {
        if (entityContainer == FullQualifiedName(namespace, csdlEntityContainer.name)) {
            return csdlEntitySet[entitySetName]
        }
        return super.getEntitySet(entityContainer, entitySetName)
    }

    override fun getEntityContainer(): CsdlEntityContainer {
        return csdlEntityContainer
    }

    override fun getSchemas(): List<CsdlSchema?> {
        return csdlSchemas
    }

    override fun getEntityContainerInfo(entityContainerName: FullQualifiedName?): CsdlEntityContainerInfo? {
        if (entityContainerName == null || entityContainerName == FullQualifiedName(namespace, csdlEntityContainer.name)) {
            val info = CsdlEntityContainerInfo()
            info.containerName = FullQualifiedName(namespace, csdlEntityContainer.name)
            return info
        }
        return super.getEntityContainerInfo(entityContainerName)
    }
}