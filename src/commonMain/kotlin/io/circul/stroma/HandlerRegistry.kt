package io.circul.stroma

import kotlin.reflect.KClass

typealias Handler<E> = (E) -> Unit

abstract class HandlerRegistry<T : Any> {

    private val handlers: MutableMap<KClass<*>, MutableList<Handler<*>>> = mutableMapOf()
    fun <E : T> register(eventClass: KClass<E>, handler: Handler<E>) {
        handlers.getOrPut(eventClass) { mutableListOf() }.add(handler)
    }

    inline fun <reified E : T> register(noinline handler: Handler<E>) =
        register(E::class, handler)

    fun <E : T> handle(event: E) {
        @Suppress("UNCHECKED_CAST")
        val handlers = handlers[event::class] as? List<Handler<E>>
        handlers?.forEach { it(event) }
    }
}
