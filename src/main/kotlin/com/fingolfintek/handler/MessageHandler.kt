package com.fingolfintek.handler

import io.vavr.control.Option
import net.dv8tion.jda.core.entities.Message

interface MessageHandler {

  fun isApplicableTo(message: Message): Boolean

  fun processMessage(message: Message)

  fun handle(message: Message) =
      Option.of(message)
          .filter(this::isApplicableTo)
          .peek(this::processMessage)
}