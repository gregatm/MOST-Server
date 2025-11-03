package de.muenchen.mostserver.odata

import de.muenchen.mostserver.data.dao.IDao
import net.bytebuddy.ByteBuddy
import net.bytebuddy.asm.Advice
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.dynamic.loading.ByteArrayClassLoader
import net.bytebuddy.implementation.Implementation
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.implementation.bind.annotation.Origin
import net.bytebuddy.implementation.bind.annotation.RuntimeType
import net.bytebuddy.implementation.bind.annotation.This
import net.bytebuddy.matcher.ElementMatchers
import org.springframework.cglib.proxy.InvocationHandler
import org.springframework.data.annotation.Id
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.UUID

fun <T: Any> getIdMethod(clazz: Class<T>): Method {
    return clazz.declaredFields
        .filter { it.annotations.any { a -> a is Id } }
        .firstNotNullOf { f -> clazz.declaredMethods.firstOrNull { m -> m.name.lowercase() == "get${f.name}" } }
}

fun <T: Any> createProxy(dao: T, context: DaoContext): T {
    val clazz = dao::class.java
    val method = getIdMethod(clazz)
    val id = method.invoke(dao)
    return createProxy(clazz, context, id)
}

fun <T: Any> createProxy(clazz: Class<T>, context: DaoContext, id: Any): T{
    val method = getIdMethod(clazz)
    val l = ByteBuddy()
        .subclass(clazz)
        .name(clazz.name + "\$Proxy")
        .method(ElementMatchers.not(ElementMatchers.`is`(method)))
        .intercept(MethodDelegation.withDefaultConfiguration().filter(ElementMatchers.named("invoke")).to(DaoContextPullAdvice(context)))
        .make()
        .load(clazz.classLoader).loaded.getDeclaredConstructor(id::class.java)
        .newInstance(id)

    return l as T
}

class DaoContext(val context: HashMap<Class<*>, Map<Any, Any>> = HashMap())
{
    fun <T> getParameters(clazz: Class<T>) : Map<Any, T> {
        return context.getOrElse(clazz) { mapOf<Any, T>() } as Map<Any, T>
    }

    fun addParameters(dao: Any) {
        val m = getIdMethod(dao::class.java)
        val id = m.invoke(dao) ?: return
        val map = context.getOrPut(dao::class.java) { HashMap() } as MutableMap
        map[id] = dao
    }
}

class DaoContextPullAdvice(val context: DaoContext) {

    @RuntimeType
    fun invoke(
        @This obj: Any,
        @Origin method: Method
    ): Any? {
        val clazz = obj::class.java.superclass
        println(method.name)
        val m = getIdMethod(clazz)
        val id = m.invoke(obj)
        val proxyObj = context.getParameters(clazz)[id]
        if (proxyObj != null) {
            return method.invoke(proxyObj)
        }
        return null;
    }

}
