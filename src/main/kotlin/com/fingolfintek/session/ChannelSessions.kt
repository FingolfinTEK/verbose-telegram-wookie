package com.fingolfintek.session

import io.vavr.Tuple
import io.vavr.collection.Map
import io.vavr.collection.Stream
import net.dv8tion.jda.core.entities.Message
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
open class ChannelSessions(val repository: RaidsRepository) {

  private lateinit var sessions: Map<String, RaidSessions>

  fun doWithSessions(message: Message, processor: (RaidSessions) -> Any) {
    val channel = message.channel.name
    doWithSessions(channel, processor)
  }

  private fun doWithSessions(channel: String, processor: (RaidSessions) -> Any) {
    createSessionsForChannelIfNeeded(channel)
    sessions.get(channel).peek {
      processor(it)
      persist(channel, it)
    }
  }

  private fun createSessionsForChannelIfNeeded(channel: String) {
    synchronized(sessions, {
      sessions = sessions.computeIfAbsent(channel, { RaidSessions() })._2
    })
  }

  private fun persist(channel: String, raids: RaidSessions) {
    repository.save(RedisRaidSessions(channel, raids))
  }

  @PostConstruct
  private fun initFromRedis() {
    sessions = Stream.ofAll(repository.findAll())
        .toMap { Tuple.of(it.channel, it.sessions) }
  }

}
