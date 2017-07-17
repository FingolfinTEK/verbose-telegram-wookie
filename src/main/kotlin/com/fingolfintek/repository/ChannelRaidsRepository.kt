package com.fingolfintek.repository

import com.fingolfintek.model.redis.RedisRaid
import org.springframework.data.repository.CrudRepository

interface ChannelRaidsRepository : CrudRepository<RedisRaid, String> {
  fun findAllByClosedExplicitly(closedExplicitly: Boolean): List<RedisRaid>
}
