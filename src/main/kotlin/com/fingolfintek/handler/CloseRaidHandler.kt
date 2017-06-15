package com.fingolfintek.handler

import com.fingolfintek.bot.BotProperties
import com.fingolfintek.session.ChannelSessions
import com.fingolfintek.session.Session
import net.dv8tion.jda.core.entities.Message
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
open class CloseRaidHandler(
    val properties: BotProperties, val raidSessions: ChannelSessions) : MessageHandler {

  private val messageRegex = "!r(?:aid) (\\w+) (close|stop)".toRegex()

  override fun isApplicableTo(message: Message): Boolean =
      message.content.matches(messageRegex)

  override fun processMessage(message: Message) {
    when {
      properties.isAuthorAnOfficer(message) -> closeRaidSessionFor(message)
      else -> sendActionUnauthorizedMessageFor(message)
    }
  }

  private fun closeRaidSessionFor(message: Message) {
    val name = resolveMessageNameFor(message)
    raidSessions.doWithSessions(message, {
      it.closeSession(name)
          .onSuccess { sendRaidReportMessageFor(message, it!!) }
          .onFailure { sendFailureMessageFor(message) }
    })
  }

  private fun sendRaidReportMessageFor(message: Message, session: Session) {
    val name = resolveMessageNameFor(message)
    message.channel.sendFile(
        exportDamagesToCsv(session),
        "$name-${LocalDate.now()}.csv",
        null
    ).queue()
  }

  private fun resolveMessageNameFor(message: Message) =
      messageRegex.replace(message.content, "$1").toUpperCase()

  private fun exportDamagesToCsv(session: Session): ByteArray {
    return session.damagesByUsers
        .flatMap { t -> t.value.map { "${t.key},$it" } }
        .joinToString("\n", "User,Damage done").toByteArray()
  }

  private fun sendActionUnauthorizedMessageFor(message: Message) {
    message.channel.sendMessage("Sorry, only officers can close the raid!").queue()
  }

  private fun sendFailureMessageFor(message: Message) =
      message.channel.sendMessage(
          "Sorry, I experienced an error and could not close the raid!"
      ).queue()
}
