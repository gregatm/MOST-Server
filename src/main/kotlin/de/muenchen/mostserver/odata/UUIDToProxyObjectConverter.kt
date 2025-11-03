package de.muenchen.mostserver.odata

import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.GenericConverter
import java.util.UUID

class UUIDToProxyObjectConverter(val clazzes: Set<Class<*>>, val context: DaoContext): GenericConverter {

    override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair?> {
        return clazzes.map { clazz -> GenericConverter.ConvertiblePair(UUID::class.java, clazz) }
            .toSet()
    }

    override fun convert(
        source: Any?,
        sourceType: TypeDescriptor,
        targetType: TypeDescriptor
    ): Any? {
        val clazz = clazzes.firstOrNull(targetType.type::equals)
        if (clazz != null && source is UUID)
            return createProxy(clazz, context, source)
        return null
    }
}