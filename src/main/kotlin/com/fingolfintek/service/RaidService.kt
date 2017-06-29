package com.fingolfintek.service

import com.fingolfintek.bot.Messenger
import com.fingolfintek.session.ServerRaids
import com.fingolfintek.session.redis.ChannelRaidsRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

const val SESSION_EXPIRY_TASK_DELAY_MILLIS = 300000L

@Component
open class RaidService(
    private val messenger: Messenger,
    private val serverRaids: ServerRaids,
    private val channelRaidsRepository: ChannelRaidsRepository) {

  fun startRaidFor(channelId: String, raidName: String) {
    serverRaids.doWithChannelRaids(channelId, {
      it.startRaidWithName(raidName)
          .onSuccess { messenger.sendRaidStartedMessageFor(channelId, it) }
          .onFailure { messenger.sendFailureMessageFor(channelId) }
    })
  }

  fun closeRaidFor(channelId: String, name: String) {
    serverRaids.doWithChannelRaids(channelId, {
      it.closeRaidWithName(name)
          .onSuccess { messenger.sendRaidReportMessageFor(channelId, it!!) }
          .onFailure { messenger.sendFailureMessageFor(channelId) }
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
    serverRaids.doWithChannelRaids({ _, channelRaids ->
      channelRaids.doWithRaids { raids ->
        raids.filter { (_, raid) -> raid.closedExplicitly }
            .forEach({ (_, raid) ->
              raids.remove(raid.raidName)
              channelRaidsRepository.deleteAllByClosedExplicitly(true)
            })
      }
    })
  }
}
