package com.fingolfintek.handler

import com.fingolfintek.bot.Messenger
import com.fingolfintek.ocr.TotalDamageResolver
import com.fingolfintek.session.ServerRaids
import net.dv8tion.jda.core.entities.EmbedType
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import org.springframework.stereotype.Component

@Component
open class EmbedDamageResolvingHandler(
    raidSessions: ServerRaids, messenger: Messenger, damageResolver: TotalDamageResolver)
  : BaseDamageResolvingHandler(raidSessions, messenger, damageResolver) {

  override fun isApplicableTo(message: Message): Boolean =
      message.embeds?.filter { it.type == EmbedType.IMAGE }?.isNotEmpty() ?: false

  override fun processMessage(message: Message) {
    message.embeds
        .filter { it.type == EmbedType.IMAGE }
        .forEach { processDamageReportFor(message, it) }
  }

  private fun processDamageReportFor(message: Message, it: MessageEmbed) {
    val raidName = resolveRaidNameFor(message, it)
    processDamageReportFor(message, it.url, raidName)
  }

  private fun resolveRaidNameFor(message: Message, it: MessageEmbed) =
      message.content.replace(it.url, "").trim().toUpperCase()


}
