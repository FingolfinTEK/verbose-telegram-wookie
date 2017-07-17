package com.fingolfintek.model.redis

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Reference
import org.springframework.data.redis.core.RedisHash

@RedisHash("raids")
class RedisChannelRaids(
    @Id val channel: String,
    @Reference val sessions: MutableList<RedisRaid>? = ArrayList())
