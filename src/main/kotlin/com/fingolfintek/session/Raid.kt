package com.fingolfintek.session

import java.time.ZonedDateTime

open class Raid(
    val raidName: String,
    val createdOn: ZonedDateTime = ZonedDateTime.now(),
    var validUntil: ZonedDateTime = createdOn.plusDays(2),
    var damagesByUsers: LinkedHashMap<String, ArrayList<Int>> = LinkedHashMap(),
    var closedExplicitly: Boolean = false)
