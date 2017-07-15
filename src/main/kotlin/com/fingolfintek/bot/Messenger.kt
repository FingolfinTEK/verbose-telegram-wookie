package com.fingolfintek.bot

import com.fingolfintek.model.Raid
import net.dv8tion.jda.core.JDA
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
open class Messenger(private val jda: JDA) {

  fun sendRaidReportMessageFor(channelId: String, raid: Raid) =
      sendFile(channelId,
          "${raid.raidName}-${LocalDate.now()}.csv",
          exportDamagesToCsv(raid))

  fun sendFile(channelId: String, fileName: String, fileContent: ByteArray) =
      channelFor(channelId)
          .sendFile(fileContent, fileName, null)
          .queue()

  private fun channelFor(channelId: String) = jda.getTextChannelById(channelId)

  private fun exportDamagesToCsv(raid: Raid): ByteArray =
      raid.damagesByUsers
          .flatMap { t -> t.value.map { "${t.key},$it" } }
          .joinToString("\n", "User,Damage done")
          .toByteArray()

  fun sendUnknownRaidMessageFor(channelId: String, raidName: String) {
    sendMessage(channelId, "Unknown raid $raidName")
  }

  private fun sendMessage(channelId: String, message: String) =
      channelFor(channelId)
          .sendMessage(message)
          .queue()

  fun sendFailureMessageFor(channelId: String) =
      sendMessage(channelId, "Sorry, I experienced an error and could not do what you asked!")

  fun sendRaidListMessageFor(channelId: String, sessions: LinkedHashMap<String, Raid>) {
    val raidReport = sessions.values
        .filter { !it.closedExplicitly }
        .map {
          "${it.raidName} - users registered: ${it.damagesByUsers.size}, ends ${it.validUntil}"
        }.joinToString("\n")
    return sendMessage(channelId, "Active raids:\n$raidReport")
  }

  fun sendNoActiveRaidsMessageFor(channelId: String) =
      sendMessage(channelId, "No currently active raids")

  fun sendActionUnauthorizedMessageFor(channelId: String) =
      sendMessage(channelId, "Sorry, only officers can manage raids!")

  fun sendDamageAttributedMessageFor(channelId: String, damage: Int) =
      sendMessage(channelId, "Resolved damage was $damage")

  fun sendRaidStartedMessageFor(channelId: String, raid: Raid) =
      sendMessage(channelId, "${raid.raidName} now started and will last until ${raid.validUntil}")
}
