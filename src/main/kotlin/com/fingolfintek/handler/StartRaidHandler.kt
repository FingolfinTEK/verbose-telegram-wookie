package com.fingolfintek.handler

import com.fingolfintek.bot.BotProperties
import com.fingolfintek.bot.Messenger
import com.fingolfintek.service.RaidService
import net.dv8tion.jda.core.entities.Message
import org.springframework.stereotype.Component

@Component
open class StartRaidHandler(
    private val properties: BotProperties,
    private val messenger: Messenger,
    private val raidService: RaidService) : MessageHandler {

  private val messageRegex = "!r(?:aid)? (\\w+) start".toRegex()

  override fun isApplicableTo(message: Message): Boolean =
      message.content.matches(messageRegex)

  override fun processMessage(message: Message) {
    when {
      properties.isAuthorAnOfficer(message) -> startRaidSessionFor(message)
      else -> messenger.sendActionUnauthorizedMessageFor(message.channel.id)
    }
  }

  private fun startRaidSessionFor(message: Message) =
      raidService.startRaidFor(message.channel.id, resolveRaidNameFor(message))

  private fun resolveRaidNameFor(message: Message) =
      messageRegex.replace(message.content, "$1").toUpperCase()

}
