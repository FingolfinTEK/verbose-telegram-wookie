package com.fingolfintek.model

import java.time.ZonedDateTime

open class Raid(
    val raidName: String,
    val damagesByUsers: LinkedHashMap<String, ArrayList<Int>> = LinkedHashMap(),
    val createdOn: ZonedDateTime = ZonedDateTime.now(),
    var validUntil: ZonedDateTime = createdOn.plusDays(2),
    var closedExplicitly: Boolean = false) {

  fun attributeDamageFor(user: String, damage: Int) {
    synchronized(damagesByUsers, {
      damagesByUsers.computeIfAbsent(user, { arrayListOf<Int>() }).add(damage)
    })
  }
}
