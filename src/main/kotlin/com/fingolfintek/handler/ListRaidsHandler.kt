package com.fingolfintek.handler

import com.fingolfintek.session.ChannelSessions
import com.fingolfintek.session.Session
import net.dv8tion.jda.core.entities.Message
import org.springframework.stereotype.Component

@Component
open class ListRaidsHandler(val raidSessions: ChannelSessions) : MessageHandler {

  private val messageRegex = "!r(aid)? (list|show)( all)?".toRegex()

  override fun isApplicableTo(message: Message): Boolean =
      message.content.matches(messageRegex)

  override fun processMessage(message: Message) {
    raidSessions.doWithSessions(message, {
      it.doWithSessions {
        when {
          it.isNotEmpty() -> sendRaidReportFor(it, message)
          else -> sendNoActiveRaidsMessageFor(message)
        }
      }
    })
  }

  private fun sendRaidReportFor(sessions: LinkedHashMap<String, Session>, message: Message) {
    val raidReport = sessions.map {
      "${it.key} - users registered: ${it.value.damagesByUsers.size}, ends ${it.value.validUntil}"
    }.joinToString("\n")
    return message.channel.sendMessage("Active raids:\n$raidReport").queue()
  }

  private fun sendNoActiveRaidsMessageFor(message: Message) =
      message.channel.sendMessage("No currently active raids").queue()

}
