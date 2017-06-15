package com.fingolfintek.session

import org.springframework.data.repository.CrudRepository

interface RaidsRepository : CrudRepository<RedisRaidSessions, String>
