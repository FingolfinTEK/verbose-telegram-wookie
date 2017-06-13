package com.fingolfintek.handler

import com.fingolfintek.bot.BotProperties
import com.fingolfintek.session.RaidSessions
import com.fingolfintek.session.Session
import net.dv8tion.jda.core.entities.Message
import org.springframework.stereotype.Component

@Component
open class StartRaidHandler(
    val properties: BotProperties, val raidSessions: RaidSessions) : MessageHandler {

  private val messageRegex = "!r(?:aid)? (\\w+) start".toRegex()

  override fun isApplicableTo(message: Message): Boolean =
      message.content.matches(messageRegex)

  override fun processMessage(message: Message) {
    when {
      properties.isAuthorAnOfficer(message) -> startRaidSessionFor(message)
      else -> sendActionUnauthorizedMessageFor(message)
    }
  }

  private fun resolveMessageNameFor(message: Message) =
      messageRegex.replace(message.content, "$1").toUpperCase()

  private fun startRaidSessionFor(message: Message) {
    val name = resolveMessageNameFor(message)
    raidSessions.startSession(name)
        .onSuccess { sendRaidStartedMessageFor(message, it) }
        .onFailure { sendFailureMessageFor(message) }
  }

  private fun sendRaidStartedMessageFor(message: Message, session: Session) {
    val name = resolveMessageNameFor(message)
    val infoMessage = "$name now started and will last until ${session.validUntil}"
    message.channel.sendMessage(infoMessage).queue()
  }

  private fun sendActionUnauthorizedMessageFor(message: Message) =
      message.channel.sendMessage("Sorry, only officers can start the raid!").queue()

  private fun sendFailureMessageFor(message: Message) =
      message.channel.sendMessage(
          "Sorry, I experienced an error and could not start the raid!"
      ).queue()

}