package com.fingolfintek.service

import com.fingolfintek.bot.Messenger
import com.fingolfintek.model.ServerRaids
import com.fingolfintek.model.redis.ChannelRaidsRepository
import com.fingolfintek.model.redis.RedisChannelSessions
import com.fingolfintek.model.redis.ServerRaidsRepository
import io.vavr.collection.Stream
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

const val SESSION_EXPIRY_TASK_DELAY_MILLIS = 30000L

@Component
open class RaidService(
    private val messenger: Messenger,
    private val serverRaids: ServerRaids,
    private val serverRaidsRepository: ServerRaidsRepository,
    private val channelRaidsRepository: ChannelRaidsRepository) {

  private val logger = LoggerFactory.getLogger(javaClass)

  fun startRaidFor(channelId: String, raidName: String) {
    serverRaids.doWithChannelRaids(channelId, {
      it.startRaidWithName(raidName)
          .onSuccess { messenger.sendRaidStartedMessageFor(channelId, it) }
          .onFailure { sendFailureMessageFor(channelId, it) }
    })
  }

  private fun sendFailureMessageFor(channelId: String, it: Throwable) {
    logger.error("Error performing action", it)
    messenger.sendFailureMessageFor(channelId)
  }

  fun closeRaidFor(channelId: String, name: String) {
    serverRaids.doWithChannelRaids(channelId, {
      it.closeRaidWithName(name)
          .onSuccess { messenger.sendRaidReportMessageFor(channelId, it!!) }
          .onFailure { sendFailureMessageFor(channelId, it) }
    })
  }

  fun sendRaidListMessageFor(channelId: String) {
    serverRaids.doWithChannelRaids(channelId, {
      it.doWithRaids {
        when {
          it.isNotEmpty() -> messenger.sendRaidListMessageFor(channelId, it)
          else -> messenger.sendNoActiveRaidsMessageFor(channelId)
        }
      }
    })
  }

  @Scheduled(fixedDelay = SESSION_EXPIRY_TASK_DELAY_MILLIS)
  fun closeExpiredSessions() {
    serverRaids.doWithChannelRaids({ channelId, raids ->
      raids.doWithRaids {
        it.values.filter { session -> !session.closedExplicitly }
            .filter { session -> session.validUntil.isBefore(ZonedDateTime.now()) }
            .forEach({ session -> closeRaidFor(channelId, session.raidName) })
      }
    })
  }

  @Scheduled(fixedDelay = SESSION_EXPIRY_TASK_DELAY_MILLIS)
  fun deleteExplicitlyClosedSessions() {
    serverRaids.removeAllExplicitlyClosedRaids()
    deleteAllExplicitlyClosedRaidsFromDb()
  }

  private fun deleteAllExplicitlyClosedRaidsFromDb() {
    serverRaidsRepository.findAll()
        .forEach { deleteAllExplicitlyClosedRaidsFromDbFor(it) }
  }

  private fun deleteAllExplicitlyClosedRaidsFromDbFor(channel: RedisChannelSessions) {
    val closedRaids = Stream.ofAll(channel.sessions ?: emptyList())
        .filter { it.closedExplicitly }
        .peek { channelRaidsRepository.delete(it) }
        .toList()

    if (closedRaids.nonEmpty()) {
      channel.sessions?.removeAll(closedRaids)
      serverRaidsRepository.save(channel)
    }
  }
}
