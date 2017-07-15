package com.fingolfintek.model.redis

import org.springframework.data.repository.CrudRepository

interface ServerRaidsRepository : CrudRepository<RedisChannelSessions, String>
