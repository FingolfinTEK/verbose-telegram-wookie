package com.fingolfintek.session.redis

import com.fingolfintek.session.RaidSessions
import com.fingolfintek.session.Session
import io.vavr.collection.Stream
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

@RedisHash("sessions")
open class RedisRaidSession(
    @Id val channelAndName: String,
    val createdOn: ZonedDateTime,
    var validUntil: ZonedDateTime,
    val damagesByUsers: List<DamagesByUser>,
    val closedExplicitly: Boolean = false,
    @TimeToLive(unit = TimeUnit.DAYS) val ttl: Long = 2) {

  constructor() : this("")

  constructor(name: String, s: Session = Session()) :
      this(name, s.createdOn, s.validUntil, fromSessionDamages(s.damagesByUsers), s.closedExplicitly)

  fun toSession() = Session(createdOn, validUntil, sessionDamages(), closedExplicitly)

  fun sessionDamages(): LinkedHashMap<String, List<Int>> {
    val damages = Stream.ofAll(damagesByUsers)
        .toMap { it.toTuple() }
        .toJavaMap()
    return LinkedHashMap(damages)
  }
}

fun fromRaidSessions(channel: String, sessions: RaidSessions): List<RedisRaidSession> {
  return sessions.doWithSessions {
    it.entries
        .map { RedisRaidSession("$channel&&${it.key}", it.value) }
        .toList()
  }
}

