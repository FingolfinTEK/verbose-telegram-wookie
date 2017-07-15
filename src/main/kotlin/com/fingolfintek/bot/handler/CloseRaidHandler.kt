package com.fingolfintek.bot.handler

import com.fingolfintek.bot.BotProperties
import com.fingolfintek.bot.Messenger
import com.fingolfintek.service.RaidService
import net.dv8tion.jda.core.entities.Message
import org.springframework.stereotype.Component

@Component
open class CloseRaidHandler(
    private val properties: BotProperties,
    private val messenger: Messenger,
    private val raidService: RaidService) : MessageHandler {

  private val messageRegex = "!r(?:aid)? (\\w+) (close|stop)".toRegex()

  override fun isApplicableTo(message: Message): Boolean =
      message.content.matches(messageRegex)

  override fun processMessage(message: Message) {
    when {
      properties.isAuthorAnOfficer(message) -> closeRaidSessionFor(message)
      else -> messenger.sendActionUnauthorizedMessageFor(message.channel.id)
    }
  }

  private fun closeRaidSessionFor(message: Message) {
    val name = resolveMessageNameFor(message)
    raidService.closeRaidFor(message.channel.id, name)
  }

  private fun resolveMessageNameFor(message: Message) =
      messageRegex.replace(message.content, "$1").toUpperCase()
}
