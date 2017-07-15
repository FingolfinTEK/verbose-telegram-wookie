package com.fingolfintek.bot.handler

import com.fingolfintek.bot.Messenger
import com.fingolfintek.model.ServerRaids
import com.fingolfintek.ocr.TotalDamageResolver
import net.dv8tion.jda.core.entities.Message
import org.springframework.stereotype.Component

@Component
open class AttachmentDamageResolvingHandler(
    raidSessions: ServerRaids, messenger: Messenger, damageResolver: TotalDamageResolver)
  : BaseDamageResolvingHandler(raidSessions, messenger, damageResolver) {

  override fun isApplicableTo(message: Message): Boolean =
      message.attachments?.filter { it.isImage }?.isNotEmpty() ?: false

  override fun processMessage(message: Message) {
    message.attachments
        .filter { it.isImage }
        .forEach { processDamageReportFor(message, it.url, message.content.toUpperCase()) }
  }


}
