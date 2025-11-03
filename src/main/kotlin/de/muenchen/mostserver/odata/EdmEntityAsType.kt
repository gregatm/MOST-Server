package de.muenchen.mostserver.odata

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class EdmEntityAsType(val value: KClass<*>, val mapper: KClass<out EdmEntityTypeMapper> = DefaultEdmEntityTypeMapper::class, val isCollection: Boolean = false)
