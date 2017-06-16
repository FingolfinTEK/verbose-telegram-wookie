package com.fingolfintek.session.redis

import org.springframework.data.repository.CrudRepository

interface RaidsRepository : CrudRepository<RedisChannelSessions, String>
