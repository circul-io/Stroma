package io.circul.stroma

import kotlinx.datetime.Clock

abstract class DomainEvent {
    open val timestamp = Clock.System.now().toEpochMilliseconds()
}
