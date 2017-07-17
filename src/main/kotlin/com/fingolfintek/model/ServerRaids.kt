package com.fingolfintek.model

import com.fingolfintek.model.redis.RedisChannelRaids
import com.fingolfintek.model.redis.RedisRaid
import com.fingolfintek.model.redis.fromChannelRaids
import com.fingolfintek.repository.ChannelRaidsRepository
import com.fingolfintek.repository.ServerRaidsRepository
import io.vavr.Tuple
import io.vavr.collection.Map
import io.vavr.collection.Stream
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
open class ServerRaids(
    private val serverRaidsRepository: ServerRaidsRepository,
    private val channelRaidsRepository: ChannelRaidsRepository) {

  private lateinit var raids: Map<String, ChannelRaids>

  fun removeAllExplicitlyClosedRaids() {
    doWithChannelRaids({ _, channelRaids ->
      channelRaids.doWithRaids { raids ->
        raids.filter { (_, raid) -> raid.closedExplicitly }
            .forEach({ (_, raid) ->
              raids.remove(raid.raidName)
            })
      }
    })
  }

  fun doWithChannelRaids(processor: (String, ChannelRaids) -> Any) {
    raids.forEach {
      processor(it._1, it._2)
    }
  }

  fun <T> doWithChannelRaids(channelId: String, processor: (ChannelRaids) -> T) {
    createSessionsForChannelIfNeeded(channelId)
    raids.get(channelId).peek {
      processor(it)
      persist(channelId, it)
    }
  }

  private fun createSessionsForChannelIfNeeded(channel: String) {
    synchronized(raids, {
      raids = raids.computeIfAbsent(channel, { ChannelRaids() })._2
    })
  }

  private fun persist(channel: String, raids: ChannelRaids) {
    val redisRaids = channelRaidsRepository.save(fromChannelRaids(channel, raids))
    serverRaidsRepository.save(RedisChannelRaids(channel, redisRaids.toMutableList()))
  }

  @PostConstruct
  private fun initFromRedis() {
    raids = Stream.ofAll(serverRaidsRepository.findAll())
        .toMap { Tuple.of(it.channel, toChannelRaids(it.sessions)) }
  }

  private fun toChannelRaids(raidsFromDb: List<RedisRaid>?): ChannelRaids {
    val raids = Stream.ofAll(raidsFromDb ?: emptyList())
        .toMap { Tuple.of(it.raidName(), it.toRaid()) }
        .toJavaMap()
    return ChannelRaids(raids)
  }

}
