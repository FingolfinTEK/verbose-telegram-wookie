package com.fingolfintek.session

import com.fingolfintek.session.redis.*
import io.vavr.Tuple
import io.vavr.collection.Map
import io.vavr.collection.Stream
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
open class ServerRaids(
    private val serverRaidsRepository: ServerRaidsRepository,
    private val channelRaidsRepository: ChannelRaidsRepository) {

  private lateinit var sessions: Map<String, ChannelRaids>

  fun doWithChannelRaids(processor: (String, ChannelRaids) -> Any) {
    sessions.forEach {
      processor(it._1, it._2)
    }
  }

  fun <T> doWithChannelRaids(channelId: String, processor: (ChannelRaids) -> T) {
    createSessionsForChannelIfNeeded(channelId)
    sessions.get(channelId).peek {
      processor(it)
      persist(channelId, it)
    }
  }

  private fun createSessionsForChannelIfNeeded(channel: String) {
    synchronized(sessions, {
      sessions = sessions.computeIfAbsent(channel, { ChannelRaids() })._2
    })
  }

  private fun persist(channel: String, raids: ChannelRaids) {
    val redisRaids = channelRaidsRepository.save(fromChannelRaids(channel, raids))
    serverRaidsRepository.save(RedisChannelSessions(channel, redisRaids.toList()))
  }

  @PostConstruct
  private fun initFromRedis() {
    sessions = Stream.ofAll(serverRaidsRepository.findAll())
        .toMap { Tuple.of(it.channel, toRaidSessions(it.sessions)) }
  }

  private fun toRaidSessions(sessionsFromDb: List<RedisChannelRaid>?): ChannelRaids {
    val sessions = Stream.ofAll(sessionsFromDb ?: emptyList())
        .toMap { Tuple.of(it.channelAndName.split("&&")[1], it.toSession()) }
        .toJavaMap()
    return ChannelRaids(sessions)
  }

}
