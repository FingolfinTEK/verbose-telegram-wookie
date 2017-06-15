package com.fingolfintek.handler

import com.fingolfintek.ocr.TotalDamageResolver
import com.fingolfintek.session.ChannelSessions
import net.dv8tion.jda.core.entities.Message
import org.springframework.stereotype.Component

@Component
open class AttachmentDamageResolvingHandler(
    raidSessions: ChannelSessions, damageResolver: TotalDamageResolver)
  : BaseDamageResolvingHandler(raidSessions, damageResolver) {

  override fun isApplicableTo(message: Message): Boolean =
      message.attachments?.filter { it.isImage }?.isNotEmpty() ?: false

  override fun processMessage(message: Message) {
    message.attachments
        .filter { it.isImage }
        .forEach { processDamageReportFor(message, it.url, message.content.toUpperCase()) }
  }


}
