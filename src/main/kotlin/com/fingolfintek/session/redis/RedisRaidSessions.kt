package com.fingolfintek.session.redis

import com.fingolfintek.session.RaidSessions
import com.fingolfintek.session.Session
import io.vavr.Tuple
import io.vavr.collection.Stream
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.time.ZonedDateTime

@RedisHash("sessions")
open class RedisRaidSession(
    @Id val channelAndName: String,
    val createdOn: ZonedDateTime,
    var validUntil: ZonedDateTime,
    val damagesByUsers: LinkedHashMap<String, ArrayList<Int>>,
    var closedExplicitly: Boolean = false) {

  constructor(name: String, s: Session) :
      this(name, s.createdOn, s.validUntil, s.damagesByUsers, s.closedExplicitly)

  fun toSession() = Session(createdOn, validUntil, damagesByUsers, closedExplicitly)
}
