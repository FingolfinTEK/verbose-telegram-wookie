package com.fingolfintek.model.redis

import com.fingolfintek.model.ChannelRaids
import com.fingolfintek.model.Raid
import io.vavr.Tuple
import io.vavr.collection.Stream
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

@RedisHash("sessions")
open class RedisRaid(
    @Id val channelAndName: String,
    damagesByUsers: LinkedHashMap<String, ArrayList<Int>>,
    val createdOn: ZonedDateTime,
    val validUntil: ZonedDateTime,
    val closedExplicitly: Boolean = false,
    @TimeToLive(unit = TimeUnit.DAYS) val ttl: Long = 2) {

  val damagesByUsers: List<RedisDamagesByUser> =
      damagesByUsers.map { RedisDamagesByUser(it.key, it.value) }

  constructor() : this("")

  constructor(name: String, s: Raid = Raid(name)) :
      this(name, s.damagesByUsers, s.createdOn, s.validUntil, s.closedExplicitly)

  fun raidName() = channelAndName.split("&&")[1]

  fun toRaid() = Raid(raidName(), damagesAsMap(), createdOn, validUntil, closedExplicitly)

  private fun damagesAsMap() =
      Stream.ofAll(damagesByUsers)
          .toJavaMap({ LinkedHashMap<String, ArrayList<Int>>() }, { Tuple.of(it.user, it.damages) })
}

fun fromChannelRaids(channel: String, channelRaids: ChannelRaids): List<RedisRaid> {
  return channelRaids.doWithRaids {
    it.map { RedisRaid("$channel&&${it.key}", it.value) }.toList()
  }
}
