package com.fingolfintek.handler

import com.fingolfintek.session.RaidSessions
import net.dv8tion.jda.core.entities.Message
import org.springframework.stereotype.Component

@Component
open class AttachmentDamageResolvingHandler(
    raidSessions: RaidSessions, damageResolver: TotalDamageResolver)
  : BaseDamageResolvingHandler(raidSessions, damageResolver) {

  override fun isApplicableTo(message: Message): Boolean =
      message.attachments?.filter { it.isImage }?.isNotEmpty() ?: false

  override fun processMessage(message: Message) {
    message.attachments
        .filter { it.isImage }
        .forEach { processDamageReportFor(message, it.proxyUrl) }
  }


}