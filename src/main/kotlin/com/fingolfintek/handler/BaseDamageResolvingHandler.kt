package com.fingolfintek.handler

import com.fingolfintek.session.RaidSessions
import net.dv8tion.jda.core.entities.Message
import java.io.File
import java.net.URL

abstract class BaseDamageResolvingHandler(
    val raidSessions: RaidSessions, val damageResolver: TotalDamageResolver) : MessageHandler {

  private val mozzilaAgent =
      "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) " +
          "Chrome/23.0.1271.95 Safari/537.11"

  protected fun processDamageReportFor(message: Message, imageUrl: String) {
    val sessionName = message.content.toUpperCase()
    raidSessions
        .attributeDamage(sessionName, message.author.name, { resolveDamageFor(imageUrl) })
        .onFailure { sendUnknownRaidMessageFor(message, sessionName) }
        .onSuccess {
          val damageMessage = "Resolved damage was $it"
          message.channel.sendMessage(damageMessage).queue()
        }
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
    val connection = URL(url).openConnection()
    connection.setRequestProperty("User-Agent", mozzilaAgent)
    connection.getInputStream().copyTo(tempFile.outputStream())
  }

  private fun sendUnknownRaidMessageFor(message: Message, sessionName: String) {
    message.channel.sendMessage("Unknown raid $sessionName")
  }
}