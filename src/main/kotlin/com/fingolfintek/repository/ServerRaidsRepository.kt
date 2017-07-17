package com.fingolfintek.repository

import com.fingolfintek.model.redis.RedisChannelRaids
import org.springframework.data.repository.CrudRepository

interface ServerRaidsRepository : CrudRepository<RedisChannelRaids, String>
