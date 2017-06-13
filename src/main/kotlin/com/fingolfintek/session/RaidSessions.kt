package com.fingolfintek.session

import io.vavr.collection.LinkedHashMap
import io.vavr.collection.List
import io.vavr.control.Option
import io.vavr.control.Try
import org.springframework.stereotype.Component
import java.time.ZonedDateTime.now

@Component
open class RaidSessions {
  var sessions: LinkedHashMap<String, Session> = LinkedHashMap.empty<String, Session>()

  fun startSession(name: String): Try<Session> {
    return doWithSessions {
      val session = Session()
      sessions = it.put(name, session)
      Try.success(session)
    }
  }

  fun <T> doWithSessions(block: (LinkedHashMap<String, Session>) -> T): T {
    return synchronized(sessions, { block(sessions) })
  }

  fun closeSession(name: String): Try<Session> {
    return doWithSessions {
      it.get(name).peek {
        it.closedExplicitly = true
        it.validUntil = now()
      }
    }.toTry()
  }

  fun attributeDamage(sessionName: String, user: String, damage: Int): Try<Session> {
    return doWithSessions {
      it.get(sessionName)
          .orElse {
            if (sessionName.isBlank()) firstStillValidSession() else Option.none()
          }
          .peek {
            val damagesByUsers = it.damagesByUsers
            it.damagesByUsers = damagesByUsers.get(user)
                .orElse { Option.of(List.empty()) }
                .map { damagesByUsers.put(user, it.push(damage)) }
                .get()
          }
    }.toTry()
  }

  private fun firstStillValidSession(): Option<Session> {
    return sessions
        .filter { _, s -> s.validUntil.isAfter(now()) }
        .headOption().map { it._2 }
  }

}