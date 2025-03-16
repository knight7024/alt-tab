package com.example

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class FakeClock : Clock() {
    private var delegate = fixed(Instant.now(), ZoneId.systemDefault())

    override fun instant(): Instant = delegate.instant()

    override fun withZone(zone: ZoneId?): Clock = delegate.withZone(zone)

    override fun getZone(): ZoneId = delegate.zone

    fun tick(tock: Duration) {
        delegate = fixed(delegate.instant() + tock, delegate.zone)
    }

    fun reset() {
        delegate = fixed(Instant.now(), ZoneId.systemDefault())
    }
}
