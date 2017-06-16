package com.fingolfintek.session.redis

import org.springframework.data.repository.CrudRepository

interface RaidSessionsRepository : CrudRepository<RedisRaidSession, String>
