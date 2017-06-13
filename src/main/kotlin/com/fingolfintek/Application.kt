package com.fingolfintek

import com.fingolfintek.bot.BotProperties
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties

@SpringBootApplication
@EnableConfigurationProperties(BotProperties::class)
open class Application

fun main(args: Array<String>) {
  SpringApplication.run(Application::class.java, *args)
}
