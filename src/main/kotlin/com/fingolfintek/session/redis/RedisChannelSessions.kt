package com.fingolfintek.session.redis

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Reference
import org.springframework.data.redis.core.RedisHash

@RedisHash("raids")
class RedisChannelSessions(
    @Id val channel: String,
    @Reference val sessions: List<RedisChannelRaid>? = ArrayList())
