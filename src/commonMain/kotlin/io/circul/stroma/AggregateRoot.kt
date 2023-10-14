package io.circul.stroma


abstract class AggregateRoot<ID, T : DomainEvent> {

    private val events = mutableListOf<T>()

    var version = 0L
        protected set

    abstract val id: ID

    protected fun applyEvent(action: () -> T?) = action()?.let { applyEvent(it) }

    protected fun applyEvent(event: T) {
        handleEvent(event)
        version++
        events.add(event)
    }

    abstract fun handleEvent(event: T)

    fun flushEvents(): List<T> = events.toList().also { events.clear() }

    fun withEvents(block: List<T>.() -> Boolean): List<T> = events.toList().also {
        if (it.block()) events.clear()
    }

    fun rehydrate(events: List<T>) = events.forEach {
        handleEvent(it)
        version++
    }
}
