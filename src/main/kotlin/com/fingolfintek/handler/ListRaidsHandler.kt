package com.fingolfintek.handler

import com.fingolfintek.session.RaidSessions
import com.fingolfintek.session.Session
import io.vavr.collection.LinkedHashMap
import net.dv8tion.jda.core.entities.Message
import org.springframework.stereotype.Component

@Component
open class ListRaidsHandler(val raidSessions: RaidSessions) : MessageHandler {

  private val messageRegex = "!r(aid)? (list|show)( all)?".toRegex()

  override fun isApplicableTo(message: Message): Boolean =
      message.content.matches(messageRegex)

  override fun processMessage(message: Message) {
    raidSessions.doWithSessions {
      when {
        it.nonEmpty() -> sendRaidReportFor(it, message)
        else -> sendNoActiveRaidsMessageFor(message)
      }
    }
  }

  private fun sendRaidReportFor(sessions: LinkedHashMap<String, Session>, message: Message) {
    val raidReport = sessions.toStream()
        .map {
          "${it._1} - users registered: ${it._2.damagesByUsers.size()}, ends ${it._2.validUntil}"
        }.mkString("\n")
    return message.channel.sendMessage("Active raids:\n$raidReport").queue()
  }

  private fun sendNoActiveRaidsMessageFor(message: Message) =
      message.channel.sendMessage("No currently active raids").queue()

}