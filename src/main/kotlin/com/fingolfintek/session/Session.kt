package com.fingolfintek.session

import io.vavr.collection.LinkedHashMap
import io.vavr.collection.List
import java.time.ZonedDateTime

open class Session(createdOn: ZonedDateTime = ZonedDateTime.now()) {
  var validUntil: ZonedDateTime = createdOn.plusDays(2)
  var damagesByUsers = LinkedHashMap.empty<String, List<Int>>()!!
  var closedExplicitly: Boolean = false
}