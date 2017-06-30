package com.fingolfintek.session

import io.vavr.control.Option
import io.vavr.control.Try
import java.time.ZonedDateTime.now

open class ChannelRaids(raids: Map<String, Raid> = HashMap()) {
  private var raidsByName: LinkedHashMap<String, Raid> = LinkedHashMap(raids)

  fun startRaidWithName(name: String): Try<Raid> {
    return doWithRaids {
      Try.success(it.computeIfAbsent(name, { Raid(name) }))
    }
  }

  fun <T> doWithRaids(block: (LinkedHashMap<String, Raid>) -> T): T {
    return synchronized(raidsByName, { block(raidsByName) })
  }

  fun closeRaidWithName(name: String): Try<Raid?> {
    return doWithRaids {
      Option.of(it[name]).peek {
        it?.closedExplicitly = true
        it?.validUntil = now()
      }
    }.toTry()
  }

  fun attributeDamage(
      raidName: String, user: String, damageProcessor: () -> Int): Try<Int> {
    return doWithRaids {
      Option.of(it[raidName])
          .orElse { if (raidName.isNullOrBlank()) firstStillValidRaid() else Option.none() }
          .map {
            val damage = damageProcessor.invoke()
            it!!.attributeDamageFor(user, damage)
            damage
          }
    }.toTry()
  }

  private fun firstStillValidRaid(): Option<Raid> {
    val raid = raidsByName
        .filter { e -> e.value.validUntil.isAfter(now()) }
        .map { e -> e.value }.firstOrNull()
    return Option.of(raid)
  }

}
