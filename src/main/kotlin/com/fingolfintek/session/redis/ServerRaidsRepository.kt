package com.fingolfintek.session.redis

import org.springframework.data.repository.CrudRepository

interface ServerRaidsRepository : CrudRepository<RedisChannelSessions, String>
