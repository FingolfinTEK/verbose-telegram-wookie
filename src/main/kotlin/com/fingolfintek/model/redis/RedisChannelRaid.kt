package com.fingolfintek.model.redis

import com.fingolfintek.model.ChannelRaids
import com.fingolfintek.model.Raid
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

@RedisHash("sessions")
open class RedisChannelRaid(
    @Id val channelAndName: String,
    val damagesByUsers: LinkedHashMap<String, ArrayList<Int>>,
    val createdOn: ZonedDateTime,
    val validUntil: ZonedDateTime,
    val closedExplicitly: Boolean = false,
    @TimeToLive(unit = TimeUnit.DAYS) val ttl: Long = 2) {

  constructor() : this("")

  constructor(name: String, s: Raid = Raid(name)) :
      this(name, s.damagesByUsers, s.createdOn, s.validUntil, s.closedExplicitly)

  fun raidName() = channelAndName.split("&&")[1]

  fun toSession() = Raid(raidName(), damagesByUsers, createdOn, validUntil, closedExplicitly)
}

fun fromChannelRaids(channel: String, channelRaids: ChannelRaids): List<RedisChannelRaid> {
  return channelRaids.doWithRaids {
    it.map { RedisChannelRaid("$channel&&${it.key}", it.value) }.toList()
  }
}
