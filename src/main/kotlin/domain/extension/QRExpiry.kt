package com.example.domain.extension

import java.time.Duration

enum class QRExpiry(
    val duration: Duration,
) {
    TEN_MINUTES(Duration.ofMinutes(10)),
    ONE_HOUR(Duration.ofHours(1)),
    THREE_HOUR(Duration.ofHours(3)),
    SIX_HOUR(Duration.ofHours(6)),
    ;

    companion object {
        fun of(duration: Long) =
            QRExpiry.entries
                .firstOrNull { it.duration.seconds == duration }
                .let { requireNotNull(it) { "illegal qr duration" } }
    }
}
