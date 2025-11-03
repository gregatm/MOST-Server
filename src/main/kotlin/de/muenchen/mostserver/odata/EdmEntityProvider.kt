package de.muenchen.mostserver.odata

@Retention(value = AnnotationRetention.RUNTIME)
@Target(allowedTargets = [ AnnotationTarget.CLASS ])
annotation class EdmEntityProvider(val namespace: String, val container: String = "Container", val type: String = "", val typeSet: String = "")
