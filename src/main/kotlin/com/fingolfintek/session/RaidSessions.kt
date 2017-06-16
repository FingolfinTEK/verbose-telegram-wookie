package com.fingolfintek.session

import io.vavr.control.Option
import io.vavr.control.Try
import java.time.ZonedDateTime.now

open class RaidSessions(raids: Map<String, Session> = HashMap()) {
  private var sessions: LinkedHashMap<String, Session> = LinkedHashMap(raids)

  fun startSession(name: String): Try<Session> {
    return doWithSessions {
      Try.success(it.computeIfAbsent(name, { Session() }))
    }
  }

  fun <T> doWithSessions(block: (LinkedHashMap<String, Session>) -> T): T {
    return synchronized(sessions, { block(sessions) })
  }

  fun closeSession(name: String): Try<Session?> {
    return doWithSessions {
      Option.of(it[name]).peek {
        it?.closedExplicitly = true
        it?.validUntil = now()
      }
    }.toTry()
  }

  fun attributeDamage(
      sessionName: String, user: String, damageProcessor: () -> Int): Try<Int> {
    return doWithSessions {
      Option.of(it[sessionName])
          .orElse {
            if (sessionName.isNullOrBlank()) firstStillValidSession() else Option.none()
          }
          .map {
            val damage = damageProcessor.invoke()
            it!!.damagesByUsers.computeIfAbsent(user, { arrayListOf<Int>() }) + damage
            damage
          }
    }.toTry()
  }

  private fun firstStillValidSession(): Option<Session> {
    val session = sessions
        .filter { e -> e.value.validUntil.isAfter(now()) }
        .map { e -> e.value }.firstOrNull()
    return Option.of(session)
  }

}
