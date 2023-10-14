package io.circul.stroma

import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

sealed class TestEvent : DomainEvent() {
    data class NumberUpdated(val number: Int) : TestEvent()
    data class MessageUpdated(val message: String) : TestEvent()
}

class TestAggregate(override val id: String) : AggregateRoot<String, TestEvent>() {

    private var _message: String? = null
    val message get() = _message

    private var _number: Int = 0
    val number get() = _number

    fun setPositiveNumber(num: Int) = applyEvent {
        require(num >= 0)
        if (num != number) {
            TestEvent.NumberUpdated(num)
        } else {
            null
        }
    }

    fun setMessage(msg: String) = applyEvent {
        if (msg != message) {
            TestEvent.MessageUpdated(msg)
        } else {
            null
        }
    }

    companion object {
        // Rehydration
        operator fun invoke(id: String, events: List<TestEvent>): TestAggregate =
            TestAggregate(id).apply { rehydrate(events) }

        operator fun invoke(state: TestAggregateState) = TestAggregate(state.id).apply {
            version = state.version
            _message = state.message
            _number = state.number
        }
    }

    override fun handleEvent(event: TestEvent) = when (event) {
        is TestEvent.NumberUpdated -> _number = event.number.also { println("EventA: $it") }
        is TestEvent.MessageUpdated -> _message = event.message.also { println("EventB: $it") }
    }
}

data class FakeEvent(val x: Int) : DomainEvent()

class ReadModelUpdater : HandlerRegistry<FakeEvent>() {

    init {
        register(FakeEvent::class, ::handleProcessStarted)
        register<FakeEvent> { println("PRINTING FROM generic in init") }
    }

    private fun handleProcessStarted(event: FakeEvent) {
        println("Printing from method using KClass arg register: $event")
    }
}

data class TestAggregateState(val version: Long, val id: String, val message: String?, val number: Int)
class AggregateRootTest {

    @Test
    @JsName("test_aggregate")
    fun `test aggregate`() {
        val aggregate = TestAggregate("ABC123")
        assertEquals("ABC123", aggregate.id)
        assertEquals(null, aggregate.message)
        assertEquals(0, aggregate.number)
        aggregate.setMessage("Hello")
        assertEquals("Hello", aggregate.message)
        aggregate.setPositiveNumber(42)
        assertEquals(42, aggregate.number)
        assertFailsWith<IllegalArgumentException> { aggregate.setPositiveNumber(-21) }
        aggregate.setMessage("Hello2")
        assertEquals("Hello2", aggregate.message)
        aggregate.setMessage("Hello2")
        assertEquals("Hello2", aggregate.message)
        val events = aggregate.flushEvents().size
        println(events)
        assertEquals(3, events)
        // ensure aggregate events are cleared
        assertEquals(0, aggregate.flushEvents().size)
    }

    @Test
    @JsName("test_aggregate_rehydration")
    fun `test aggregate rehydration`() {
        val aggregate = TestAggregate("ABC123")
        aggregate.setMessage("Hello")
        aggregate.setPositiveNumber(42)
        aggregate.setMessage("Hello2")
        aggregate.setMessage("Hello2")
        val events = aggregate.flushEvents()
        val rehydrated = TestAggregate("ABC123", events)
        assertEquals("ABC123", rehydrated.id)
        assertEquals("Hello2", rehydrated.message)
        assertEquals(42, rehydrated.number)
        assertEquals(aggregate.id, rehydrated.id)
        assertEquals(aggregate.message, rehydrated.message)
        assertEquals(aggregate.number, rehydrated.number)
    }

    @Test
    @JsName("test_withEvents")
    fun `test withEvents`() {
        val aggregate = TestAggregate("ABC123")
        aggregate.setMessage("Hello")
        aggregate.setPositiveNumber(42)
        aggregate.setMessage("Hello2")
        aggregate.setMessage("Hello2")
        val events = aggregate.withEvents { size == 3 }
        assertEquals(3, events.size)
        assertEquals(0, aggregate.flushEvents().size)
    }

    @Test
    @JsName("test_handler_registry")
    fun `test handler registry`() {
        val readModelUpdater = ReadModelUpdater()
        readModelUpdater.register<FakeEvent> {
            println("print from external register")
        }

        readModelUpdater.handle(FakeEvent(42))
    }
}
