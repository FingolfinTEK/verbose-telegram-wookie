package com.fingolfintek.bot

import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.hooks.EventListener
import org.springframework.stereotype.Component

@Component
open class DamageParsingBot(
    properties: BotProperties, listeners: List<EventListener>) {

  val jda: JDA = JDABuilder(AccountType.BOT)
      .setToken(properties.token)
      .setAutoReconnect(true)
      .addEventListener(*listeners.toTypedArray())
      .buildAsync()

}