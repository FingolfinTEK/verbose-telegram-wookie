package com.fingolfintek.session.redis

import com.fingolfintek.session.RaidSessions
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Reference
import org.springframework.data.redis.core.RedisHash

@RedisHash("raids")
class RedisChannelSessions(
    @Id val channel: String,
    @Reference val sessions: List<RedisRaidSession> = ArrayList()) {

  constructor(channel: String, sessions: RaidSessions) :
      this(channel, toRedisRaidSessions(channel, sessions))
}

private fun toRedisRaidSessions(channel: String, sessions: RaidSessions): List<RedisRaidSession> {
  return sessions.doWithSessions {
    it.entries
        .map { RedisRaidSession("$channel&&${it.key}", it.value) }
        .toList()
  }
}
