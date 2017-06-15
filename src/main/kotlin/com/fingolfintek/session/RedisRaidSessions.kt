package com.fingolfintek.session

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash("raids")
class RedisRaidSessions(@Id val channel: String, val sessions: RaidSessions = RaidSessions()) {
  constructor() : this("")
}


