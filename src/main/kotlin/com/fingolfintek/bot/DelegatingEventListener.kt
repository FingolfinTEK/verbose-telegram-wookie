package com.fingolfintek.bot

import com.fingolfintek.handler.MessageHandler
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.springframework.stereotype.Component

@Component
open class DelegatingEventListener(
    val handlers: List<MessageHandler>) : ListenerAdapter() {

  override fun onMessageReceived(event: MessageReceivedEvent) {
    handlers.forEach { it.handle(event.message) }
  }


}