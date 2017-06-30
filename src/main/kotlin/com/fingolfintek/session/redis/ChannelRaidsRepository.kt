package com.fingolfintek.session.redis

import org.springframework.data.repository.CrudRepository

interface ChannelRaidsRepository : CrudRepository<RedisChannelRaid, String> {
  fun findAllByClosedExplicitly(closedExplicitly: Boolean): List<RedisChannelRaid>
}
