package com.fingolfintek.bot.handler

import com.fingolfintek.service.RaidService
import net.dv8tion.jda.core.entities.Message
import org.springframework.stereotype.Component

@Component
open class ListRaidsHandler(
    private val raidService: RaidService) : MessageHandler {

  private val messageRegex = "!r(aid)? (list|show)( all)?".toRegex()

  override fun isApplicableTo(message: Message): Boolean =
      message.content.matches(messageRegex)

  override fun processMessage(message: Message) {
    val channelId = message.channel.id
    raidService.sendRaidListMessageFor(channelId)
  }

}
