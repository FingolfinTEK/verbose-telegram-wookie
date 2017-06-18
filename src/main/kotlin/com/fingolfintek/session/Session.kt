package com.fingolfintek.session

import io.vavr.control.Try
import java.time.ZonedDateTime

open class Session(
    val createdOn: ZonedDateTime = ZonedDateTime.now(),
    var validUntil: ZonedDateTime = createdOn.plusDays(2),
    var damagesByUsers: LinkedHashMap<String, List<Int>> = LinkedHashMap(),
    var closedExplicitly: Boolean = false) {

  fun close() {
    closedExplicitly = true
    validUntil = ZonedDateTime.now()
  }

  fun attributeDamageFor(user: String, damageProcessor: () -> Int): Int {
    return Try.of({ damageProcessor.invoke() })
        .peek { damagesByUsers.put(user, damagesByUsers.computeIfAbsent(user, { ArrayList() }) + it) }
        .get()
  }
}
