package com.fingolfintek.bot.handler

import io.vavr.control.Option
import io.vavr.control.Try
import net.dv8tion.jda.core.entities.Message

interface MessageHandler {

  fun isApplicableTo(message: Message): Boolean

  fun processMessage(message: Message)

  fun handle(message: Message): Try<Message> =
      Option.of(message)
          .filter(this::isApplicableTo)
          .peek(this::processMessage)
          .toTry()
}