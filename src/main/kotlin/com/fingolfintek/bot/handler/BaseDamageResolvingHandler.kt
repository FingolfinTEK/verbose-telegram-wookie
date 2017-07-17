package com.fingolfintek.bot.handler

import com.fingolfintek.bot.Messenger
import com.fingolfintek.model.ServerRaids
import com.fingolfintek.ocr.TotalDamageResolver
import com.fingolfintek.util.using
import net.dv8tion.jda.core.entities.Message
import java.io.File
import java.net.URL

abstract class BaseDamageResolvingHandler(
    private val raidSessions: ServerRaids,
    private val messenger: Messenger,
    private val damageResolver: TotalDamageResolver) : MessageHandler {

  private val mozzilaAgent =
      "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) " +
          "Chrome/23.0.1271.95 Safari/537.11"

  protected fun processDamageReportFor(message: Message, imageUrl: String, raidName: String) {
    val damageProcessor = { resolveDamageFor(imageUrl) }
    val channelId = message.channel.id
    val user = message.author.name
    raidSessions.doWithChannelRaids(channelId, {
      it.attributeDamage(raidName, user, damageProcessor)
          .onFailure { messenger.sendUnknownRaidMessageFor(channelId, raidName) }
          .onSuccess { messenger.sendDamageAttributedMessageFor(channelId, it) }
    })
  }

  private fun resolveDamageFor(url: String): Int {
    val tempFile = createTempFile()
    try {
      downloadImage(url, tempFile)
      return damageResolver.resolveDamageFrom(tempFile.absolutePath)
    } finally {
      tempFile.delete()
    }
  }

  private fun downloadImage(url: String, tempFile: File) {
    using {
      val connection = URL(url).openConnection()
      connection.setRequestProperty("User-Agent", mozzilaAgent)
      val input = connection.getInputStream().autoClose()
      val output = tempFile.outputStream().autoClose()
      input.copyTo(output)
    }
  }
}
