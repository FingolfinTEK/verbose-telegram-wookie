package com.fingolfintek.handler

import com.fingolfintek.ocr.TotalDamageResolver
import com.fingolfintek.session.RaidSessions
import net.dv8tion.jda.core.entities.EmbedType
import net.dv8tion.jda.core.entities.Message
import org.springframework.stereotype.Component

@Component
open class EmbedDamageResolvingHandler(
    raidSessions: RaidSessions, damageResolver: TotalDamageResolver)
  : BaseDamageResolvingHandler(raidSessions, damageResolver) {

  override fun isApplicableTo(message: Message): Boolean =
      message.embeds?.filter { it.type == EmbedType.IMAGE }?.isNotEmpty() ?: false

  override fun processMessage(message: Message) {
    message.embeds
        .filter { it.type == EmbedType.IMAGE }
        .forEach {
          val sessionName = message.content.replace(it.url, "").trim().toUpperCase()
          processDamageReportFor(message, it.url, sessionName)
        }
  }


}
