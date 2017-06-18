package com.fingolfintek.session

import com.fingolfintek.session.redis.*
import io.vavr.Tuple
import io.vavr.collection.Map
import io.vavr.collection.Stream
import net.dv8tion.jda.core.entities.Message
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
open class ChannelSessions(
    private val raidsRepository: RaidsRepository,
    private val sessionsRepository: RaidSessionsRepository) {

  private lateinit var sessions: Map<String, RaidSessions>

  fun doWithSessions(message: Message, processor: (RaidSessions) -> Any) {
    val channel = message.channel.name
    doWithSessions(channel, processor)
  }

  private fun doWithSessions(channel: String, processor: (RaidSessions) -> Any) {
    createSessionsForChannelIfNeeded(channel)
    sessions.get(channel).peek { processor(it) }
    sessions.get(channel).peek { persist(channel, it) }
  }

  private fun createSessionsForChannelIfNeeded(channel: String) {
    synchronized(sessions, {
      sessions = sessions.computeIfAbsent(channel, { RaidSessions() })._2
    })
  }

  private fun persist(channel: String, raids: RaidSessions) {
    val redisRaids = sessionsRepository.save(fromRaidSessions(channel, raids))
    raidsRepository.save(RedisChannelSessions(channel, redisRaids.toList()))
  }

  @PostConstruct
  private fun initFromRedis() {
    sessions = Stream.ofAll(raidsRepository.findAll())
        .toMap { Tuple.of(it.channel, toRaidSessions(it.sessions)) }
  }

  private fun toRaidSessions(sessionsFromDb: List<RedisRaidSession>?): RaidSessions {
    val sessions = Stream.ofAll(sessionsFromDb ?: emptyList())
        .toMap { Tuple.of(it.channelAndName.split("&&")[1], it.toSession()) }
        .toJavaMap()
    return RaidSessions(sessions)
  }

}
