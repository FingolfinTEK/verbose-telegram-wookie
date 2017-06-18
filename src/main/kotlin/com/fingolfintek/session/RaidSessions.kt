package com.fingolfintek.session

import io.vavr.control.Option
import io.vavr.control.Try
import java.time.ZonedDateTime.now

open class RaidSessions(raids: Map<String, Session> = HashMap()) {
  private var sessions: LinkedHashMap<String, Session> = LinkedHashMap(raids)

  fun startSession(raidName: String): Try<Session> {
    return doWithSessions {
      Try.success(it.computeIfAbsent(raidName, { Session() }))
    }
  }

  fun <T> doWithSessions(block: (LinkedHashMap<String, Session>) -> T): T {
    return synchronized(sessions, { block(sessions) })
  }

  fun closeSession(raidName: String): Try<Session?> {
    return doWithSessions {
      Option.of(it[raidName]).peek { it?.close() }
    }.toTry()
  }

  fun attributeDamage(
      raidName: String, user: String, damageProcessor: () -> Int): Try<Int> {
    return doWithSessions {
      Option.of(it[raidName])
          .orElse { if (raidName.isNullOrBlank()) firstStillValidSession() else Option.none() }
          .map { it!!.attributeDamageFor(user, damageProcessor) }
          .toTry()
    }
  }

  private fun firstStillValidSession(): Option<Session> {
    val session = sessions
        .filter { e -> e.value.validUntil.isAfter(now()) }
        .map { e -> e.value }.firstOrNull()
    return Option.of(session)
  }

}
